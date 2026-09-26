package com.beomsoo.sentencelibrary.export

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.ByteArrayOutputStream

class PdfExportService(private val layoutEngine:A4LayoutEngine=A4LayoutEngine()) {
    fun create(article:ExportArticle):ByteArray = createMany(listOf(article))
    fun createMany(articles:List<ExportArticle>):ByteArray {
        val document=PdfDocument()
        val normal=Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize=11f; typeface=Typeface.create("sans-serif",Typeface.NORMAL) }
        val bold=Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize=12f; typeface=Typeface.create("sans-serif",Typeface.BOLD) }
        var pageNo=1
        articles.forEach { article -> layoutEngine.layout(article).forEach { page ->
            val info=PdfDocument.PageInfo.Builder(595,842,pageNo).create()
            val pdfPage=document.startPage(info)
            page.lines.forEach { line -> pdfPage.canvas.drawText(line.text,50f,line.y,if(line.bold) bold else normal) }
            pdfPage.canvas.drawText("$pageNo", 545f, 820f, normal)
            document.finishPage(pdfPage); pageNo++
        }}
        val out=ByteArrayOutputStream(); document.writeTo(out); document.close(); return out.toByteArray()
    }
}
