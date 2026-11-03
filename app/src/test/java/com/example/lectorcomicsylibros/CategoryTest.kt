package com.example.lectorcomicsylibros

import com.example.lectorcomicsylibros.data.model.Category
import com.example.lectorcomicsylibros.data.model.MediaItem
import com.example.lectorcomicsylibros.ui.library.LibraryUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryTest {

    @Test
    fun testCategoryCreationAndDefaults() {
        val category = Category(id = 10, name = "Manga", color = 0xFFFF5722)
        assertEquals(10L, category.id)
        assertEquals("Manga", category.name)
        assertEquals(0xFFFF5722, category.color)
    }

    @Test
    fun testCategoryUpdateCopy() {
        val category = Category(id = 5, name = "Fantasía", color = 0xFF2196F3)
        val updated = category.copy(name = "Fantasía Épica", color = 0xFFE91E63)

        assertEquals(5L, updated.id)
        assertEquals("Fantasía Épica", updated.name)
        assertEquals(0xFFE91E63, updated.color)
    }

    @Test
    fun testMediaItemCategoryDisassociationOnDelete() {
        val categoryId = 42L
        val items = listOf(
            MediaItem(id = 1, title = "Comic 1", fileUri = "file1.cbz", format = "CBZ", categoryId = categoryId),
            MediaItem(id = 2, title = "Book 1", fileUri = "file2.epub", format = "EPUB", categoryId = categoryId),
            MediaItem(id = 3, title = "Other Comic", fileUri = "file3.cbr", format = "CBR", categoryId = 99L)
        )

        // Simulate category deletion disassociation logic (removeCategoryFromItems)
        val updatedItems = items.map { item ->
            if (item.categoryId == categoryId) item.copy(categoryId = null) else item
        }

        assertNull(updatedItems[0].categoryId)
        assertNull(updatedItems[1].categoryId)
        assertEquals(99L, updatedItems[2].categoryId)

        // Verify media items were not deleted or lost
        assertEquals(3, updatedItems.size)
        assertEquals("Comic 1", updatedItems[0].title)
        assertEquals("Book 1", updatedItems[1].title)
    }

    @Test
    fun testCategoryFilterSelectionAndReset() {
        val category1 = Category(id = 1, name = "Cómics", color = 0xFFFF5722)
        val category2 = Category(id = 2, name = "Libros", color = 0xFF2196F3)
        val categories = listOf(category1, category2)

        var uiState = LibraryUiState(
            categories = categories,
            selectedCategoryId = 1L
        )
        assertEquals(1L, uiState.selectedCategoryId)

        // When category 1 is deleted, filter should reset to null (Todas Categorías)
        if (uiState.selectedCategoryId == category1.id) {
            uiState = uiState.copy(
                categories = categories.filter { it.id != category1.id },
                selectedCategoryId = null
            )
        }

        assertNull(uiState.selectedCategoryId)
        assertEquals(1, uiState.categories.size)
        assertEquals("Libros", uiState.categories[0].name)
    }

    @Test
    fun testCategoryDialogUiStateTransitions() {
        val category = Category(id = 3, name = "Sci-Fi", color = 0xFF4CAF50)

        var uiState = LibraryUiState()
        assertNull(uiState.categoryToEdit)
        assertNull(uiState.categoryToDelete)
        assertEquals(false, uiState.showManageCategoriesDialog)

        // Open edit dialog
        uiState = uiState.copy(categoryToEdit = category)
        assertEquals(category, uiState.categoryToEdit)

        // Close edit dialog
        uiState = uiState.copy(categoryToEdit = null)
        assertNull(uiState.categoryToEdit)

        // Open delete dialog
        uiState = uiState.copy(categoryToDelete = category)
        assertEquals(category, uiState.categoryToDelete)

        // Open manage categories dialog
        uiState = uiState.copy(showManageCategoriesDialog = true)
        assertTrue(uiState.showManageCategoriesDialog)
    }
}
