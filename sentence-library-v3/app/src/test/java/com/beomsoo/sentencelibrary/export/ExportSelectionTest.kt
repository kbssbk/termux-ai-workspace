package com.beomsoo.sentencelibrary.export

import com.beomsoo.sentencelibrary.data.Article
import org.junit.Assert.assertEquals
import org.junit.Test

class ExportSelectionTest {
    private val articles = listOf(
        Article("a1","i1",title="A"),
        Article("a2","i1",title="B"),
        Article("a3","i2",title="C")
    )

    @Test fun article_issue_and_all_scopes_select_expected_records() {
        assertEquals(listOf("a1"), ExportSelection.select(articles, ExportScope.Article("a1")).map { it.id })
        assertEquals(listOf("a1","a2"), ExportSelection.select(articles, ExportScope.Issue("i1")).map { it.id })
        assertEquals(listOf("a1","a2","a3"), ExportSelection.select(articles, ExportScope.All).map { it.id })
    }
}
