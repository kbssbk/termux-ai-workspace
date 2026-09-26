package com.beomsoo.sentencelibrary.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressRulesTest {
    @Test fun default_statuses_map_to_expected_progress() {
        val defaults = ProgressRules.defaultStatuses().associateBy { it.name }
        assertEquals(0, defaults["미완"]!!.defaultProgress)
        assertEquals(25, defaults["진행중"]!!.defaultProgress)
        assertEquals(100, defaults["완성"]!!.defaultProgress)
    }

    @Test fun adjustable_status_clamps_to_one_through_ninety_nine() {
        val status = ProgressRules.defaultStatuses().first { it.name == "진행중" }
        assertEquals(1, ProgressRules.normalize(status, 0))
        assertEquals(99, ProgressRules.normalize(status, 100))
        assertEquals(54, ProgressRules.normalize(status, 54))
    }

    @Test fun averages_article_progress() {
        assertEquals(50, ProgressRules.average(listOf(0, 50, 100)))
        assertEquals(0, ProgressRules.average(emptyList()))
    }
}
