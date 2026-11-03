package com.example.lectorcomicsylibros.data.cloud

import com.example.lectorcomicsylibros.data.model.MediaFormat

enum class CloudStorageProvider(
    val displayName: String,
    val iconResName: String,
    val brandColorHex: Long
) {
    GOOGLE_DRIVE("Google Drive", "ic_google_drive", 0xFF4285F4),
    ONEDRIVE("Microsoft OneDrive", "ic_onedrive", 0xFF0078D4)
}

data class CloudFile(
    val id: String,
    val name: String,
    val isFolder: Boolean,
    val parentId: String? = null,
    val mimeType: String? = null,
    val sizeBytes: Long = 0L,
    val modifiedTimestamp: Long = System.currentTimeMillis(),
    val provider: CloudStorageProvider,
    val format: MediaFormat = MediaFormat.fromFileNameOrMime(name, mimeType)
)

data class CloudAccountInfo(
    val provider: CloudStorageProvider,
    val accountName: String,
    val email: String,
    val isConnected: Boolean
)
