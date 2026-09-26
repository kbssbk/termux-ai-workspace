package com.beomsoo.sentencelibrary.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertPublications(items: List<Publication>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertPublication(item: Publication)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertIssues(items: List<Issue>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertIssue(item: Issue)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertArticles(items: List<Article>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertArticle(item: Article)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertQuotes(items: List<Quote>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertQuote(item: Quote)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertTags(items: List<Tag>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertTag(item: Tag)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertQuoteTagRefs(items: List<QuoteTagCrossRef>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertStatuses(items: List<StatusDefinition>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertStatus(item: StatusDefinition)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertCustomFields(items: List<CustomFieldDefinition>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertCustomFieldValues(items: List<CustomFieldValue>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertPreference(item: AppPreference)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertPreferences(items: List<AppPreference>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertSnapshot(item: RecoverySnapshot)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertSyncMetadata(item: SyncMetadata)

    @Query("SELECT * FROM publications ORDER BY sortOrder, name") fun observePublications(): Flow<List<Publication>>
    @Query("SELECT * FROM issues ORDER BY year DESC, sortOrder, label") fun observeIssues(): Flow<List<Issue>>
    @Query("SELECT * FROM articles ORDER BY updatedAt DESC") fun observeArticles(): Flow<List<Article>>
    @Query("SELECT * FROM quotes ORDER BY updatedAt DESC") fun observeQuotes(): Flow<List<Quote>>
    @Query("SELECT * FROM tags ORDER BY sortOrder, name") fun observeTags(): Flow<List<Tag>>
    @Query("SELECT * FROM status_definitions ORDER BY sortOrder, name") fun observeStatuses(): Flow<List<StatusDefinition>>
    @Query("SELECT * FROM app_preferences ORDER BY key") fun observePreferences(): Flow<List<AppPreference>>
    @Query("SELECT * FROM recovery_snapshots ORDER BY createdAt DESC") fun observeSnapshots(): Flow<List<RecoverySnapshot>>
    @Query("SELECT * FROM quote_tag_cross_ref") fun observeQuoteTagRefs(): Flow<List<QuoteTagCrossRef>>

    @Query("SELECT a.id AS id, i.year AS year, i.label AS issueLabel, a.title AS title, s.name AS status, a.progress AS progress, a.updatedAt AS updatedAt, p.name AS publicationName, i.id AS issueId FROM articles a JOIN issues i ON i.id=a.issueId JOIN publications p ON p.id=i.publicationId JOIN status_definitions s ON s.id=a.statusId ORDER BY a.updatedAt DESC")
    fun observeArticleSummaries(): Flow<List<ArticleSummary>>

    @Query("SELECT * FROM publications ORDER BY sortOrder, name") suspend fun allPublications(): List<Publication>
    @Query("SELECT * FROM issues ORDER BY year DESC, sortOrder, label") suspend fun allIssues(): List<Issue>
    @Query("SELECT * FROM articles ORDER BY sortOrder, title") suspend fun allArticles(): List<Article>
    @Query("SELECT * FROM quotes ORDER BY createdAt") suspend fun allQuotes(): List<Quote>
    @Query("SELECT * FROM tags ORDER BY sortOrder, name") suspend fun allTags(): List<Tag>
    @Query("SELECT * FROM quote_tag_cross_ref") suspend fun allQuoteTagRefs(): List<QuoteTagCrossRef>
    @Query("SELECT * FROM status_definitions ORDER BY sortOrder, name") suspend fun allStatuses(): List<StatusDefinition>
    @Query("SELECT * FROM custom_field_definitions ORDER BY sortOrder, name") suspend fun allCustomFields(): List<CustomFieldDefinition>
    @Query("SELECT * FROM custom_field_values") suspend fun allCustomFieldValues(): List<CustomFieldValue>
    @Query("SELECT * FROM app_preferences") suspend fun allPreferences(): List<AppPreference>
    @Query("SELECT * FROM recovery_snapshots ORDER BY createdAt DESC") suspend fun allSnapshots(): List<RecoverySnapshot>
    @Query("SELECT * FROM sync_metadata") suspend fun allSyncMetadata(): List<SyncMetadata>

    @Query("SELECT * FROM publications WHERE name=:name LIMIT 1") suspend fun publicationByName(name: String): Publication?
    @Query("SELECT * FROM issues WHERE id=:id LIMIT 1") suspend fun issueById(id: String): Issue?
    @Query("SELECT * FROM issues WHERE officialKey=:key LIMIT 1") suspend fun issueByOfficialKey(key: String): Issue?
    @Query("SELECT * FROM articles WHERE id=:id LIMIT 1") suspend fun articleById(id: String): Article?
    @Query("SELECT * FROM articles WHERE officialKey=:key LIMIT 1") suspend fun articleByOfficialKey(key: String): Article?
    @Query("SELECT * FROM status_definitions WHERE id=:id LIMIT 1") suspend fun statusById(id: String): StatusDefinition?
    @Query("SELECT * FROM recovery_snapshots WHERE id=:id LIMIT 1") suspend fun snapshotById(id: String): RecoverySnapshot?

    @Query("DELETE FROM quote_tag_cross_ref") suspend fun clearQuoteTagRefs()
    @Query("DELETE FROM custom_field_values") suspend fun clearCustomFieldValues()
    @Query("DELETE FROM quotes") suspend fun clearQuotes()
    @Query("DELETE FROM articles") suspend fun clearArticles()
    @Query("DELETE FROM issues") suspend fun clearIssues()
    @Query("DELETE FROM publications") suspend fun clearPublications()
    @Query("DELETE FROM tags") suspend fun clearTags()
    @Query("DELETE FROM custom_field_definitions") suspend fun clearCustomFields()
    @Query("DELETE FROM status_definitions") suspend fun clearStatuses()
    @Query("DELETE FROM app_preferences") suspend fun clearPreferences()
    @Query("DELETE FROM sync_metadata") suspend fun clearSyncMetadata()
    @Query("DELETE FROM recovery_snapshots WHERE id=:id") suspend fun deleteSnapshot(id: String)
    @Query("SELECT COUNT(*) FROM recovery_snapshots") suspend fun snapshotCount(): Int
}
