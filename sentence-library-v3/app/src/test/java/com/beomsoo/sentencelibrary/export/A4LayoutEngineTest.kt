package com.beomsoo.sentencelibrary.export

import org.junit.Assert.assertTrue
import org.junit.Test

class A4LayoutEngineTest {
    @Test fun long_korean_content_paginates_without_overflow() {
        val longText = (1..180).joinToString(" ") { "아주 긴 한글 문장 $it 입니다." }
        val pages = A4LayoutEngine().layout(
            ExportArticle("파수대—연구용", "2026년 1월호", "긴 기사", "진행중", 50, longText, "느낀점", "적용점",
                quotes=listOf(ExportQuote(longText, "12면 3항", listOf("여호와"), "메모")))
        )
        assertTrue(pages.size > 1)
        assertTrue(pages.flatMap { it.lines }.all { it.text.length <= 80 })
        assertTrue(pages.all { p -> p.lines.all { it.y in 50f..792f } })
    }
}
