package com.example.lectorcomicsylibros

import com.example.lectorcomicsylibros.data.cloud.CloudFile
import com.example.lectorcomicsylibros.data.cloud.CloudStorageProvider
import com.example.lectorcomicsylibros.data.cloud.GoogleDriveServiceImpl
import com.example.lectorcomicsylibros.data.cloud.OneDriveServiceImpl
import com.example.lectorcomicsylibros.data.model.MediaFormat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudStorageTest {

    @Test
    fun testGoogleDriveServiceConnectionAndListing() = runBlocking {
        val driveService = GoogleDriveServiceImpl(initialConnected = false)
        assertFalse(driveService.isConnected.first())

        val emptyList = driveService.listFiles("root")
        assertTrue(emptyList.isEmpty())

        driveService.connect("test.user@gmail.com")
        assertTrue(driveService.isConnected.first())
        assertEquals("test.user@gmail.com", driveService.accountEmail.first())

        val rootFiles = driveService.listFiles("root")
        assertTrue(rootFiles.isNotEmpty())
        assertTrue(rootFiles.any { it.name == "Cómics y Mangas Drive" && it.isFolder })

        val subFolderFiles = driveService.listFiles("folder_comics")
        assertTrue(subFolderFiles.isNotEmpty())
        assertTrue(subFolderFiles.any { it.format == MediaFormat.CBZ || it.format == MediaFormat.CBR })

        driveService.disconnect()
        assertFalse(driveService.isConnected.first())
    }

    @Test
    fun testOneDriveServiceConnectionAndListing() = runBlocking {
        val oneDriveService = OneDriveServiceImpl(initialConnected = true, initialEmail = "user@onedrive.com")
        assertTrue(oneDriveService.isConnected.first())
        assertEquals("user@onedrive.com", oneDriveService.accountEmail.first())

        val rootFiles = oneDriveService.listFiles("root")
        assertTrue(rootFiles.isNotEmpty())
        assertTrue(rootFiles.any { it.isFolder })

        val classicFiles = oneDriveService.listFiles("folder_onedrive_classics")
        assertTrue(classicFiles.isNotEmpty())
        assertTrue(classicFiles.all { it.format == MediaFormat.EPUB })
    }

    @Test
    fun testCloudFileFormatDetection() {
        val fileCbz = CloudFile("1", "Comic.cbz", isFolder = false, provider = CloudStorageProvider.GOOGLE_DRIVE)
        assertEquals(MediaFormat.CBZ, fileCbz.format)

        val fileEpub = CloudFile("2", "Libro.epub", isFolder = false, provider = CloudStorageProvider.ONEDRIVE)
        assertEquals(MediaFormat.EPUB, fileEpub.format)
    }
}
