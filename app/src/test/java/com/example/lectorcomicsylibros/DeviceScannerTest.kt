package com.example.lectorcomicsylibros

import com.example.lectorcomicsylibros.data.model.MediaFormat
import com.example.lectorcomicsylibros.util.ScanProgressState
import com.example.lectorcomicsylibros.util.StorageScanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceScannerTest {

    @Test
    fun testInitialScanProgressState() {
        val state = ScanProgressState()
        assertFalse(state.isScanning)
        assertEquals(0, state.scannedFilesCount)
        assertEquals("", state.currentFileName)
        assertEquals(0, state.foundNewCount)
        assertEquals(0, state.totalImported)
        assertNull(state.resultMessage)
    }

    @Test
    fun testScanProgressStateUpdates() {
        val state = ScanProgressState(
            isScanning = true,
            scannedFilesCount = 10,
            currentFileName = "comic_issue_1.cbz",
            foundNewCount = 2,
            totalImported = 2,
            resultMessage = null
        )

        assertTrue(state.isScanning)
        assertEquals(10, state.scannedFilesCount)
        assertEquals("comic_issue_1.cbz", state.currentFileName)
        assertEquals(2, state.foundNewCount)

        val completedState = state.copy(
            isScanning = false,
            resultMessage = "Se encontraron e importaron 2 nuevos cómics y libros"
        )
        assertFalse(completedState.isScanning)
        assertEquals("Se encontraron e importaron 2 nuevos cómics y libros", completedState.resultMessage)
    }

    @Test
    fun testStorageScannerTypealias() {
        // Verify StorageScanner typealias resolves to FullDeviceScanner
        val className = StorageScanner::class.java.simpleName
        assertEquals("FullDeviceScanner", className)
    }

    @Test
    fun testFileFormatIdentificationForScanner() {
        val supportedFiles = listOf("test.cbz", "manga.cbr", "book.epub", "document.pdf")
        supportedFiles.forEach { fileName ->
            val format = MediaFormat.fromFileNameOrMime(fileName)
            assertTrue("Format should not be UNKNOWN for $fileName", format != MediaFormat.UNKNOWN)
        }

        val unsupportedFiles = listOf("image.png", "notes.txt", "archive.zip", "audio.mp3")
        unsupportedFiles.forEach { fileName ->
            val format = MediaFormat.fromFileNameOrMime(fileName)
            assertEquals(MediaFormat.UNKNOWN, format)
        }
    }

    @Test
    fun testDuplicateUriFilteringLogic() {
        val existingUris = setOf(
            "content://com.android.providers.downloads.documents/document/raw%3A%2Fstorage%2Femulated%2F0%2FDownload%2Fcomic1.cbz",
            "content://com.android.providers.downloads.documents/document/raw%3A%2Fstorage%2Femulated%2F0%2FDownload%2Fbook1.epub"
        )

        val candidate1 = "content://com.android.providers.downloads.documents/document/raw%3A%2Fstorage%2Femulated%2F0%2FDownload%2Fcomic1.cbz"
        val candidate2 = "content://com.android.providers.downloads.documents/document/raw%3A%2Fstorage%2Femulated%2F0%2FDownload%2Fcomic2.cbz"

        assertTrue(existingUris.contains(candidate1))
        assertFalse(existingUris.contains(candidate2))
    }
}
