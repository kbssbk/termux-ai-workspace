package com.beomsoo.sentencelibrary.recovery

import com.beomsoo.sentencelibrary.backup.BackupV3
import com.beomsoo.sentencelibrary.backup.BackupArticle
import com.beomsoo.sentencelibrary.backup.BackupQuote
import com.beomsoo.sentencelibrary.backup.BackupIssue
import org.junit.Assert.assertEquals
import org.junit.Test

class RecoveryServiceTest {
    @Test fun settings_only_restore_preserves_articles_and_quotes() {
        val store = InMemoryRecoveryStore(
            BackupV3(
                articles=listOf(BackupArticle("a","i","기사","진행중",55)),
                issues=listOf(BackupIssue("i",2026,"파수대—연구용","1월호")),
                quotes=listOf(BackupQuote("q","i","a","문장","", emptyList())),
                preferences=mapOf("density" to "compact")
            )
        )
        val service = RecoveryService(store)
        val snapshot = service.createSnapshot("before settings")
        store.current = store.current.copy(preferences=mapOf("density" to "comfortable"))
        service.restoreSnapshot(snapshot.id, RestoreScope.SETTINGS_ONLY)
        assertEquals(listOf("a"), store.current.articles.map { it.id })
        assertEquals(listOf("q"), store.current.quotes.map { it.id })
        assertEquals("compact", store.current.preferences["density"])
    }

    @Test fun retention_keeps_latest_ten_snapshots() {
        val store = InMemoryRecoveryStore(BackupV3())
        val service = RecoveryService(store)
        repeat(12) { service.createSnapshot("s$it") }
        assertEquals(10, store.snapshots.size)
    }
}
