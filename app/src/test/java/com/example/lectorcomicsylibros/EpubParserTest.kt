package com.example.lectorcomicsylibros

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EpubParserTest {

    @Test
    fun testJsoupParagraphExtraction() {
        val html = """
            <!DOCTYPE html>
            <html>
            <head><title>Capítulo 1: El Comienzo</title></head>
            <body>
                <h1>Capítulo 1</h1>
                <p>Este es el primer párrafo de prueba.</p>
                <p>Este es el segundo párrafo de prueba con <strong>texto en negrita</strong>.</p>
                <blockquote>Una cita importante.</blockquote>
            </body>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val title = doc.select("h1, h2, title").firstOrNull()?.text()
        assertEquals("Capítulo 1: El Comienzo", title)

        val elements = doc.select("p, h1, h2, h3, blockquote")
        val paragraphs = elements.map { it.text().trim() }.filter { it.isNotBlank() }

        assertEquals(4, paragraphs.size)
        assertEquals("Capítulo 1", paragraphs[0])
        assertEquals("Este es el primer párrafo de prueba.", paragraphs[1])
        assertEquals("Este es el segundo párrafo de prueba con texto en negrita.", paragraphs[2])
        assertEquals("Una cita importante.", paragraphs[3])
    }
}
