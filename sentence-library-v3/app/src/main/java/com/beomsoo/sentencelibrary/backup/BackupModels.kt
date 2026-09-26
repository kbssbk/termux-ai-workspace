package com.beomsoo.sentencelibrary.backup

import com.beomsoo.sentencelibrary.data.*
import com.beomsoo.sentencelibrary.domain.ProgressRules
import kotlinx.serialization.Serializable

@Serializable data class BackupPublication(val id:String, val name:String, val sortOrder:Int=0)
@Serializable data class BackupIssue(val id:String, val year:Int, val publication:String, val label:String, val publicationId:String="", val officialKey:String?=null, val sourceUrl:String?=null)
@Serializable data class BackupArticle(
    val id:String, val issueId:String, val title:String, val status:String, val progress:Int,
    val statusId:String="", val officialKey:String?=null, val sourceUrl:String?=null,
    val summary:String="", val reflection:String="", val application:String="", val updatedAt:Long=0L
)
@Serializable data class BackupQuote(val id:String, val issueId:String, val articleId:String, val sentence:String, val note:String="", val tags:List<String> = emptyList(), val location:String="")
@Serializable data class BackupTag(val id:String, val name:String, val sortOrder:Int=0)
@Serializable data class BackupStatus(val id:String, val name:String, val defaultProgress:Int, val adjustable:Boolean, val completed:Boolean, val showOnDashboard:Boolean=true, val sortOrder:Int=0)
@Serializable data class BackupCustomField(val id:String, val name:String, val type:String="text", val target:String="article", val sortOrder:Int=0, val enabled:Boolean=true)
@Serializable data class BackupCustomValue(val id:String, val fieldId:String, val ownerId:String, val value:String)

@Serializable
data class BackupV3(
    val schemaVersion:Int = 3,
    val exportedAt:Long = System.currentTimeMillis(),
    val publications:List<BackupPublication> = emptyList(),
    val issues:List<BackupIssue> = emptyList(),
    val articles:List<BackupArticle> = emptyList(),
    val quotes:List<BackupQuote> = emptyList(),
    val tags:List<BackupTag> = emptyList(),
    val statuses:List<BackupStatus> = emptyList(),
    val customFields:List<BackupCustomField> = emptyList(),
    val customValues:List<BackupCustomValue> = emptyList(),
    val preferences:Map<String,String> = emptyMap()
)

fun DataSnapshot.toBackupV3(): BackupV3 {
    val publicationById = publications.associateBy { it.id }
    val statusById = statuses.associateBy { it.id }
    val tagsById = tags.associateBy { it.id }
    val tagRefs = quoteTagRefs.groupBy { it.quoteId }
    return BackupV3(
        publications = publications.map { BackupPublication(it.id,it.name,it.sortOrder) },
        issues = issues.map { BackupIssue(it.id,it.year,publicationById[it.publicationId]?.name.orEmpty(),it.label,it.publicationId,it.officialKey,it.sourceUrl) },
        articles = articles.map { a -> BackupArticle(a.id,a.issueId,a.title,statusById[a.statusId]?.name ?: a.statusId,a.progress,a.statusId,a.officialKey,a.sourceUrl,a.summary,a.reflection,a.application,a.updatedAt) },
        quotes = quotes.map { q -> BackupQuote(q.id,q.issueId,q.articleId,q.sentence,q.note,tagRefs[q.id].orEmpty().mapNotNull { tagsById[it.tagId]?.name },q.location) },
        tags = tags.map { BackupTag(it.id,it.name,it.sortOrder) },
        statuses = statuses.map { BackupStatus(it.id,it.name,it.defaultProgress,it.adjustable,it.completed,it.showOnDashboard,it.sortOrder) },
        customFields = customFields.map { BackupCustomField(it.id,it.name,it.type,it.target,it.sortOrder,it.enabled) },
        customValues = customFieldValues.map { BackupCustomValue(it.id,it.fieldId,it.ownerId,it.value) },
        preferences = preferences.associate { it.key to it.value }
    )
}

fun BackupV3.toDataSnapshot(): DataSnapshot {
    val statusEntities = statuses.map { StatusDefinition(it.id,it.name,it.defaultProgress,it.adjustable,it.completed,it.showOnDashboard,it.sortOrder) }.ifEmpty { ProgressRules.defaultStatuses() }
    val statusIdByName = statusEntities.associate { it.name to it.id }
    val publicationEntities = if(publications.isNotEmpty()) publications.map { Publication(it.id,it.name,it.sortOrder) } else issues.map { it.publication }.distinct().mapIndexed { i,n -> Publication("import-pub-$i",n,i) }
    val pubIdByName = publicationEntities.associate { it.name to it.id }
    val issueEntities = issues.map { b -> Issue(b.id, b.publicationId.ifBlank { pubIdByName[b.publication] ?: publicationEntities.firstOrNull()?.id ?: "pub-import" }, b.year,b.label,b.officialKey,b.sourceUrl) }
    val articleEntities = articles.map { b -> Article(b.id,b.issueId,b.officialKey,b.title,b.sourceUrl,b.statusId.ifBlank { statusIdByName[b.status] ?: "status-unfinished" },b.progress.coerceIn(0,100),b.summary,b.reflection,b.application,updatedAt=b.updatedAt) }
    val tagEntities = tags.map { Tag(it.id,it.name,it.sortOrder) }.ifEmpty { quotes.flatMap { it.tags }.distinct().mapIndexed { i,n -> Tag("import-tag-$i",n,i) } }
    val tagIdByName = tagEntities.associate { it.name to it.id }
    val quoteEntities = quotes.map { Quote(it.id,it.issueId,it.articleId,it.sentence,it.location,it.note) }
    val refs = quotes.flatMap { q -> q.tags.mapNotNull { n -> tagIdByName[n]?.let { QuoteTagCrossRef(q.id,it) } } }
    return DataSnapshot(
        publications=publicationEntities, issues=issueEntities, articles=articleEntities, quotes=quoteEntities, tags=tagEntities, quoteTagRefs=refs,
        statuses=statusEntities, customFields=customFields.map { CustomFieldDefinition(it.id,it.name,it.type,it.target,it.sortOrder,it.enabled) },
        customFieldValues=customValues.map { CustomFieldValue(it.id,it.fieldId,it.ownerId,it.value) }, preferences=preferences.map { AppPreference(it.key,it.value) }
    )
}
