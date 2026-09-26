package com.beomsoo.sentencelibrary.export

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class DocxExportService(private val layoutEngine: A4LayoutEngine = A4LayoutEngine()) {
    fun create(article: ExportArticle): ByteArray = createMany(listOf(article))
    fun createMany(articles:List<ExportArticle>): ByteArray {
        val out=ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            fun put(name:String, content:String) { zip.putNextEntry(ZipEntry(name)); zip.write(content.toByteArray(Charsets.UTF_8)); zip.closeEntry() }
            put("[Content_Types].xml", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/></Types>""")
            put("_rels/.rels", """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/></Relationships>""")
            put("word/document.xml", documentXml(articles))
        }
        return out.toByteArray()
    }

    private fun documentXml(articles:List<ExportArticle>):String {
        val body=buildString {
            articles.forEachIndexed { articleIndex,article ->
                val pages=layoutEngine.layout(article)
                pages.forEachIndexed { pageIndex,page ->
                    page.lines.forEach { line -> append(paragraph(line.text,line.bold)) }
                    if(pageIndex < pages.lastIndex || articleIndex < articles.lastIndex) append("<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>")
                }
            }
            append("<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/><w:pgMar w:top=\"1134\" w:right=\"1134\" w:bottom=\"1134\" w:left=\"1134\" w:header=\"708\" w:footer=\"708\" w:gutter=\"0\"/></w:sectPr>")
        }
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:body>$body</w:body></w:document>"""
    }

    private fun paragraph(text:String,bold:Boolean):String {
        val p=if(bold) "<w:rPr><w:b/><w:rFonts w:ascii=\"Arial\" w:eastAsia=\"Malgun Gothic\"/></w:rPr>" else "<w:rPr><w:rFonts w:ascii=\"Arial\" w:eastAsia=\"Malgun Gothic\"/></w:rPr>"
        return "<w:p><w:r>$p<w:t xml:space=\"preserve\">${xml(text)}</w:t></w:r></w:p>"
    }
    private fun xml(v:String)=v.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;")
}
