package com.beomsoo.sentencelibrary.recovery

import com.beomsoo.sentencelibrary.backup.*
import com.beomsoo.sentencelibrary.data.ReadingRepository
import com.beomsoo.sentencelibrary.data.RecoverySnapshot
import java.util.UUID

sealed interface RestoreScope {
    data object SETTINGS_ONLY : RestoreScope
    data object FULL : RestoreScope
}

data class SnapshotRecord(val id:String, val reason:String, val backup:BackupV3, val createdAt:Long=System.currentTimeMillis())

interface RecoveryStore {
    var current: BackupV3
    val snapshots: MutableList<SnapshotRecord>
}

class InMemoryRecoveryStore(override var current: BackupV3) : RecoveryStore {
    override val snapshots = mutableListOf<SnapshotRecord>()
}

class RecoveryService(private val store: RecoveryStore) {
    fun createSnapshot(reason:String): SnapshotRecord {
        val record = SnapshotRecord("snapshot-${UUID.randomUUID()}", reason, store.current)
        store.snapshots.add(0, record)
        while(store.snapshots.size > 10) store.snapshots.removeAt(store.snapshots.lastIndex)
        return record
    }

    fun restoreSnapshot(id:String, scope: RestoreScope) {
        val saved = store.snapshots.firstOrNull { it.id == id } ?: return
        store.current = when(scope) {
            RestoreScope.FULL -> saved.backup
            RestoreScope.SETTINGS_ONLY -> store.current.copy(
                tags=saved.backup.tags,
                statuses=saved.backup.statuses,
                customFields=saved.backup.customFields,
                customValues=saved.backup.customValues,
                preferences=saved.backup.preferences
            )
        }
    }

    fun integrityCheck(): List<String> {
        val issues = store.current.issues.map { it.id }.toSet()
        val articles = store.current.articles.map { it.id }.toSet()
        val warnings = mutableListOf<String>()
        store.current.articles.filter { it.issueId !in issues }.forEach { warnings += "orphan article:${it.id}" }
        store.current.quotes.filter { it.issueId !in issues || it.articleId !in articles }.forEach { warnings += "orphan quote:${it.id}" }
        store.current.articles.filter { it.progress !in 0..100 }.forEach { warnings += "invalid progress:${it.id}" }
        return warnings
    }
}

class PersistentRecoveryService(private val repository: ReadingRepository) {
    private val json = JsonBackupService()

    suspend fun createSnapshot(reason:String): RecoverySnapshot {
        val backup = repository.snapshot().toBackupV3()
        val record = RecoverySnapshot("snapshot-${UUID.randomUUID()}", reason, json.encode(backup))
        repository.saveSnapshot(record)
        return record
    }

    suspend fun restoreSnapshot(id:String, settingsOnly:Boolean) {
        val record = repository.snapshotById(id) ?: return
        val backup = json.decode(record.json)
        repository.replace(backup.toDataSnapshot(), settingsOnly)
    }

    suspend fun integrityCheck(): List<String> {
        val snap = repository.snapshot()
        val issueIds=snap.issues.map { it.id }.toSet(); val articleIds=snap.articles.map { it.id }.toSet()
        return buildList {
            snap.articles.filter { it.issueId !in issueIds }.forEach { add("고아 기사: ${it.title}") }
            snap.quotes.filter { it.issueId !in issueIds || it.articleId !in articleIds }.forEach { add("고아 문장: ${it.id}") }
            snap.articles.filter { it.progress !in 0..100 }.forEach { add("잘못된 진행률: ${it.title}") }
        }
    }
}
