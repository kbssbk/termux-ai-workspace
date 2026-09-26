package com.beomsoo.sentencelibrary.sync

data class OfficialArticle(val officialKey:String, val title:String, val sourceUrl:String)
data class MergeArticle(
    val id:String,
    val officialKey:String,
    val title:String,
    val sourceUrl:String,
    val status:String,
    val progress:Int,
    val summary:String,
    val reflection:String,
    val application:String,
    val quotes:List<String>
)

object OfficialMergeService {
    fun mergeArticle(existing:MergeArticle, official:OfficialArticle):MergeArticle = existing.copy(
        officialKey=official.officialKey,
        title=official.title,
        sourceUrl=official.sourceUrl
    )
}
