package com.example.lectorcomicsylibros.util

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.example.lectorcomicsylibros.data.repository.LibraryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

data class ScanProgressState(
    val isScanning: Boolean = false,
    val scannedFilesCount: Int = 0,
    val currentFileName: String = "",
    val foundNewCount: Int = 0,
    val totalImported: Int = 0,
    val resultMessage: String? = null
)

class FullDeviceScanner(
    private val context: Context,
    private val repository: LibraryRepository
) {
    private val _scanProgress = MutableStateFlow(ScanProgressState())
    val scanProgress: StateFlow<ScanProgressState> = _scanProgress.asStateFlow()

    fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
            true
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasImages = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
            val hasRead = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            hasImages || hasRead
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    suspend fun scanFullDevice(): Int = withContext(Dispatchers.IO) {
        _scanProgress.value = ScanProgressState(
            isScanning = true,
            scannedFilesCount = 0,
            currentFileName = "Iniciando escaneo...",
            foundNewCount = 0,
            totalImported = 0,
            resultMessage = null
        )

        val existingUris = try {
            repository.getAllFileUris().toSet()
        } catch (e: Exception) {
            emptySet()
        }

        var scannedCount = 0
        var importedCount = 0
        val processedKeys = mutableSetOf<String>()
        val supportedExtensions = setOf("cbz", "cbr", "epub", "pdf")

        // 1. Query MediaStore.Files
        val mediaStoreFiles = queryMediaStore()
        for ((contentUri, file) in mediaStoreFiles) {
            val key = file?.absolutePath ?: contentUri.toString()
            if (processedKeys.contains(key)) continue
            processedKeys.add(key)

            scannedCount++
            val fileName = file?.name ?: getFileNameFromUri(contentUri) ?: "Archivo"
            _scanProgress.value = _scanProgress.value.copy(
                scannedFilesCount = scannedCount,
                currentFileName = fileName
            )

            val uriString = contentUri.toString()
            val fileUriString = file?.let { Uri.fromFile(it).toString() } ?: uriString

            val isAlreadyImported = existingUris.contains(uriString) ||
                    existingUris.contains(fileUriString) ||
                    (file != null && existingUris.contains(file.absolutePath))

            if (!isAlreadyImported) {
                try {
                    val importUri = file?.let { Uri.fromFile(it) } ?: contentUri
                    val item = FileImportHelper.processImportUri(context, importUri)
                    repository.insertMediaItem(item)
                    importedCount++
                    _scanProgress.value = _scanProgress.value.copy(
                        foundNewCount = importedCount,
                        totalImported = importedCount
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // 2. Scan Environment.getExternalStorageDirectory() and subfolders directly
        val directFiles = scanExternalStorageDirectories(supportedExtensions)
        for (file in directFiles) {
            val key = file.absolutePath
            if (processedKeys.contains(key)) continue
            processedKeys.add(key)

            scannedCount++
            _scanProgress.value = _scanProgress.value.copy(
                scannedFilesCount = scannedCount,
                currentFileName = file.name
            )

            val fileUri = Uri.fromFile(file)
            val uriString = fileUri.toString()
            val rawPathString = file.absolutePath

            val isAlreadyImported = existingUris.contains(uriString) || existingUris.contains(rawPathString)
            if (!isAlreadyImported) {
                try {
                    val item = FileImportHelper.processImportUri(context, fileUri)
                    repository.insertMediaItem(item)
                    importedCount++
                    _scanProgress.value = _scanProgress.value.copy(
                        foundNewCount = importedCount,
                        totalImported = importedCount
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        val message = "Se encontraron e importaron $importedCount archivos"

        _scanProgress.value = ScanProgressState(
            isScanning = false,
            scannedFilesCount = scannedCount,
            currentFileName = "",
            foundNewCount = importedCount,
            totalImported = importedCount,
            resultMessage = message
        )

        importedCount
    }

    suspend fun scanDirectory(treeUri: Uri): Int = scanFullDevice()

    private fun queryMediaStore(): List<Pair<Uri, File?>> {
        val list = mutableListOf<Pair<Uri, File?>>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DISPLAY_NAME
        )

        val selection = "(${MediaStore.Files.FileColumns.DATA} LIKE '%.cbz' OR " +
                "${MediaStore.Files.FileColumns.DATA} LIKE '%.cbr' OR " +
                "${MediaStore.Files.FileColumns.DATA} LIKE '%.epub' OR " +
                "${MediaStore.Files.FileColumns.DATA} LIKE '%.pdf' OR " +
                "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.cbz' OR " +
                "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.cbr' OR " +
                "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.epub' OR " +
                "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.pdf')"

        try {
            context.contentResolver.query(collection, projection, selection, null, null)?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val dataColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val contentUri = ContentUris.withAppendedId(collection, id)
                    val path = if (dataColumn != -1) cursor.getString(dataColumn) else null
                    val file = if (!path.isNullOrBlank()) File(path) else null
                    list.add(Pair(contentUri, file))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun scanExternalStorageDirectories(supportedExtensions: Set<String>): List<File> {
        val result = mutableListOf<File>()
        val startDirs = mutableListOf<File>()

        val rootDir = Environment.getExternalStorageDirectory()
        if (rootDir != null && rootDir.exists() && rootDir.canRead()) {
            startDirs.add(rootDir)
        }

        val publicDirNames = listOf(
            Environment.DIRECTORY_DOWNLOADS,
            Environment.DIRECTORY_DOCUMENTS,
            Environment.DIRECTORY_PICTURES,
            Environment.DIRECTORY_MOVIES,
            "Books",
            "Comics",
            "Libros"
        )

        for (dirType in publicDirNames) {
            try {
                val pDir = Environment.getExternalStoragePublicDirectory(dirType)
                if (pDir != null && pDir.exists() && pDir.canRead() && !startDirs.contains(pDir)) {
                    startDirs.add(pDir)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val visitedDirs = mutableSetOf<String>()
        val queue = ArrayDeque<File>()
        queue.addAll(startDirs)

        while (queue.isNotEmpty()) {
            val currentDir = queue.removeFirst()
            val canonicalPath = try { currentDir.canonicalPath } catch (e: Exception) { currentDir.absolutePath }
            if (visitedDirs.contains(canonicalPath)) continue
            visitedDirs.add(canonicalPath)

            val files = try {
                currentDir.listFiles()
            } catch (e: Exception) {
                null
            } ?: continue

            for (file in files) {
                if (file.isDirectory) {
                    val name = file.name
                    if (!name.startsWith(".") &&
                        !name.equals("Android", ignoreCase = true) &&
                        !name.equals("cache", ignoreCase = true)
                    ) {
                        queue.addLast(file)
                    }
                } else if (file.isFile) {
                    val ext = file.extension.lowercase()
                    if (ext in supportedExtensions) {
                        result.add(file)
                    }
                }
            }
        }

        return result
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        return uri.path?.substringAfterLast('/')
    }

    fun clearResultMessage() {
        _scanProgress.value = _scanProgress.value.copy(resultMessage = null)
    }
}
