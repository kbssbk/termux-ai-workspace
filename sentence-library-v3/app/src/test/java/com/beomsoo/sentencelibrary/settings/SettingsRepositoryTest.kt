package com.beomsoo.sentencelibrary.settings

import com.beomsoo.sentencelibrary.data.StatusDefinition
import com.beomsoo.sentencelibrary.data.Tag
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsRepositoryTest {
    @Test fun status_and_tag_edits_preserve_stable_ids() {
        val repo = InMemorySettingsRepository()
        val status = StatusDefinition(id="custom", name="보류", defaultProgress=10, adjustable=true, completed=false, showOnDashboard=true, sortOrder=3)
        repo.upsertStatus(status)
        repo.renameStatus("custom", "잠시 보류")
        val tag = Tag(id="tag-x", name="중요", sortOrder=9)
        repo.upsertTag(tag)
        repo.renameTag("tag-x", "핵심")
        assertEquals("custom", repo.statuses().single { it.name == "잠시 보류" }.id)
        assertEquals("tag-x", repo.tags().single { it.name == "핵심" }.id)
    }
}
