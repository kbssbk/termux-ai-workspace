package com.beomsoo.sentencelibrary.settings

import com.beomsoo.sentencelibrary.data.*

class SettingsRepository(private val repository: ReadingRepository) {
    suspend fun statuses(): List<StatusDefinition> = repository.snapshot().statuses.sortedBy { it.sortOrder }
    suspend fun tags(): List<Tag> = repository.snapshot().tags.sortedBy { it.sortOrder }
    suspend fun upsertStatus(value: StatusDefinition) = repository.upsertStatus(value)
    suspend fun upsertTag(value: Tag) = repository.upsertTag(value)
    suspend fun renameStatus(id:String, name:String) {
        val found=statuses().firstOrNull { it.id==id } ?: return
        repository.upsertStatus(found.copy(name=name.trim().ifBlank { found.name }))
    }
    suspend fun renameTag(id:String, name:String) {
        val found=tags().firstOrNull { it.id==id } ?: return
        repository.upsertTag(found.copy(name=name.trim().ifBlank { found.name }))
    }
    suspend fun setPreference(key:String,value:String)=repository.setPreference(key,value)
}

class InMemorySettingsRepository {
    private val statusItems = com.beomsoo.sentencelibrary.domain.ProgressRules.defaultStatuses().toMutableList()
    private val tagItems = ReadingRepository.DEFAULT_TAGS.mapIndexed { i,n -> Tag("default-$i",n,i) }.toMutableList()
    fun statuses():List<StatusDefinition> = statusItems.sortedBy { it.sortOrder }
    fun tags():List<Tag> = tagItems.sortedBy { it.sortOrder }
    fun upsertStatus(value:StatusDefinition) { statusItems.removeAll { it.id==value.id }; statusItems += value }
    fun upsertTag(value:Tag) { tagItems.removeAll { it.id==value.id }; tagItems += value }
    fun renameStatus(id:String,name:String) { statusItems.indexOfFirst { it.id==id }.takeIf { it>=0 }?.let { i -> statusItems[i]=statusItems[i].copy(name=name) } }
    fun renameTag(id:String,name:String) { tagItems.indexOfFirst { it.id==id }.takeIf { it>=0 }?.let { i -> tagItems[i]=tagItems[i].copy(name=name) } }
}
