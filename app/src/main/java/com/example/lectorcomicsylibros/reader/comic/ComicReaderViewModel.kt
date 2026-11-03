package com.example.lectorcomicsylibros.reader.comic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lectorcomicsylibros.data.local.AppDatabase
import com.example.lectorcomicsylibros.data.model.MediaItem
import com.example.lectorcomicsylibros.data.repository.LibraryRepository
import com.example.lectorcomicsylibros.data.settings.ReadingColorScheme
import com.example.lectorcomicsylibros.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class ComicReaderUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val item: MediaItem? = null,
    val pageFiles: List<File> = emptyList(),
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val showControls: Boolean = true,
    val showGallerySheet: Boolean = false,
    val readingColorScheme: ReadingColorScheme = ReadingColorScheme.OSCURO
)

class ComicReaderViewModel(
    application: Application,
    private val repository: LibraryRepository,
    private val itemId: Long
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ComicReaderUiState())
    val uiState: StateFlow<ComicReaderUiState> = _uiState.asStateFlow()

    init {
        loadSettingsAndComic()
    }

    private fun loadSettingsAndComic() {
        viewModelScope.launch {
            try {
                val settings = SettingsRepository.getInstance(getApplication()).settingsFlow.first()
                _uiState.update { it.copy(readingColorScheme = settings.readingColorScheme) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            loadComic()
        }
    }

    private fun loadComic() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val item = repository.getItemById(itemId)
                if (item == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "No se encontró el elemento") }
                    return@launch
                }

                val pages = ComicExtractor.extractComicPages(
                    context = getApplication(),
                    fileUriString = item.fileUri,
                    format = item.format
                )

                if (pages.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            item = item,
                            errorMessage = "No se pudieron extraer páginas de este archivo comic"
                        )
                    }
                    return@launch
                }

                val initialPage = item.lastReadPage.coerceIn(0, pages.size - 1)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        item = item,
                        pageFiles = pages,
                        currentPage = initialPage,
                        totalPages = pages.size
                    )
                }

                if (item.totalPages != pages.size) {
                    repository.updateReadingProgress(itemId, initialPage, pages.size)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al abrir cómic: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun onPageChanged(pageIndex: Int) {
        val total = _uiState.value.totalPages
        if (total == 0) return
        val clampedIndex = pageIndex.coerceIn(0, total - 1)
        _uiState.update { it.copy(currentPage = clampedIndex) }
        viewModelScope.launch {
            repository.updateReadingProgress(itemId, clampedIndex, total)
        }
    }

    fun updateReadingColorScheme(scheme: ReadingColorScheme) {
        _uiState.update { it.copy(readingColorScheme = scheme) }
        viewModelScope.launch {
            SettingsRepository.getInstance(getApplication()).updateReadingColorScheme(scheme)
        }
    }

    fun toggleControls() {
        _uiState.update { it.copy(showControls = !it.showControls) }
    }

    fun toggleGallerySheet() {
        _uiState.update { it.copy(showGallerySheet = !it.showGallerySheet) }
    }

    fun closeGallerySheet() {
        _uiState.update { it.copy(showGallerySheet = false) }
    }

    class Factory(
        private val application: Application,
        private val itemId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getDatabase(application)
            val repository = LibraryRepository(db.mediaItemDao(), db.categoryDao())
            return ComicReaderViewModel(application, repository, itemId) as T
        }
    }
}
