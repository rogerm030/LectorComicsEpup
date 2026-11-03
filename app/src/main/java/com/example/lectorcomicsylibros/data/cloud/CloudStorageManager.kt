package com.example.lectorcomicsylibros.data.cloud

import android.content.Context
import android.net.Uri
import com.example.lectorcomicsylibros.data.repository.LibraryRepository
import com.example.lectorcomicsylibros.util.FileImportHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.io.File

class CloudStorageManager private constructor(
    val googleDriveService: GoogleDriveServiceImpl,
    val oneDriveService: OneDriveServiceImpl
) {
    fun getService(provider: CloudStorageProvider): CloudStorageService {
        return when (provider) {
            CloudStorageProvider.GOOGLE_DRIVE -> googleDriveService
            CloudStorageProvider.ONEDRIVE -> oneDriveService
        }
    }

    suspend fun importCloudFile(
        context: Context,
        repository: LibraryRepository,
        cloudFile: CloudFile
    ): String {
        val service = getService(cloudFile.provider)
        val downloadedFile: File = service.downloadFile(context, cloudFile)
        val fileUri = Uri.fromFile(downloadedFile)

        val mediaItem = FileImportHelper.processImportUri(context, fileUri)

        val finalItem = mediaItem.copy(
            title = cloudFile.name.substringBeforeLast("."),
            fileUri = fileUri.toString()
        )

        repository.insertMediaItem(finalItem)
        return "Importado exitosamente: ${finalItem.title}"
    }

    companion object {
        @Volatile
        private var instance: CloudStorageManager? = null

        fun getInstance(
            gdriveConnected: Boolean = false,
            gdriveEmail: String = "",
            onedriveConnected: Boolean = false,
            onedriveEmail: String = ""
        ): CloudStorageManager {
            return instance ?: synchronized(this) {
                instance ?: CloudStorageManager(
                    googleDriveService = GoogleDriveServiceImpl(gdriveConnected, gdriveEmail),
                    oneDriveService = OneDriveServiceImpl(onedriveConnected, onedriveEmail)
                ).also { instance = it }
            }
        }
    }
}
