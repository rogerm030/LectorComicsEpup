package com.example.lectorcomicsylibros

import com.example.lectorcomicsylibros.reader.comic.ComicExtractor
import org.junit.Assert.assertEquals
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
            "Chapter 2 - Page 10.png",
            "Chapter 1 - Page 2.png",
            "Chapter 1 - Page 1.png",
            "Chapter 1 - Page 10.png"
        )

        val sorted = filenames.sortedWith(comparator)
        val expected = listOf(
            "Chapter 1 - Page 1.png",
            "Chapter 1 - Page 2.png",
            "Chapter 1 - Page 10.png",
            "Chapter 2 - Page 10.png"
        )

        assertEquals(expected, sorted)
    }
}
