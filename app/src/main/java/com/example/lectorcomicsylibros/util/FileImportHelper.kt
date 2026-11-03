package com.example.lectorcomicsylibros.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import com.example.lectorcomicsylibros.data.model.MediaFormat
import com.example.lectorcomicsylibros.data.model.MediaItem
import com.example.lectorcomicsylibros.reader.comic.ComicExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

object FileImportHelper {

    suspend fun processImportUri(context: Context, uri: Uri): MediaItem = withContext(Dispatchers.IO) {
        // 1. Take persistable permission if possible
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Get display name
        val fileName = getFileName(context, uri) ?: "Archivo Sin Nombre"
        val mimeType = context.contentResolver.getType(uri)
        val format = MediaFormat.fromFileNameOrMime(fileName, mimeType)

        // Clean title (remove extension)
        val title = fileName.substringBeforeLast(".")

        // 3. Extract cover & page count
        var coverPath: String? = null
        var totalPages = 0

        val coversDir = File(context.filesDir, "covers").apply { if (!exists()) mkdirs() }
        val coverFile = File(coversDir, "${System.currentTimeMillis()}_${fileName.hashCode()}.jpg")

        when (format) {
            MediaFormat.CBZ, MediaFormat.CBR, MediaFormat.PDF -> {
                val result = ComicExtractor.extractComicCoverAndPageCount(context, uri, format.name, coverFile)
                coverPath = result.first
                totalPages = result.second
            }
            MediaFormat.EPUB -> {
                val result = extractEpubCoverAndPageCount(context, uri, coverFile)
                coverPath = result.first
                totalPages = result.second
            }
            MediaFormat.UNKNOWN -> {
                totalPages = 0
            }
        }

        MediaItem(
            title = title,
            fileUri = uri.toString(),
            format = format.name,
            coverPath = coverPath,
            lastReadPage = 0,
            totalPages = totalPages,
            addedDate = System.currentTimeMillis(),
            readingProgress = 0f
        )
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        name = cursor.getString(index)
                    }
                }
            }
        }
        if (name == null) {
            name = uri.path?.substringAfterLast('/')
        }
        return name
    }

    private fun extractEpubCoverAndPageCount(context: Context, uri: Uri, targetCoverFile: File): Pair<String?, Int> {
        var coverBytes: ByteArray? = null
        var firstImageBytes: ByteArray? = null
        var chapterCount = 0

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipStream ->
                    var entry = zipStream.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory) {
                            val entryName = entry.name.lowercase()
                            if (entryName.endsWith(".html") || entryName.endsWith(".xhtml")) {
                                chapterCount++
                            }
                            if (entryName.endsWith(".jpg") || entryName.endsWith(".jpeg") ||
                                entryName.endsWith(".png") || entryName.endsWith(".webp")
                            ) {
                                if (entryName.contains("cover") && coverBytes == null) {
                                    coverBytes = zipStream.readBytes()
                                } else if (firstImageBytes == null) {
                                    firstImageBytes = zipStream.readBytes()
                                }
                            }
                        }
                        zipStream.closeEntry()
                        entry = zipStream.nextEntry
                    }
                }
            }

            val finalBytes = coverBytes ?: firstImageBytes
            finalBytes?.let { bytes ->
                FileOutputStream(targetCoverFile).use { out ->
                    out.write(bytes)
                }
                return Pair("file://${targetCoverFile.absolutePath}", maxOf(chapterCount, 1))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Pair(null, maxOf(chapterCount, 1))
    }
}
