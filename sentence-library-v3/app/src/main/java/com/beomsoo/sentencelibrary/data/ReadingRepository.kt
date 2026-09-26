package com.beomsoo.sentencelibrary.data

import androidx.room.withTransaction
import com.beomsoo.sentencelibrary.domain.ProgressRules
import java.util.UUID
import kotlinx.coroutines.flow.Flow

class ReadingRepository(private val db: AppDatabase) {
    private val dao = db.readingDao()
    val publications: Flow<List<Publication>> = dao.observePublications(); val issues: Flow<List<Issue>> = dao.observeIssues(); val articles: Flow<List<Article>> = dao.observeArticles(); val quotes: Flow<List<Quote>> = dao.observeQuotes(); val tags: Flow<List<Tag>> = dao.observeTags(); val statuses: Flow<List<StatusDefinition>> = dao.observeStatuses(); val preferences: Flow<List<AppPreference>> = dao.observePreferences(); val snapshots: Flow<List<RecoverySnapshot>> = dao.observeSnapshots(); val quoteTagRefs: Flow<List<QuoteTagCrossRef>> = dao.observeQuoteTagRefs(); val articleSummaries: Flow<List<ArticleSummary>> = dao.observeArticleSummaries()

    suspend fun bootstrapDefaults() { db.withTransaction {
        if(dao.allStatuses().isEmpty()) dao.upsertStatuses(ProgressRules.defaultStatuses())
        if(dao.allTags().isEmpty()) dao.upsertTags(DEFAULT_TAGS.mapIndexed{i,n->Tag("tag-default-$i",n,i)})
        if(dao.allPublications().isEmpty()) dao.upsertPublications(listOf(Publication("pub-watchtower-study","파수대—연구용",0),Publication("pub-watchtower","파수대",1),Publication("pub-awake","깨어라",2)))
    }}

    suspend fun addIssue(publicationName:String,year:Int,label:String):Issue { val p=dao.publicationByName(publicationName)?:Publication("pub-${UUID.randomUUID()}",publicationName,dao.allPublications().size).also{dao.upsertPublication(it)};return Issue("issue-${UUID.randomUUID()}",p.id,year,label).also{dao.upsertIssue(it)} }
    suspend fun addArticle(issueId:String,title:String):Article = Article("article-${UUID.randomUUID()}",issueId=issueId,title=title,sortOrder=dao.allArticles().count{it.issueId==issueId}).also{dao.upsertArticle(it)}
    suspend fun setArticleStatus(articleId:String,statusId:String){val a=dao.articleById(articleId)?:return;val s=dao.statusById(statusId)?:return;dao.upsertArticle(a.copy(statusId=s.id,progress=ProgressRules.normalize(s,s.defaultProgress),updatedAt=System.currentTimeMillis()))}
    suspend fun setArticleProgress(articleId:String,requested:Int){val a=dao.articleById(articleId)?:return;val s=dao.statusById(a.statusId);val progressStatus=if(s?.adjustable==true)s else dao.statusById("status-progress")?:ProgressRules.defaultStatuses()[1];dao.upsertArticle(a.copy(statusId=progressStatus.id,progress=ProgressRules.normalize(progressStatus,requested),updatedAt=System.currentTimeMillis()))}
    suspend fun setArticleNotes(articleId:String,summary:String,reflection:String,application:String){val a=dao.articleById(articleId)?:return;dao.upsertArticle(a.copy(summary=summary,reflection=reflection,application=application,updatedAt=System.currentTimeMillis()))}
    suspend fun addQuote(articleId:String,sentence:String,location:String="",note:String="",tagIds:List<String> = emptyList()):Quote? {val a=dao.articleById(articleId)?:return null;val q=Quote("quote-${UUID.randomUUID()}",a.issueId,a.id,sentence,location,note);db.withTransaction{dao.upsertQuote(q);if(tagIds.isNotEmpty())dao.upsertQuoteTagRefs(tagIds.distinct().map{QuoteTagCrossRef(q.id,it)})};return q}
    suspend fun upsertStatus(status:StatusDefinition)=dao.upsertStatus(status); suspend fun upsertTag(tag:Tag)=dao.upsertTag(tag); suspend fun setPreference(key:String,value:String)=dao.upsertPreference(AppPreference(key,value)); suspend fun saveSnapshot(snapshot:RecoverySnapshot)=dao.upsertSnapshot(snapshot); suspend fun snapshotById(id:String)=dao.snapshotById(id); suspend fun deleteSnapshot(id:String)=dao.deleteSnapshot(id); suspend fun syncMetadata(item:SyncMetadata)=dao.upsertSyncMetadata(item); suspend fun issueByOfficialKey(key:String)=dao.issueByOfficialKey(key); suspend fun articleByOfficialKey(key:String)=dao.articleByOfficialKey(key); suspend fun upsertIssue(issue:Issue)=dao.upsertIssue(issue); suspend fun upsertArticle(article:Article)=dao.upsertArticle(article)

    suspend fun snapshot():DataSnapshot=DataSnapshot(dao.allPublications(),dao.allIssues(),dao.allArticles(),dao.allQuotes(),dao.allTags(),dao.allQuoteTagRefs(),dao.allStatuses(),dao.allCustomFields(),dao.allCustomFieldValues(),dao.allPreferences(),dao.allSyncMetadata())

    suspend fun replace(snapshot:DataSnapshot,settingsOnly:Boolean=false){db.withTransaction{
        if(settingsOnly){
            snapshot.statuses.forEach{dao.upsertStatus(it)};snapshot.tags.forEach{dao.upsertTag(it)};snapshot.customFields.forEach{dao.upsertCustomFields(listOf(it))};dao.clearPreferences();dao.upsertPreferences(snapshot.preferences)
        } else {
            dao.clearQuoteTagRefs();dao.clearCustomFieldValues();dao.clearQuotes();dao.clearArticles();dao.clearIssues();dao.clearPublications();dao.clearSyncMetadata();dao.clearCustomFields();dao.clearTags();dao.clearStatuses();dao.clearPreferences()
            dao.upsertStatuses(snapshot.statuses.ifEmpty{ProgressRules.defaultStatuses()});dao.upsertTags(snapshot.tags.ifEmpty{DEFAULT_TAGS.mapIndexed{i,n->Tag("tag-default-$i",n,i)}});dao.upsertPublications(snapshot.publications);dao.upsertIssues(snapshot.issues);dao.upsertArticles(snapshot.articles);dao.upsertQuotes(snapshot.quotes);dao.upsertQuoteTagRefs(snapshot.quoteTagRefs);dao.upsertCustomFields(snapshot.customFields);dao.upsertCustomFieldValues(snapshot.customFieldValues);dao.upsertPreferences(snapshot.preferences);snapshot.syncMetadata.forEach{dao.upsertSyncMetadata(it)}
        }
    }}
    companion object{val DEFAULT_TAGS=listOf("여호와","예수","성령","관계","도움","발전")}
}

data class DataSnapshot(val publications:List<Publication> = emptyList(),val issues:List<Issue> = emptyList(),val articles:List<Article> = emptyList(),val quotes:List<Quote> = emptyList(),val tags:List<Tag> = emptyList(),val quoteTagRefs:List<QuoteTagCrossRef> = emptyList(),val statuses:List<StatusDefinition> = emptyList(),val customFields:List<CustomFieldDefinition> = emptyList(),val customFieldValues:List<CustomFieldValue> = emptyList(),val preferences:List<AppPreference> = emptyList(),val syncMetadata:List<SyncMetadata> = emptyList())
