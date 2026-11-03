package com.example.lectorcomicsylibros

import com.example.lectorcomicsylibros.reader.comic.ComicExtractor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ComicExtractorTest {

    @Test
    fun testNaturalOrderComparatorForComicPages() {
        val comparator = ComicExtractor.naturalOrderComparator()
        val filenames = listOf(
            "page10.jpg",
            "page1.jpg",
            "page2.jpg",
            "page100.jpg",
            "page20.jpg"
        )

        val sorted = filenames.sortedWith(comparator)
        val expected = listOf(
            "page1.jpg",
            "page2.jpg",
            "page10.jpg",
            "page20.jpg",
            "page100.jpg"
        )

        assertEquals(expected, sorted)
    }

    @Test
    fun testNaturalOrderComparatorPaddedNumbersAndPrefixes() {
        val comparator = ComicExtractor.naturalOrderComparator()
        val filenames = listOf(
            "Chapter 2 - Page 10.PNG",
            "Chapter 1 - Page 2.JPG",
            "Chapter 1 - Page 1.jpg",
            "Chapter 1 - Page 10.WEBP"
        )

        val sorted = filenames.sortedWith(comparator)
        val expected = listOf(
            "Chapter 1 - Page 1.jpg",
            "Chapter 1 - Page 2.JPG",
            "Chapter 1 - Page 10.WEBP",
            "Chapter 2 - Page 10.PNG"
        )

        assertEquals(expected, sorted)
    }

    @Test
    fun testMacOsxAndHiddenFileFiltering() {
        val entryNames = listOf(
            "__MACOSX/._page1.jpg",
            ".DS_Store",
            "comic/page_01.JPG",
            "comic/page_02.PNG",
            "comic/.hidden_page.png"
        )

        val validImages = entryNames.filter { name ->
            val lower = name.lowercase()
            !lower.contains("__macosx") &&
            !lower.substringAfterLast('/').startsWith(".") &&
            setOf("jpg", "jpeg", "png", "webp", "bmp").contains(lower.substringAfterLast('.', ""))
        }

        assertEquals(2, validImages.size)
        assertTrue(validImages.contains("comic/page_01.JPG"))
        assertTrue(validImages.contains("comic/page_02.PNG"))
    }
}
