package com.example.lectorcomicsylibros.reader.epub

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream

data class TocItem(
    val title: String,
    val chapterIndex: Int
)

data class EpubChapter(
    val index: Int,
    val id: String,
    val title: String,
    val contentHtml: String,
    val paragraphs: List<String>
)

data class EpubData(
    val title: String,
    val author: String,
    val chapters: List<EpubChapter>,
    val toc: List<TocItem>
)

object EpubParser {

    suspend fun parseEpub(context: Context, fileUriString: String): EpubData = withContext(Dispatchers.IO) {
        val uri = Uri.parse(fileUriString)
        val entries = mutableMapOf<String, ByteArray>()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            ZipInputStream(inputStream).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        entries[entry.name.lowercase()] = zip.readBytes()
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }

        // 1. Find OPF file path from META-INF/container.xml
        val containerBytes = entries["meta-inf/container.xml"]
            ?: throw IllegalArgumentException("Archivo EPUB no válido: falta META-INF/container.xml")
        val containerXml = String(containerBytes, Charsets.UTF_8)
        val opfPath = extractAttribute(containerXml, "full-path")
            ?: throw IllegalArgumentException("Archivo EPUB no válido: no se encontró full-path en container.xml")

        val opfPathLower = opfPath.lowercase()
        val opfBytes = entries[opfPathLower]
            ?: throw IllegalArgumentException("No se encontró el archivo OPF en $opfPath")
        val opfXml = String(opfBytes, Charsets.UTF_8)
        val opfDir = if (opfPathLower.contains('/')) opfPathLower.substringBeforeLast('/') else ""

        // 2. Parse OPF metadata
        val title = extractTagContent(opfXml, "dc:title") ?: "Libro sin título"
        val author = extractTagContent(opfXml, "dc:creator") ?: "Autor desconocido"

        // 3. Parse Manifest (item id -> href)
        val manifestMap = mutableMapOf<String, String>()
        var ncxPath: String? = null

        val manifestRegex = Regex("<item\\s+[^>]*>", RegexOption.IGNORE_CASE)
        manifestRegex.findAll(opfXml).forEach { match ->
            val tag = match.value
            val id = extractAttribute(tag, "id")
            val href = extractAttribute(tag, "href")
            val mediaType = extractAttribute(tag, "media-type")

            if (id != null && href != null) {
                val fullHref = resolvePath(opfDir, href)
                manifestMap[id] = fullHref

                if (mediaType == "application/x-dtbncx+xml" || id.lowercase().contains("ncx")) {
                    ncxPath = fullHref
                }
            }
        }

        // 4. Parse Spine (ordered itemrefs)
        val spineIdRefs = mutableListOf<String>()
        val itemrefRegex = Regex("<itemref\\s+[^>]*>", RegexOption.IGNORE_CASE)
        itemrefRegex.findAll(opfXml).forEach { match ->
            val idref = extractAttribute(match.value, "idref")
            if (idref != null) {
                spineIdRefs.add(idref)
            }
        }

        // 5. Parse TOC (NCX if available)
        val tocList = mutableListOf<TocItem>()
        if (ncxPath != null && entries.containsKey(ncxPath!!.lowercase())) {
            val ncxXml = String(entries[ncxPath!!.lowercase()]!!, Charsets.UTF_8)
            val navPointRegex = Regex("<navPoint[^>]*>[\\s\\S]*?</navPoint>", RegexOption.IGNORE_CASE)
            navPointRegex.findAll(ncxXml).forEach { match ->
                val navChunk = match.value
                val navTitle = extractTagContent(navChunk, "text") ?: ""
                val navSrc = extractAttribute(navChunk, "src") ?: ""
                if (navTitle.isNotBlank() && navSrc.isNotBlank()) {
                    val fullNavSrc = resolvePath(opfDir, navSrc.substringBefore('#'))
                    val chapterIndex = spineIdRefs.indexOfFirst { id ->
                        manifestMap[id]?.lowercase() == fullNavSrc.lowercase()
                    }
                    if (chapterIndex >= 0) {
                        tocList.add(TocItem(navTitle, chapterIndex))
                    }
                }
            }
        }

        // 6. Build Chapters
        val chapters = mutableListOf<EpubChapter>()
        spineIdRefs.forEachIndexed { index, idref ->
            val href = manifestMap[idref] ?: return@forEachIndexed
            val chapterBytes = entries[href.lowercase()] ?: return@forEachIndexed
            val htmlContent = String(chapterBytes, Charsets.UTF_8)

            val doc = Jsoup.parse(htmlContent)

            // Extract Chapter Title
            var chapterTitle = tocList.find { it.chapterIndex == index }?.title
            if (chapterTitle.isNullOrBlank()) {
                chapterTitle = doc.select("h1, h2, h3, title").firstOrNull()?.text()
            }
            if (chapterTitle.isNullOrBlank()) {
                chapterTitle = "Capítulo ${index + 1}"
            }

            // Extract Paragraphs
            val pElements = doc.select("p, h1, h2, h3, h4, h5, h6, blockquote, li")
            val paragraphs = if (pElements.isNotEmpty()) {
                pElements.map { it.text().trim() }.filter { it.isNotBlank() }
            } else {
                listOf(doc.text().trim()).filter { it.isNotBlank() }
            }

            chapters.add(
                EpubChapter(
                    index = index,
                    id = idref,
                    title = chapterTitle,
                    contentHtml = htmlContent,
                    paragraphs = paragraphs
                )
            )
        }

        // If TOC was empty, build TOC from chapters
        if (tocList.isEmpty()) {
            chapters.forEachIndexed { index, chapter ->
                tocList.add(TocItem(chapter.title, index))
            }
        }

        EpubData(
            title = title,
            author = author,
            chapters = chapters,
            toc = tocList
        )
    }

    private fun resolvePath(dir: String, relative: String): String {
        val cleanRel = relative.replace('\\', '/').trimStart('/')
        if (dir.isBlank()) return cleanRel
        return "$dir/$cleanRel"
    }

    private fun extractAttribute(tag: String, attrName: String): String? {
        val regex = Regex("$attrName\\s*=\\s*[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE)
        return regex.find(tag)?.groupValues?.get(1)
    }

    private fun extractTagContent(xml: String, tagName: String): String? {
        val regex = Regex("<$tagName[^>]*>([\\s\\S]*?)</$tagName>", RegexOption.IGNORE_CASE)
        val match = regex.find(xml)?.groupValues?.get(1)?.trim()
        if (match == null) return null
        // Strip inner XML/HTML tags if present
        return match.replace(Regex("<[^>]*>"), "").trim()
    }
}
