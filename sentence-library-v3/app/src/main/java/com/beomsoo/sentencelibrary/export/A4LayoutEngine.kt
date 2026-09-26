package com.beomsoo.sentencelibrary.export

class A4LayoutEngine {
    fun layout(article: ExportArticle): List<ExportPage> {
        val pages = mutableListOf<MutableList<LayoutLine>>(mutableListOf())
        var y = 58f
        fun newPage() {
            pages.add(mutableListOf())
            y = 58f
            pages.last() += LayoutLine("${article.publication} · ${article.issue} · ${article.title}", y, true)
            y += 22f
        }
        fun add(text:String, bold:Boolean=false, gapBefore:Float=0f) {
            if(text.isBlank()) return
            y += gapBefore
            val lines = wrap(text, 72)
            for(line in lines) {
                if(y > 776f) newPage()
                pages.last() += LayoutLine(line, y, bold)
                y += 16f
            }
        }
        add(article.title, true)
        add("${article.publication} · ${article.issue}")
        add("상태 ${article.status} · 진행률 ${article.progress}%")
        add("기사 요약", true, 12f); add(article.summary)
        add("느낀점", true, 12f); add(article.reflection)
        add("적용점", true, 12f); add(article.application)
        if(article.quotes.isNotEmpty()) add("저장한 문장", true, 12f)
        article.quotes.forEachIndexed { index, q ->
            add("${index+1}. ${q.sentence}", false, 8f)
            if(q.location.isNotBlank()) add("위치: ${q.location}")
            if(q.tags.isNotEmpty()) add("태그: ${q.tags.joinToString(" · ") { "#$it" }}")
            if(q.note.isNotBlank()) add("메모: ${q.note}")
        }
        return pages.mapIndexed { index, lines -> ExportPage(index+1, lines) }
    }

    private fun wrap(text:String, maxChars:Int):List<String> {
        val normalized = text.replace("\r", "").split('\n')
        val result = mutableListOf<String>()
        normalized.forEach { paragraph ->
            var rest = paragraph.trim()
            if(rest.isEmpty()) { result += ""; return@forEach }
            while(rest.length > maxChars) {
                var cut = rest.lastIndexOf(' ', maxChars)
                if(cut < maxChars/2) cut = maxChars
                result += rest.substring(0, cut).trim()
                rest = rest.substring(cut).trim()
            }
            if(rest.isNotEmpty()) result += rest
        }
        return result
    }
}
