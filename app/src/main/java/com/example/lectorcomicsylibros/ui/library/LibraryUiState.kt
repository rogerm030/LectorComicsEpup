package com.example.lectorcomicsylibros.ui.library

import com.example.lectorcomicsylibros.data.model.Category
import com.example.lectorcomicsylibros.data.model.MediaItem
import com.example.lectorcomicsylibros.util.ScanProgressState

enum class FormatFilterOption(val label: String) {
    ALL("Todos"),
    COMICS("Cómics"),
    BOOKS("Libros")
}

data class LibraryUiState(
    val items: List<MediaItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val selectedFormatFilter: FormatFilterOption = FormatFilterOption.ALL,
    val searchQuery: String = "",
    val isGridView: Boolean = true,
    val showAddCategoryDialog: Boolean = false,
    val categoryToEdit: Category? = null,
    val categoryToDelete: Category? = null,
    val showManageCategoriesDialog: Boolean = false,
    val itemToAssignCategory: MediaItem? = null,
    val isLoading: Boolean = false,
    val isImporting: Boolean = false,
    val importMessage: String? = null,
    val scanProgress: ScanProgressState = ScanProgressState()
)
