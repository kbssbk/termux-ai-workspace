package com.beomsoo.sentencelibrary.export

import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream
import org.junit.Assert.assertTrue
import org.junit.Test

class DocxExportServiceTest {
    @Test fun docx_contains_required_ooxml_parts_and_a4_section() {
        val bytes = DocxExportService().create(
            ExportArticle("깨어라", "2026년 1호", "기사", "완성", 100, "요약", "느낀점", "적용점", emptyList())
        )
        val names = mutableSetOf<String>()
        var documentXml = ""
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var e = zip.nextEntry
            while (e != null) {
                names += e.name
                if (e.name == "word/document.xml") documentXml = zip.readBytes().toString(Charsets.UTF_8)
                e = zip.nextEntry
            }
        }
        assertTrue("[Content_Types].xml" in names)
        assertTrue("_rels/.rels" in names)
        assertTrue("word/document.xml" in names)
        assertTrue(documentXml.contains("w:pgSz"))
        assertTrue(documentXml.contains("w:w=\"11906\""))
        assertTrue(documentXml.contains("w:h=\"16838\""))
    }
}
