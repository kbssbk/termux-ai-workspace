package com.beomsoo.sentencelibrary.backup

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class JsonBackupService {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }
    fun encode(backup: BackupV3): String = json.encodeToString(backup)
    fun decode(text: String): BackupV3 = json.decodeFromString(text)
}
