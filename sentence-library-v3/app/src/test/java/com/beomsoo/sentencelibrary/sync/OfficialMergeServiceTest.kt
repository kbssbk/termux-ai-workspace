package com.beomsoo.sentencelibrary.sync

import org.junit.Assert.assertEquals
import org.junit.Test

class OfficialMergeServiceTest {
    @Test fun refreshing_official_metadata_preserves_personal_fields() {
        val existing = MergeArticle(
            id="a1", officialKey="k1", title="옛 제목", sourceUrl="https://www.jw.org/old",
            status="진행중", progress=67, summary="요약", reflection="느낌", application="적용",
            quotes=listOf("내 문장")
        )
        val merged = OfficialMergeService.mergeArticle(
            existing,
            OfficialArticle("k1", "새 공식 제목", "https://www.jw.org/new")
        )
        assertEquals("새 공식 제목", merged.title)
        assertEquals("https://www.jw.org/new", merged.sourceUrl)
        assertEquals("진행중", merged.status)
        assertEquals(67, merged.progress)
        assertEquals("요약", merged.summary)
        assertEquals("느낌", merged.reflection)
        assertEquals("적용", merged.application)
        assertEquals(listOf("내 문장"), merged.quotes)
    }
}
