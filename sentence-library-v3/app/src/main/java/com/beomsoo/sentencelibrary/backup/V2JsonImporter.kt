package com.beomsoo.sentencelibrary.backup

import kotlinx.serialization.json.*

data class ImportReport(
    val issues: List<BackupIssue>,
    val articles: List<BackupArticle>,
    val quotes: List<BackupQuote>,
    val errors: List<String>
)

object V2JsonImporter {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(text: String): ImportReport {
        val root = json.parseToJsonElement(text).jsonObject
        val issues = mutableListOf<BackupIssue>()
        val articles = mutableListOf<BackupArticle>()
        val quotes = mutableListOf<BackupQuote>()
        val errors = mutableListOf<String>()

        root["issues"]?.jsonArray?.forEach { el ->
            val o = el.jsonObject
            val issueId = o.string("id")
            val year = o.int("year")
            val publication = o.string("publication")
            val label = o.string("label")
            if (issueId.isBlank() || year == null || publication.isBlank() || label.isBlank()) {
                errors += "issue:${issueId.ifBlank { "unknown" }} missing required fields"
                return@forEach
            }
            issues += BackupIssue(issueId, year, publication, label, officialKey=o.stringOrNull("officialCode"), sourceUrl=o.stringOrNull("sourceUrl"))
            o["articles"]?.jsonArray?.forEach { artEl ->
                val a = artEl.jsonObject
                val id = a.string("id")
                val title = a.string("title")
                if (id.isBlank() || title.isBlank()) {
                    errors += "article:${id.ifBlank { "unknown" }} missing title"
                    return@forEach
                }
                articles += BackupArticle(
                    id=id,
                    issueId=issueId,
                    title=title,
                    status=a.string("status").ifBlank { "미완" },
                    progress=a.int("progress")?.coerceIn(0,100) ?: 0,
                    officialKey=a.stringOrNull("officialKey"),
                    sourceUrl=a.stringOrNull("sourceUrl"),
                    summary=a.string("summary"),
                    reflection=a.string("reflection"),
                    application=a.string("application"),
                    updatedAt=a.long("updatedAt") ?: 0L
                )
            }
        }

        root["quotes"]?.jsonArray?.forEach { el ->
            val q = el.jsonObject
            val id=q.string("id"); val issueId=q.string("issueId"); val articleId=q.string("articleId"); val sentence=q.string("sentence")
            if(id.isBlank() || issueId.isBlank() || articleId.isBlank() || sentence.isBlank()) {
                errors += "quote:${id.ifBlank { "unknown" }} missing required fields"
                return@forEach
            }
            val tags = q["tags"]?.jsonArray?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }.orEmpty()
            quotes += BackupQuote(id,issueId,articleId,sentence,q.string("note"),tags,q.string("location"))
        }
        return ImportReport(issues,articles,quotes,errors)
    }

    fun toBackupV3(report: ImportReport): BackupV3 {
        val pubs = report.issues.map { it.publication }.distinct().mapIndexed { i,n -> BackupPublication("v2-pub-$i",n,i) }
        return BackupV3(
            publications=pubs,
            issues=report.issues.map { it.copy(publicationId=pubs.first { p -> p.name==it.publication }.id) },
            articles=report.articles,
            quotes=report.quotes
        )
    }

    private fun JsonObject.string(key:String) = (this[key] as? JsonPrimitive)?.contentOrNull.orEmpty()
    private fun JsonObject.stringOrNull(key:String) = (this[key] as? JsonPrimitive)?.contentOrNull
    private fun JsonObject.int(key:String) = (this[key] as? JsonPrimitive)?.intOrNull
    private fun JsonObject.long(key:String) = (this[key] as? JsonPrimitive)?.longOrNull
}
