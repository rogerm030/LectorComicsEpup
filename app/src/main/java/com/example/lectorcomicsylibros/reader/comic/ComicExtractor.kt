package com.example.lectorcomicsylibros.reader.comic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.github.junrar.Junrar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.math.BigInteger
import java.util.zip.ZipInputStream

object ComicExtractor {

    private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "bmp")

    private fun isInvalidEntryName(name: String): Boolean {
        val lower = name.lowercase()
        if (lower.contains("__macosx")) return true
        val fileName = lower.substringAfterLast('/')
        if (fileName.startsWith(".")) return true
        if (fileName.contains("metadata") || fileName == "thumbs.db" || fileName == ".ds_store") return true
        return false
    }

    suspend fun extractComicPages(
        context: Context,
        fileUriString: String,
        format: String,
    ): List<File> = withContext(Dispatchers.IO) {
        val uri = Uri.parse(fileUriString)
        val cacheDir = File(context.cacheDir, "comic_pages_${fileUriString.hashCode()}")
        if (cacheDir.exists()) {
            cacheDir.deleteRecursively()
        }
        cacheDir.mkdirs()

        when (format.uppercase()) {
            "CBZ" -> extractCbz(context, uri, cacheDir)
            "CBR" -> extractCbr(context, uri, cacheDir)
            "PDF" -> extractPdf(context, uri, cacheDir)
            else -> extractCbz(context, uri, cacheDir)
        }
    }

    suspend fun extractComicCoverAndPageCount(
        context: Context,
        uri: Uri,
        format: String,
        targetCoverFile: File
    ): Pair<String?, Int> = withContext(Dispatchers.IO) {
        when (format.uppercase()) {
            "CBZ" -> extractCbzCoverAndPageCount(context, uri, targetCoverFile)
            "CBR" -> extractCbrCoverAndPageCount(context, uri, targetCoverFile)
            "PDF" -> extractPdfCoverAndPageCount(context, uri, targetCoverFile)
            else -> extractCbzCoverAndPageCount(context, uri, targetCoverFile)
        }
    }

    private fun extractCbz(context: Context, uri: Uri, targetDir: File): List<File> {
        val pageEntries = mutableMapOf<String, ByteArray>()
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory && !isInvalidEntryName(entry.name)) {
                            val name = entry.name
                            val ext = name.substringAfterLast('.', "").lowercase()
                            if (ext in IMAGE_EXTENSIONS) {
                                val bytes = zip.readBytes()
                                pageEntries[name] = bytes
                            }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val sortedNames = pageEntries.keys.sortedWith(naturalOrderComparator())
        val resultFiles = mutableListOf<File>()

        sortedNames.forEachIndexed { index, name ->
            val bytes = pageEntries[name] ?: return@forEachIndexed
            val ext = name.substringAfterLast('.', "jpg")
            val targetFile = File(targetDir, "page_%04d.%s".format(index + 1, ext))
            FileOutputStream(targetFile).use { out ->
                out.write(bytes)
            }
            resultFiles.add(targetFile)
        }

        return resultFiles
    }

    private fun extractCbzCoverAndPageCount(context: Context, uri: Uri, targetCoverFile: File): Pair<String?, Int> {
        val pageEntries = mutableMapOf<String, ByteArray>()
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory && !isInvalidEntryName(entry.name)) {
                            val name = entry.name
                            val ext = name.substringAfterLast('.', "").lowercase()
                            if (ext in IMAGE_EXTENSIONS) {
                                pageEntries[name] = zip.readBytes()
                            }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val sortedNames = pageEntries.keys.sortedWith(naturalOrderComparator())
        val totalPages = sortedNames.size

        if (sortedNames.isNotEmpty()) {
            val firstKey = sortedNames.first()
            val bytes = pageEntries[firstKey]
            if (bytes != null) {
                try {
                    FileOutputStream(targetCoverFile).use { out ->
                        out.write(bytes)
                    }
                    return Pair("file://${targetCoverFile.absolutePath}", totalPages)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        return Pair(null, totalPages)
    }

    private fun extractCbr(context: Context, uri: Uri, targetDir: File): List<File> {
        var tempCbrFile: File? = null
        try {
            tempCbrFile = File(targetDir.parentFile, "temp_${System.currentTimeMillis()}.cbr")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempCbrFile).use { output ->
                    input.copyTo(output)
                }
            }
            Junrar.extract(tempCbrFile, targetDir)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            tempCbrFile?.delete()
        }

        return targetDir.walkTopDown()
            .filter { it.isFile && !isInvalidEntryName(it.path) && it.extension.lowercase() in IMAGE_EXTENSIONS }
            .sortedWith(Comparator { f1, f2 ->
                naturalOrderComparator().compare(f1.name, f2.name)
            })
            .toList()
    }

    private fun extractCbrCoverAndPageCount(context: Context, uri: Uri, targetCoverFile: File): Pair<String?, Int> {
        val tempDir = File(context.cacheDir, "temp_cbr_${System.currentTimeMillis()}")
        tempDir.mkdirs()
        try {
            val pages = extractCbr(context, uri, tempDir)
            val totalPages = pages.size
            if (pages.isNotEmpty()) {
                val firstPage = pages.first()
                firstPage.copyTo(targetCoverFile, overwrite = true)
                return Pair("file://${targetCoverFile.absolutePath}", totalPages)
            }
            return Pair(null, totalPages)
        } catch (e: Exception) {
            e.printStackTrace()
            return Pair(null, 0)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    private fun extractPdf(context: Context, uri: Uri, targetDir: File): List<File> {
        val resultFiles = mutableListOf<File>()
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            pfd = context.contentResolver.openFileDescriptor(uri, "r")
            if (pfd != null) {
                renderer = PdfRenderer(pfd)
                val pageCount = renderer.pageCount
                for (i in 0 until pageCount) {
                    val page = renderer.openPage(i)
                    val scale = 2f
                    val width = (page.width * scale).toInt().coerceAtLeast(800)
                    val height = (page.height * scale).toInt().coerceAtLeast(1000)
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    val targetFile = File(targetDir, "page_%04d.jpg".format(i + 1))
                    FileOutputStream(targetFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    bitmap.recycle()
                    resultFiles.add(targetFile)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                renderer?.close()
                pfd?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return resultFiles
    }

    private fun extractPdfCoverAndPageCount(context: Context, uri: Uri, targetCoverFile: File): Pair<String?, Int> {
        var pfd: ParcelFileDescriptor? = null
        var pdfRenderer: PdfRenderer? = null
        try {
            pfd = context.contentResolver.openFileDescriptor(uri, "r")
            if (pfd != null) {
                pdfRenderer = PdfRenderer(pfd)
                val pageCount = pdfRenderer.pageCount
                if (pageCount > 0) {
                    val page = pdfRenderer.openPage(0)
                    val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    FileOutputStream(targetCoverFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    }
                    bitmap.recycle()
                    return Pair("file://${targetCoverFile.absolutePath}", pageCount)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                pdfRenderer?.close()
                pfd?.close()
            } catch (_: Exception) {
            }
        }
        return Pair(null, 0)
    }

    fun naturalOrderComparator(): Comparator<String> {
        val regex = Regex("(\\d+)|(\\D+)")
        return Comparator { s1, s2 ->
            val m1 = regex.findAll(s1).map { it.value }.iterator()
            val m2 = regex.findAll(s2).map { it.value }.iterator()
            while (m1.hasNext() && m2.hasNext()) {
                val token1 = m1.next()
                val token2 = m2.next()
                val res = if (token1[0].isDigit() && token2[0].isDigit()) {
                    try {
                        BigInteger(token1).compareTo(BigInteger(token2))
                    } catch (e: Exception) {
                        token1.compareTo(token2, ignoreCase = true)
                    }
                } else {
                    token1.compareTo(token2, ignoreCase = true)
                }
                if (res != 0) return@Comparator res
            }
            if (m1.hasNext()) 1 else if (m2.hasNext()) -1 else 0
        }
    }
}
