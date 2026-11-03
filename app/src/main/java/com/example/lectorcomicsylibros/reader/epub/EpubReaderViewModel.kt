package com.example.lectorcomicsylibros.reader.epub

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lectorcomicsylibros.data.local.AppDatabase
import com.example.lectorcomicsylibros.data.model.MediaItem
import com.example.lectorcomicsylibros.data.repository.LibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.example.lectorcomicsylibros.data.settings.ReadingColorScheme
import com.example.lectorcomicsylibros.data.settings.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class EpubFontFamily(val displayName: String) {
    SANS_SERIF("Sans-serif"),
    SERIF("Serif"),
    MONOSPACE("Monospace")
}

enum class EpubReaderTheme(
    val displayName: String,
    val backgroundColor: Long,
    val textColor: Long
) {
    LIGHT("Blanco", 0xFFFFFFFF, 0xFF121212),
    SEPIA("Sepia", 0xFFFBF0D9, 0xFF362819),
    GREY("Gris", 0xFF2D2D2D, 0xFFE0E0E0),
    DARK("Oscuro", 0xFF121212, 0xFFFFFFFF)
}

data class EpubReaderUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val item: MediaItem? = null,
    val epubData: EpubData? = null,
    val currentChapterIndex: Int = 0,
    val fontFamilyOption: EpubFontFamily = EpubFontFamily.SANS_SERIF,
    val fontSizeSp: Float = 18f,
    val lineSpacingMultiplier: Float = 1.4f,
    val marginDp: Int = 16,
    val readerTheme: EpubReaderTheme = EpubReaderTheme.LIGHT,
    val showTocSheet: Boolean = false,
    val showSettingsSheet: Boolean = false
)

class EpubReaderViewModel(
    application: Application,
    private val repository: LibraryRepository,
    private val itemId: Long
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(EpubReaderUiState())
    val uiState: StateFlow<EpubReaderUiState> = _uiState.asStateFlow()

    init {
        loadSettingsAndEpub()
    }

    private fun loadSettingsAndEpub() {
        viewModelScope.launch {
            try {
                val settings = SettingsRepository.getInstance(getApplication()).settingsFlow.first()
                val mappedTheme = when (settings.readingColorScheme) {
                    ReadingColorScheme.BLANCO -> EpubReaderTheme.LIGHT
                    ReadingColorScheme.SEPIA -> EpubReaderTheme.SEPIA
                    ReadingColorScheme.GRIS -> EpubReaderTheme.GREY
                    ReadingColorScheme.OSCURO -> EpubReaderTheme.DARK
                }
                _uiState.update { it.copy(readerTheme = mappedTheme) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            loadEpub()
        }
    }

    private fun loadEpub() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val item = repository.getItemById(itemId)
                if (item == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "No se encontró el elemento") }
                    return@launch
                }

                val epubData = EpubParser.parseEpub(
                    context = getApplication(),
                    fileUriString = item.fileUri
                )

                if (epubData.chapters.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            item = item,
                            errorMessage = "No se encontraron capítulos en este archivo EPUB"
                        )
                    }
                    return@launch
                }

                val initialChapter = item.lastReadPage.coerceIn(0, epubData.chapters.size - 1)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        item = item,
                        epubData = epubData,
                        currentChapterIndex = initialChapter
                    )
                }

                if (item.totalPages != epubData.chapters.size) {
                    repository.updateReadingProgress(itemId, initialChapter, epubData.chapters.size)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al abrir EPUB: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun onChapterChanged(chapterIndex: Int) {
        val total = _uiState.value.epubData?.chapters?.size ?: return
        val clamped = chapterIndex.coerceIn(0, total - 1)
        _uiState.update { it.copy(currentChapterIndex = clamped) }
        viewModelScope.launch {
            repository.updateReadingProgress(itemId, clamped, total)
        }
    }

    fun updateFontFamily(fontFamily: EpubFontFamily) {
        _uiState.update { it.copy(fontFamilyOption = fontFamily) }
    }

    fun updateFontSize(sizeSp: Float) {
        val clamped = sizeSp.coerceIn(12f, 32f)
        _uiState.update { it.copy(fontSizeSp = clamped) }
    }

    fun updateLineSpacing(multiplier: Float) {
        val clamped = multiplier.coerceIn(1.0f, 2.2f)
        _uiState.update { it.copy(lineSpacingMultiplier = clamped) }
    }

    fun updateMargin(marginDp: Int) {
        val clamped = marginDp.coerceIn(8, 36)
        _uiState.update { it.copy(marginDp = clamped) }
    }

    fun updateReaderTheme(theme: EpubReaderTheme) {
        _uiState.update { it.copy(readerTheme = theme) }
    }

    fun toggleTocSheet() {
        _uiState.update { it.copy(showTocSheet = !it.showTocSheet) }
    }

    fun closeTocSheet() {
        _uiState.update { it.copy(showTocSheet = false) }
    }

    fun toggleSettingsSheet() {
        _uiState.update { it.copy(showSettingsSheet = !it.showSettingsSheet) }
    }

    fun closeSettingsSheet() {
        _uiState.update { it.copy(showSettingsSheet = false) }
    }

    class Factory(
        private val application: Application,
        private val itemId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getDatabase(application)
            val repository = LibraryRepository(db.mediaItemDao(), db.categoryDao())
            return EpubReaderViewModel(application, repository, itemId) as T
        }
    }
}
