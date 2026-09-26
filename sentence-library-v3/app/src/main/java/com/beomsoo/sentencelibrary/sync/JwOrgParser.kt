package com.beomsoo.sentencelibrary.sync

import org.jsoup.Jsoup

data class OfficialIssueCandidate(val year:Int,val month:Int,val day:Int?,val code:String)
data class ParsedOfficialIssue(val publicationName:String,val articles:List<OfficialArticle>)

object JwOrgParser {
    private val excluded=setOf("여호와의 증인","JW.ORG","다운로드 옵션","공유","목차","이전","다음","한국어")

    fun candidates(maxYear:Int=2026,minYear:Int=2001):List<OfficialIssueCandidate> = buildList {
        for(y in maxYear downTo minYear) for(m in 1..12) {
            val mm=m.toString().padStart(2,'0')
            when {
                y>=2016 -> add(OfficialIssueCandidate(y,m,null,"$y$mm"))
                y>=2008 -> add(OfficialIssueCandidate(y,m,15,"$y${mm}15"))
                else -> { add(OfficialIssueCandidate(y,m,1,"$y${mm}01")); add(OfficialIssueCandidate(y,m,15,"$y${mm}15")) }
            }
        }
    }

    fun issueLabel(c:OfficialIssueCandidate)= if(c.day==null) "${c.month}월호" else "${c.month}월 ${c.day}일호"
    fun issueUrl(c:OfficialIssueCandidate):String {
        val base="https://www.jw.org/ko/라이브러리/magazines/"
        return when {
            c.year>=2018 -> "${base}파수대-연구-${c.year}년-${c.month}월/"
            c.year>=2016 -> "${base}파수대-연구-${c.year}-${c.month}/"
            else -> "${base}w${c.code}/"
        }
    }

    fun parse(html:String, baseUrl:String):ParsedOfficialIssue {
        val doc=Jsoup.parse(html,baseUrl)
        val publication = doc.selectFirst("h1")?.text()?.trim().takeUnless { it.isNullOrBlank() }
            ?: doc.title().substringBefore("—").trim().ifBlank { "파수대" }
        val seen=mutableSetOf<String>()
        val articles=mutableListOf<OfficialArticle>()
        doc.select("main a:has(h2), a:has(h2)").forEach { a ->
            val title=a.selectFirst("h2")?.text()?.replace(Regex("\\s+")," ")?.trim().orEmpty()
            if(title.length !in 2..220 || title in excluded) return@forEach
            val norm=normalize(title); if(!seen.add(norm)) return@forEach
            val url=a.absUrl("href").ifBlank { baseUrl }
            articles += OfficialArticle(norm,title,url)
        }
        if(articles.isEmpty()) doc.select("main h2").forEach { h ->
            val title=h.text().replace(Regex("\\s+")," ").trim(); val norm=normalize(title)
            if(title.length in 2..220 && title !in excluded && seen.add(norm)) articles += OfficialArticle(norm,title,baseUrl)
        }
        return ParsedOfficialIssue(publication,articles)
    }

    fun normalize(value:String)=value.normalize().replace(Regex("[“”‘’'\"\\s]"),"").lowercase()
    private fun String.normalize()=java.text.Normalizer.normalize(this,java.text.Normalizer.Form.NFKC)
}
