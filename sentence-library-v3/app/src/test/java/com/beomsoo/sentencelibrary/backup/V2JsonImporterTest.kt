package com.beomsoo.sentencelibrary.backup

import org.junit.Assert.*
import org.junit.Test

class V2JsonImporterTest {
    @Test fun imports_valid_records_ignores_unknown_fields_and_reports_bad_records() {
        val json = """
          {"version":1,"unknownRoot":"ok","issues":[
            {"id":"i1","year":2026,"publication":"파수대—연구용","label":"1월호","articles":[
              {"id":"a1","title":"좋은 기사","status":"진행중","progress":42,"unknown":"x"},
              {"id":"a2","status":"미완","progress":0}
            ]}
          ],"quotes":[{"id":"q1","issueId":"i1","articleId":"a1","sentence":"기억할 문장","tags":["여호와"],"note":"메모"}]}
        """.trimIndent()
        val report = V2JsonImporter.parse(json)
        assertEquals(1, report.issues.size)
        assertEquals(1, report.articles.size)
        assertEquals(42, report.articles.single().progress)
        assertEquals("진행중", report.articles.single().status)
        assertEquals(1, report.quotes.size)
        assertEquals(listOf("여호와"), report.quotes.single().tags)
        assertTrue(report.errors.any { it.contains("a2") })
    }

    @Test fun missing_optional_note_becomes_empty() {
        val json = """{"issues":[{"id":"i","year":2025,"publication":"깨어라","label":"1호","articles":[{"id":"a","title":"제목","status":"미완","progress":0}]}],"quotes":[{"id":"q","issueId":"i","articleId":"a","sentence":"문장"}]}"""
        assertEquals("", V2JsonImporter.parse(json).quotes.single().note)
    }
}
