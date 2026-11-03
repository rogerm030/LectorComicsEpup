package com.example.lectorcomicsylibros.ui.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lectorcomicsylibros.data.repository.LibraryRepository
import com.example.lectorcomicsylibros.data.settings.AppSettings
import com.example.lectorcomicsylibros.data.settings.AppThemeMode
import com.example.lectorcomicsylibros.data.settings.PresetColor
import com.example.lectorcomicsylibros.data.settings.ReadingColorScheme
import com.example.lectorcomicsylibros.data.settings.SettingsRepository
import com.example.lectorcomicsylibros.util.DeviceScannerHelper
import com.example.lectorcomicsylibros.util.ScanProgressState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

    private val deviceScannerHelper = DeviceScannerHelper(
        application,
        LibraryRepository.getInstance(application)
    )

    val scanProgress: StateFlow<ScanProgressState> = deviceScannerHelper.scanProgress

    val settingsState: StateFlow<AppSettings> = settingsRepository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    fun updateThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(mode)
        }
    }

    fun updateColorPreset(preset: PresetColor) {
        viewModelScope.launch {
            settingsRepository.updateColorPreset(preset)
        }
    }

    fun updateCustomColor(colorHex: Int) {
        viewModelScope.launch {
            settingsRepository.updateCustomColor(colorHex)
        }
    }

    fun updateReadingColorScheme(scheme: ReadingColorScheme) {
        viewModelScope.launch {
            settingsRepository.updateReadingColorScheme(scheme)
        }
    }

    fun updateBrightness(useCustom: Boolean, value: Float) {
        viewModelScope.launch {
            settingsRepository.updateBrightness(useCustom, value)
        }
    }

    fun toggleGoogleDriveConnection(currentConnected: Boolean, emailInput: String = "") {
        viewModelScope.launch {
            if (currentConnected) {
                settingsRepository.updateGoogleDriveConnection(false, "")
            } else {
                val email = if (emailInput.isNotBlank()) emailInput else "usuario.drive@gmail.com"
                settingsRepository.updateGoogleDriveConnection(true, email)
            }
        }
    }

    fun toggleOneDriveConnection(currentConnected: Boolean, emailInput: String = "") {
        viewModelScope.launch {
            if (currentConnected) {
                settingsRepository.updateOneDriveConnection(false, "")
            } else {
                val email = if (emailInput.isNotBlank()) emailInput else "lector.onedrive@outlook.com"
                settingsRepository.updateOneDriveConnection(true, email)
            }
        }
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
            val repository = SettingsRepository.getInstance(application)
            return SettingsViewModel(application, repository) as T
        }
    }
}
