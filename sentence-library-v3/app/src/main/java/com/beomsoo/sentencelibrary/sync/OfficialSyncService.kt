package com.beomsoo.sentencelibrary.sync

import com.beomsoo.sentencelibrary.data.*
import java.util.UUID
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

data class SyncReport(val success:Int,val failed:Int,val errors:List<String>)

class OfficialSyncService(private val repository:ReadingRepository, private val client:JwOrgClient=JwOrgClient()) {
    suspend fun syncRange(maxYear:Int=2026,minYear:Int=2001,onProgress:(Int,Int)->Unit={_,_->}):SyncReport = coroutineScope {
        val candidates=JwOrgParser.candidates(maxYear,minYear)
        val semaphore=Semaphore(2)
        var completed=0
        val results=candidates.map { c -> async {
            semaphore.withPermit {
                val result=runCatching { syncCandidate(c) }
                synchronized(this@OfficialSyncService) { completed++; onProgress(completed,candidates.size) }
                result
            }
        }}.awaitAll()
        val errors=results.mapNotNull { it.exceptionOrNull()?.message }
        SyncReport(results.count { it.isSuccess }, errors.size, errors)
    }

    suspend fun syncYear(year:Int,onProgress:(Int,Int)->Unit={_,_->})=syncRange(year,year,onProgress)

    private suspend fun syncCandidate(c:OfficialIssueCandidate) {
        val url=JwOrgParser.issueUrl(c)
        try {
            val html=client.fetch(url)
            val parsed=JwOrgParser.parse(html,url)
            require(parsed.articles.isNotEmpty()) { "${c.code}: 기사 제목 없음" }
            val publicationName=parsed.publicationName.substringBefore('—').trim().ifBlank { parsed.publicationName }
            val issue=repository.issueByOfficialKey(c.code)?.let { old ->
                old.copy(year=c.year,label=JwOrgParser.issueLabel(c),sourceUrl=url,updatedAt=System.currentTimeMillis()).also { repository.upsertIssue(it) }
            } ?: repository.addIssue(publicationName,c.year,JwOrgParser.issueLabel(c)).let { created ->
                created.copy(officialKey=c.code,sourceUrl=url).also { repository.upsertIssue(it) }
            }
            parsed.articles.forEachIndexed { index, incoming ->
                val key="${c.code}:${incoming.officialKey}"
                val existing=repository.articleByOfficialKey(key)
                if(existing!=null) repository.upsertArticle(existing.copy(title=incoming.title,sourceUrl=incoming.sourceUrl,updatedAt=System.currentTimeMillis()))
                else repository.upsertArticle(Article("article-${UUID.randomUUID()}",issue.id,key,incoming.title,incoming.sourceUrl,sortOrder=index))
            }
            repository.syncMetadata(SyncMetadata(c.code,"success",System.currentTimeMillis(),System.currentTimeMillis(),null))
        } catch(t:Throwable) {
            repository.syncMetadata(SyncMetadata(c.code,"failed",System.currentTimeMillis(),null,t.message))
            throw t
        }
    }
}
