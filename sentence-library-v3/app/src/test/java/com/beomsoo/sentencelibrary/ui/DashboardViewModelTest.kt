package com.beomsoo.sentencelibrary.ui

import com.beomsoo.sentencelibrary.data.ArticleSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardViewModelTest {
    @Test fun newest_in_progress_article_is_resume_item_and_years_descend() {
        val items = listOf(
            ArticleSummary("a1", 2025, "1월호", "A", "진행중", 25, 10L),
            ArticleSummary("a2", 2026, "2월호", "B", "진행중", 70, 20L),
            ArticleSummary("a3", 2026, "2월호", "C", "완성", 100, 30L)
        )
        val vm = DashboardViewModel(items)
        assertEquals("a2", vm.resumeItem!!.id)
        assertEquals(listOf(2026, 2025), vm.yearRows.map { it.year })
        assertEquals(65, vm.overallProgress)
        assertEquals(1, vm.completedCount)
        assertEquals(2, vm.inProgressCount)
    }
}
