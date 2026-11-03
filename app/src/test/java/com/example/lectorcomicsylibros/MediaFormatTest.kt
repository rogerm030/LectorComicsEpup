package com.example.lectorcomicsylibros

import com.example.lectorcomicsylibros.data.model.MediaFormat
import org.junit.Assert.assertEquals
import org.junit.Test

class MediaFormatTest {

    @Test
    fun testMediaFormatDetectionFromFileName() {
        assertEquals(MediaFormat.CBZ, MediaFormat.fromFileNameOrMime("comic.cbz"))
        assertEquals(MediaFormat.CBZ, MediaFormat.fromFileNameOrMime("archive.zip"))
        assertEquals(MediaFormat.CBR, MediaFormat.fromFileNameOrMime("comic.cbr"))
        assertEquals(MediaFormat.CBR, MediaFormat.fromFileNameOrMime("archive.rar"))
        assertEquals(MediaFormat.EPUB, MediaFormat.fromFileNameOrMime("book.epub"))
        assertEquals(MediaFormat.PDF, MediaFormat.fromFileNameOrMime("document.pdf"))
        assertEquals(MediaFormat.UNKNOWN, MediaFormat.fromFileNameOrMime("unknown.txt"))
    }

    @Test
    fun testMediaFormatDetectionFromMimeType() {
        assertEquals(MediaFormat.CBZ, MediaFormat.fromFileNameOrMime("file", "application/x-cbz"))
        assertEquals(MediaFormat.CBR, MediaFormat.fromFileNameOrMime("file", "application/x-cbr"))
        assertEquals(MediaFormat.EPUB, MediaFormat.fromFileNameOrMime("file", "application/epub+zip"))
        assertEquals(MediaFormat.PDF, MediaFormat.fromFileNameOrMime("file", "application/pdf"))
    }
}
