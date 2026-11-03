package com.example.lectorcomicsylibros.data.cloud

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.lectorcomicsylibros.data.model.MediaFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

interface CloudStorageService {
    val provider: CloudStorageProvider
    val isConnected: Flow<Boolean>
    val accountEmail: Flow<String>

    suspend fun connect(email: String = ""): Boolean
    suspend fun disconnect()
    suspend fun listFiles(folderId: String = "root"): List<CloudFile>
    suspend fun downloadFile(context: Context, file: CloudFile): File
}

abstract class BaseCloudStorageService(
    override val provider: CloudStorageProvider,
    initialConnected: Boolean = false,
    initialEmail: String = ""
) : CloudStorageService {

    protected val _isConnected = MutableStateFlow(initialConnected)
    override val isConnected: Flow<Boolean> = _isConnected.asStateFlow()

    protected val _accountEmail = MutableStateFlow(initialEmail)
    override val accountEmail: Flow<String> = _accountEmail.asStateFlow()

    override suspend fun connect(email: String): Boolean {
        _accountEmail.value = if (email.isNotBlank()) email else defaultEmail()
        _isConnected.value = true
        return true
    }

    override suspend fun disconnect() {
        _isConnected.value = false
        _accountEmail.value = ""
    }

    protected abstract fun defaultEmail(): String

    protected fun createSampleCbzFile(context: Context, fileName: String, title: String): File {
        val cloudCacheDir = File(context.cacheDir, "cloud_downloads").apply { if (!exists()) mkdirs() }
        val targetFile = File(cloudCacheDir, fileName)

        ZipOutputStream(FileOutputStream(targetFile)).use { zos ->
            val pages = listOf(
                "Portada: $title" to Color.parseColor("#4285F4"),
                "Página 1: Capítulo Inicial" to Color.parseColor("#34A853"),
                "Página 2: Desarrollo de la Historia" to Color.parseColor("#FBBC05"),
                "Página 3: Gran Clímax" to Color.parseColor("#EA4335"),
                "Página 4: Fin" to Color.parseColor("#6750A4")
            )

            pages.forEachIndexed { index, (label, bgColor) ->
                val bitmap = Bitmap.createBitmap(800, 1200, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(bgColor)

                val paint = Paint().apply {
                    color = Color.WHITE
                    textSize = 48f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                canvas.drawText(label, 400f, 600f, paint)

                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
                val imageBytes = baos.toByteArray()

                val entryName = String.format("page_%03d.jpg", index + 1)
                zos.putNextEntry(ZipEntry(entryName))
                zos.write(imageBytes)
                zos.closeEntry()
                bitmap.recycle()
            }
        }
        return targetFile
    }

    protected fun createSampleEpubFile(context: Context, fileName: String, title: String): File {
        val cloudCacheDir = File(context.cacheDir, "cloud_downloads").apply { if (!exists()) mkdirs() }
        val targetFile = File(cloudCacheDir, fileName)

        ZipOutputStream(FileOutputStream(targetFile)).use { zos ->
            // 1. mimetype
            val mimetypeBytes = "application/epub+zip".toByteArray()
            zos.putNextEntry(ZipEntry("mimetype"))
            zos.write(mimetypeBytes)
            zos.closeEntry()

            // 2. META-INF/container.xml
            val containerXml = """<?xml version="1.0"?>
                <container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
                  <rootfiles>
                    <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
                  </rootfiles>
                </container>""".trimIndent()
            zos.putNextEntry(ZipEntry("META-INF/container.xml"))
            zos.write(containerXml.toByteArray())
            zos.closeEntry()

            // 3. OEBPS/content.opf
            val contentOpf = """<?xml version="1.0" encoding="utf-8"?>
                <package xmlns="http://www.idpf.org/2007/opf" unique-identifier="BookId" version="2.0">
                  <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
                    <dc:title>$title</dc:title>
                    <dc:language>es</dc:language>
                  </metadata>
                  <manifest>
                    <item id="chapter1" href="chapter1.html" media-type="application/xhtml+xml"/>
                    <item id="chapter2" href="chapter2.html" media-type="application/xhtml+xml"/>
                  </manifest>
                  <spine toc="ncx">
                    <itemref idref="chapter1"/>
                    <itemref idref="chapter2"/>
                  </spine>
                </package>""".trimIndent()
            zos.putNextEntry(ZipEntry("OEBPS/content.opf"))
            zos.write(contentOpf.toByteArray())
            zos.closeEntry()

            // 4. OEBPS/chapter1.html
            val ch1Html = """<!DOCTYPE html>
                <html>
                <head><title>Capítulo 1</title></head>
                <body>
                  <h1>$title - Capítulo 1</h1>
                  <p>Este es el primer capítulo importado desde ${provider.displayName}. Disfruta de la lectura fluida en nuestra aplicación.</p>
                  <p>El almacenamiento en la nube permite tener acceso a todos tus libros desde cualquier lugar.</p>
                </body>
                </html>""".trimIndent()
            zos.putNextEntry(ZipEntry("OEBPS/chapter1.html"))
            zos.write(ch1Html.toByteArray())
            zos.closeEntry()

            // 5. OEBPS/chapter2.html
            val ch2Html = """<!DOCTYPE html>
                <html>
                <head><title>Capítulo 2</title></head>
                <body>
                  <h1>$title - Capítulo 2</h1>
                  <p>Continuación de la obra importada directamente. Todos los estilos y personalización de lectura se aplican dinámicamente.</p>
                </body>
                </html>""".trimIndent()
            zos.putNextEntry(ZipEntry("OEBPS/chapter2.html"))
            zos.write(ch2Html.toByteArray())
            zos.closeEntry()
        }
        return targetFile
    }
}

class GoogleDriveServiceImpl(
    initialConnected: Boolean = false,
    initialEmail: String = ""
) : BaseCloudStorageService(CloudStorageProvider.GOOGLE_DRIVE, initialConnected, initialEmail) {

    override fun defaultEmail(): String = "usuario.drive@gmail.com"

    override suspend fun listFiles(folderId: String): List<CloudFile> = withContext(Dispatchers.IO) {
        if (!_isConnected.value) return@withContext emptyList()

        when (folderId) {
            "root" -> listOf(
                CloudFile("folder_comics", "Cómics y Mangas Drive", isFolder = true, provider = provider),
                CloudFile("folder_books", "Biblioteca Digital Drive", isFolder = true, provider = provider),
                CloudFile("gdrive_doc1", "Batman El Caballero Oscuro.cbz", isFolder = false, parentId = "root", sizeBytes = 14200000L, provider = provider),
                CloudFile("gdrive_doc2", "Cien Años de Soledad.epub", isFolder = false, parentId = "root", sizeBytes = 2800000L, provider = provider)
            )
            "folder_comics" -> listOf(
                CloudFile("gdrive_comic1", "Spider-Man Into the Spider-Verse.cbz", isFolder = false, parentId = "folder_comics", sizeBytes = 18500000L, provider = provider),
                CloudFile("gdrive_comic2", "Dragon Ball Vol 1.cbz", isFolder = false, parentId = "folder_comics", sizeBytes = 22100000L, provider = provider),
                CloudFile("gdrive_comic3", "Watchmen Edicion Especial.cbr", isFolder = false, parentId = "folder_comics", sizeBytes = 31000000L, provider = provider)
            )
            "folder_books" -> listOf(
                CloudFile("gdrive_book1", "El Principito.epub", isFolder = false, parentId = "folder_books", sizeBytes = 1400000L, provider = provider),
                CloudFile("gdrive_book2", "Don Quijote de la Mancha.epub", isFolder = false, parentId = "folder_books", sizeBytes = 5600000L, provider = provider),
                CloudFile("gdrive_book3", "Guia de Arquitectura Android.pdf", isFolder = false, parentId = "folder_books", sizeBytes = 8900000L, provider = provider)
            )
            else -> emptyList()
        }
    }

    override suspend fun downloadFile(context: Context, file: CloudFile): File = withContext(Dispatchers.IO) {
        if (file.format == MediaFormat.EPUB) {
            createSampleEpubFile(context, file.name, file.name.substringBeforeLast("."))
        } else {
            createSampleCbzFile(context, file.name, file.name.substringBeforeLast("."))
        }
    }
}

class OneDriveServiceImpl(
    initialConnected: Boolean = false,
    initialEmail: String = ""
) : BaseCloudStorageService(CloudStorageProvider.ONEDRIVE, initialConnected, initialEmail) {

    override fun defaultEmail(): String = "lector.onedrive@outlook.com"

    override suspend fun listFiles(folderId: String): List<CloudFile> = withContext(Dispatchers.IO) {
        if (!_isConnected.value) return@withContext emptyList()

        when (folderId) {
            "root" -> listOf(
                CloudFile("folder_onedrive_manga", "Colección Manga OneDrive", isFolder = true, provider = provider),
                CloudFile("folder_onedrive_classics", "Libros Clásicos OneDrive", isFolder = true, provider = provider),
                CloudFile("onedrive_item1", "X-Men Días del Futuro Pasado.cbz", isFolder = false, parentId = "root", sizeBytes = 16800000L, provider = provider),
                CloudFile("onedrive_item2", "Orgullo y Prejuicio.epub", isFolder = false, parentId = "root", sizeBytes = 3200000L, provider = provider)
            )
            "folder_onedrive_manga" -> listOf(
                CloudFile("onedrive_manga1", "One Piece Capitulo 1.cbz", isFolder = false, parentId = "folder_onedrive_manga", sizeBytes = 19400000L, provider = provider),
                CloudFile("onedrive_manga2", "Naruto Vol 1.cbz", isFolder = false, parentId = "folder_onedrive_manga", sizeBytes = 21000000L, provider = provider)
            )
            "folder_onedrive_classics" -> listOf(
                CloudFile("onedrive_classic1", "El Hobbit.epub", isFolder = false, parentId = "folder_onedrive_classics", sizeBytes = 4100000L, provider = provider),
                CloudFile("onedrive_classic2", "La Odisea.epub", isFolder = false, parentId = "folder_onedrive_classics", sizeBytes = 3800000L, provider = provider)
            )
            else -> emptyList()
        }
    }

    override suspend fun downloadFile(context: Context, file: CloudFile): File = withContext(Dispatchers.IO) {
        if (file.format == MediaFormat.EPUB) {
            createSampleEpubFile(context, file.name, file.name.substringBeforeLast("."))
        } else {
            createSampleCbzFile(context, file.name, file.name.substringBeforeLast("."))
        }
    }
}
