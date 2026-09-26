package com.beomsoo.sentencelibrary.sync

import org.junit.Assert.assertEquals
import org.junit.Test

class SyncQueuePolicyTest {
    @Test fun failed_only_retry_uses_only_failed_official_keys() {
        val candidates = listOf(
            OfficialIssueCandidate(2026,1,null,"202601"),
            OfficialIssueCandidate(2026,2,null,"202602"),
            OfficialIssueCandidate(2026,3,null,"202603")
        )
        val selected = SyncQueuePolicy.failedOnly(candidates, setOf("202602","202603"))
        assertEquals(listOf("202602","202603"), selected.map { it.code })
    }
}
