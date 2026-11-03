package com.example.lectorcomicsylibros.ui.library

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lectorcomicsylibros.data.model.Category
import com.example.lectorcomicsylibros.data.model.MediaItem
import com.example.lectorcomicsylibros.data.repository.LibraryRepository
import com.example.lectorcomicsylibros.util.DeviceScannerHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class FilterParams(
    val categoryId: Long?,
    val formatFilter: FormatFilterOption,
    val searchQuery: String
)

private data class UiOptions(
    val isGridView: Boolean,
    val showAddCategoryDialog: Boolean,
    val itemToAssignCategory: MediaItem?,
    val categoryToEdit: Category?,
    val categoryToDelete: Category?,
    val showManageCategoriesDialog: Boolean,
    val isImporting: Boolean,
    val importMessage: String?
)

class LibraryViewModel(
    application: Application,
    private val repository: LibraryRepository
) : AndroidViewModel(application) {

    private val deviceScannerHelper = DeviceScannerHelper(application, repository)

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    private val _selectedFormatFilter = MutableStateFlow(FormatFilterOption.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _isGridView = MutableStateFlow(true)
    private val _showAddCategoryDialog = MutableStateFlow(false)
    private val _itemToAssignCategory = MutableStateFlow<MediaItem?>(null)
    private val _categoryToEdit = MutableStateFlow<Category?>(null)
    private val _categoryToDelete = MutableStateFlow<Category?>(null)
    private val _showManageCategoriesDialog = MutableStateFlow(false)
    private val _isImporting = MutableStateFlow(false)
    private val _importMessage = MutableStateFlow<String?>(null)

    private val filterParams = combine(
        _selectedCategoryId,
        _selectedFormatFilter,
        _searchQuery
    ) { catId, format, query ->
        FilterParams(catId, format, query)
    }

    private val uiOptions = combine(
        _isGridView,
        _showAddCategoryDialog,
        _itemToAssignCategory,
        _categoryToEdit,
        _categoryToDelete,
        _showManageCategoriesDialog,
        _isImporting,
        _importMessage
    ) { array ->
        UiOptions(
            isGridView = array[0] as Boolean,
            showAddCategoryDialog = array[1] as Boolean,
            itemToAssignCategory = array[2] as MediaItem?,
            categoryToEdit = array[3] as Category?,
            categoryToDelete = array[4] as Category?,
            showManageCategoriesDialog = array[5] as Boolean,
            isImporting = array[6] as Boolean,
            importMessage = array[7] as String?
        )
    }

    val uiState: StateFlow<LibraryUiState> = combine(
        repository.allItems,
        repository.allCategories,
        filterParams,
        uiOptions,
        deviceScannerHelper.scanProgress
    ) { allItems, categories, filter, options, scanProgress ->

        val filteredItems = allItems.filter { item ->
            val matchesCategory = (filter.categoryId == null || item.categoryId == filter.categoryId)

            val matchesFormat = when (filter.formatFilter) {
                FormatFilterOption.ALL -> true
                FormatFilterOption.COMICS -> item.format == "CBZ" || item.format == "CBR"
                FormatFilterOption.BOOKS -> item.format == "EPUB" || item.format == "PDF"
            }

            val matchesSearch = filter.searchQuery.isBlank() ||
                    item.title.contains(filter.searchQuery, ignoreCase = true)

            matchesCategory && matchesFormat && matchesSearch
        }

        LibraryUiState(
            items = filteredItems,
            categories = categories,
            selectedCategoryId = filter.categoryId,
            selectedFormatFilter = filter.formatFilter,
            searchQuery = filter.searchQuery,
            isGridView = options.isGridView,
            showAddCategoryDialog = options.showAddCategoryDialog,
            itemToAssignCategory = options.itemToAssignCategory,
            categoryToEdit = options.categoryToEdit,
            categoryToDelete = options.categoryToDelete,
            showManageCategoriesDialog = options.showManageCategoriesDialog,
            isImporting = options.isImporting,
            importMessage = options.importMessage,
            scanProgress = scanProgress
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState()
    )

    init {
        viewModelScope.launch {
            repository.seedDefaultCategoriesIfEmpty()
        }
    }

    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun selectFormatFilter(filter: FormatFilterOption) {
        _selectedFormatFilter.value = filter
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleViewMode() {
        _isGridView.value = !_isGridView.value
    }

    fun openAddCategoryDialog() {
        _showAddCategoryDialog.value = true
    }

    fun closeAddCategoryDialog() {
        _showAddCategoryDialog.value = false
    }

    fun openAssignCategoryDialog(item: MediaItem) {
        _itemToAssignCategory.value = item
    }

    fun closeAssignCategoryDialog() {
        _itemToAssignCategory.value = null
    }

    fun createCategory(name: String, color: Long) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertCategory(Category(name = name.trim(), color = color))
            closeAddCategoryDialog()
        }
    }

    fun openEditCategoryDialog(category: Category) {
        _categoryToEdit.value = category
    }

    fun closeEditCategoryDialog() {
        _categoryToEdit.value = null
    }

    fun updateCategory(category: Category) {
        if (category.name.isBlank()) return
        viewModelScope.launch {
            repository.updateCategory(category.copy(name = category.name.trim()))
            closeEditCategoryDialog()
        }
    }

    fun openDeleteCategoryDialog(category: Category) {
        _categoryToDelete.value = category
    }

    fun closeDeleteCategoryDialog() {
        _categoryToDelete.value = null
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            if (_selectedCategoryId.value == category.id) {
                _selectedCategoryId.value = null
            }
            repository.deleteCategory(category)
            closeDeleteCategoryDialog()
        }
    }

    fun openManageCategoriesDialog() {
        _showManageCategoriesDialog.value = true
    }

    fun closeManageCategoriesDialog() {
        _showManageCategoriesDialog.value = false
    }

    fun assignCategory(itemId: Long, categoryId: Long?) {
        viewModelScope.launch {
            repository.assignCategory(itemId, categoryId)
            closeAssignCategoryDialog()
        }
    }

    fun deleteMediaItem(item: MediaItem) {
        viewModelScope.launch {
            repository.deleteMediaItem(item)
        }
    }

    fun importFiles(uris: List<Uri>, context: Context) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val imported = repository.importUris(context, uris)
                _importMessage.value = "Se importaron ${imported.size} archivos exitosamente"
            } catch (e: Exception) {
                e.printStackTrace()
                _importMessage.value = "Error al importar archivos: ${e.localizedMessage}"
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun clearImportMessage() {
        _importMessage.value = null
    }

    fun startFullDeviceScan() {
        viewModelScope.launch {
            deviceScannerHelper.scanFullDevice()
        }
    }

    fun scanDirectory(treeUri: Uri) {
        viewModelScope.launch {
            deviceScannerHelper.scanDirectory(treeUri)
        }
    }

    fun clearScanResultMessage() {
        deviceScannerHelper.clearResultMessage()
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repository = LibraryRepository.getInstance(application)
            return LibraryViewModel(application, repository) as T
        }
    }
}
