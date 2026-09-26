package com.beomsoo.sentencelibrary.export

data class ExportQuote(val sentence:String, val location:String="", val tags:List<String> = emptyList(), val note:String="")
data class ExportArticle(
    val publication:String,
    val issue:String,
    val title:String,
    val status:String,
    val progress:Int,
    val summary:String="",
    val reflection:String="",
    val application:String="",
    val quotes:List<ExportQuote> = emptyList()
)
data class LayoutLine(val text:String, val y:Float, val bold:Boolean=false)
data class ExportPage(val number:Int, val lines:List<LayoutLine>)
