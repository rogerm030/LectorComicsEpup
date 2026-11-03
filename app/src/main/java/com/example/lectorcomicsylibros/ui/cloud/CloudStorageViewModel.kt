package com.example.lectorcomicsylibros.ui.cloud

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lectorcomicsylibros.data.cloud.CloudFile
import com.example.lectorcomicsylibros.data.cloud.CloudStorageManager
import com.example.lectorcomicsylibros.data.cloud.CloudStorageProvider
import com.example.lectorcomicsylibros.data.local.AppDatabase
import com.example.lectorcomicsylibros.data.repository.LibraryRepository
import com.example.lectorcomicsylibros.data.settings.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CloudStorageUiState(
    val selectedProvider: CloudStorageProvider = CloudStorageProvider.GOOGLE_DRIVE,
    val isConnected: Boolean = false,
    val accountEmail: String = "",
    val currentFolderId: String = "root",
    val folderHistory: List<Pair<String, String>> = listOf("Raíz" to "root"), // (FolderName, FolderId)
    val files: List<CloudFile> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgressMessage: String? = null,
    val snackbarMessage: String? = null
)

class CloudStorageViewModel(
    application: Application,
    private val repository: LibraryRepository,
    private val settingsRepository: SettingsRepository,
    initialProvider: CloudStorageProvider? = null
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        CloudStorageUiState(selectedProvider = initialProvider ?: CloudStorageProvider.GOOGLE_DRIVE)
    )
    val uiState: StateFlow<CloudStorageUiState> = _uiState.asStateFlow()

    private var cloudManager: CloudStorageManager = CloudStorageManager.getInstance()

    init {
        // Observe settings for connection states
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                val current = _uiState.value.selectedProvider
                val isConnected = if (current == CloudStorageProvider.GOOGLE_DRIVE) {
                    settings.googleDriveConnected
                } else {
                    settings.oneDriveConnected
                }
                val email = if (current == CloudStorageProvider.GOOGLE_DRIVE) {
                    settings.googleDriveEmail
                } else {
                    settings.oneDriveEmail
                }

                _uiState.update { it.copy(isConnected = isConnected, accountEmail = email) }
                loadFiles()
            }
        }
    }

    fun selectProvider(provider: CloudStorageProvider) {
        if (_uiState.value.selectedProvider == provider) return
        _uiState.update {
            it.copy(
                selectedProvider = provider,
                currentFolderId = "root",
                folderHistory = listOf("Raíz" to "root"),
                searchQuery = ""
            )
        }

        viewModelScope.launch {
            val settings = settingsRepository.settingsFlow.first()
            val isConnected = if (provider == CloudStorageProvider.GOOGLE_DRIVE) {
                settings.googleDriveConnected
            } else {
                settings.oneDriveConnected
            }
            val email = if (provider == CloudStorageProvider.GOOGLE_DRIVE) {
                settings.googleDriveEmail
            } else {
                settings.oneDriveEmail
            }

            _uiState.update { it.copy(isConnected = isConnected, accountEmail = email) }
            loadFiles()
        }
    }

    fun loadFiles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val service = cloudManager.getService(_uiState.value.selectedProvider)
                val list = service.listFiles(_uiState.value.currentFolderId)
                _uiState.update { it.copy(files = list, isLoading = false) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false, snackbarMessage = "Error al listar archivos: ${e.localizedMessage}") }
            }
        }
    }

    fun openFolder(folder: CloudFile) {
        if (!folder.isFolder) return
        val currentHistory = _uiState.value.folderHistory.toMutableList()
        currentHistory.add(folder.name to folder.id)
        _uiState.update {
            it.copy(
                currentFolderId = folder.id,
                folderHistory = currentHistory,
                searchQuery = ""
            )
        }
        loadFiles()
    }

    fun navigateToBreadcrumb(index: Int) {
        val history = _uiState.value.folderHistory
        if (index < 0 || index >= history.size) return
        val newHistory = history.subList(0, index + 1)
        val targetFolderId = newHistory.last().second
        _uiState.update {
            it.copy(
                currentFolderId = targetFolderId,
                folderHistory = newHistory,
                searchQuery = ""
            )
        }
        loadFiles()
    }

    fun navigateUp(): Boolean {
        val history = _uiState.value.folderHistory
        if (history.size <= 1) return false
        navigateToBreadcrumb(history.size - 2)
        return true
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun connectAccount(emailInput: String = "") {
        viewModelScope.launch {
            val provider = _uiState.value.selectedProvider
            val email = emailInput.ifBlank {
                if (provider == CloudStorageProvider.GOOGLE_DRIVE) "usuario.drive@gmail.com" else "lector.onedrive@outlook.com"
            }
            if (provider == CloudStorageProvider.GOOGLE_DRIVE) {
                settingsRepository.updateGoogleDriveConnection(true, email)
            } else {
                settingsRepository.updateOneDriveConnection(true, email)
            }
            _uiState.update { it.copy(snackbarMessage = "Cuenta de ${provider.displayName} conectada") }
        }
    }

    fun disconnectAccount() {
        viewModelScope.launch {
            val provider = _uiState.value.selectedProvider
            if (provider == CloudStorageProvider.GOOGLE_DRIVE) {
                settingsRepository.updateGoogleDriveConnection(false, "")
            } else {
                settingsRepository.updateOneDriveConnection(false, "")
            }
            _uiState.update { it.copy(snackbarMessage = "Cuenta de ${provider.displayName} desconectada") }
        }
    }

    fun downloadAndImport(file: CloudFile) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDownloading = true,
                    downloadProgressMessage = "Descargando e importando '${file.name}'..."
                )
            }
            try {
                val message = cloudManager.importCloudFile(
                    context = getApplication(),
                    repository = repository,
                    cloudFile = file
                )
                _uiState.update {
                    it.copy(
                        isDownloading = false,
                        downloadProgressMessage = null,
                        snackbarMessage = message
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isDownloading = false,
                        downloadProgressMessage = null,
                        snackbarMessage = "Error al importar desde la nube: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    class Factory(
        private val application: Application,
        private val initialProvider: CloudStorageProvider? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getDatabase(application)
            val repository = LibraryRepository(db.mediaItemDao(), db.categoryDao())
            val settingsRepo = SettingsRepository.getInstance(application)
            return CloudStorageViewModel(application, repository, settingsRepo, initialProvider) as T
        }
    }
}
