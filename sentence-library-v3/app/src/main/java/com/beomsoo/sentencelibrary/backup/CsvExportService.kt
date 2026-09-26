package com.beomsoo.sentencelibrary.backup

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class CsvExportService {
    fun exportZip(backup: BackupV3): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            fun add(name:String, text:String) {
                zip.putNextEntry(ZipEntry(name)); zip.write(text.toByteArray(Charsets.UTF_8)); zip.closeEntry()
            }
            add("publications.csv", csv(listOf("id","name","sort_order"), backup.publications.map { listOf(it.id,it.name,it.sortOrder.toString()) }))
            add("issues.csv", csv(listOf("id","publication_id","year","label","official_key","source_url"), backup.issues.map { listOf(it.id,it.publicationId,it.year.toString(),it.label,it.officialKey.orEmpty(),it.sourceUrl.orEmpty()) }))
            add("articles.csv", csv(listOf("id","issue_id","title","status","progress","summary","reflection","application"), backup.articles.map { listOf(it.id,it.issueId,it.title,it.status,it.progress.toString(),it.summary,it.reflection,it.application) }))
            add("quotes.csv", csv(listOf("id","issue_id","article_id","sentence","location","note"), backup.quotes.map { listOf(it.id,it.issueId,it.articleId,it.sentence,it.location,it.note) }))
            add("tags.csv", csv(listOf("id","name","sort_order"), backup.tags.map { listOf(it.id,it.name,it.sortOrder.toString()) }))
            val quoteTags = backup.quotes.flatMap { q -> q.tags.map { listOf(q.id,it) } }
            add("quote_tags.csv", csv(listOf("quote_id","tag_name"), quoteTags))
            add("custom_fields.csv", csv(listOf("id","name","type","target","enabled"), backup.customFields.map { listOf(it.id,it.name,it.type,it.target,it.enabled.toString()) }))
        }
        return out.toByteArray()
    }

    private fun csv(header:List<String>, rows:List<List<String>>):String = buildString {
        append(header.joinToString(",") { esc(it) }).append('\n')
        rows.forEach { append(it.joinToString(",") { v -> esc(v) }).append('\n') }
    }
    private fun esc(value:String):String = "\"${value.replace("\"","\"\"")}\""
}
