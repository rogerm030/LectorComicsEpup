package com.example.lectorcomicsylibros.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PRESET_COLOR = stringPreferencesKey("preset_color")
        val PRIMARY_COLOR_HEX = intPreferencesKey("primary_color_hex")
        val CUSTOM_COLOR_HEX = intPreferencesKey("custom_color_hex")
        val READING_COLOR_SCHEME = stringPreferencesKey("reading_color_scheme")
        val USE_CUSTOM_BRIGHTNESS = booleanPreferencesKey("use_custom_brightness")
        val BRIGHTNESS_VALUE = floatPreferencesKey("brightness_value")
        val GOOGLE_DRIVE_CONNECTED = booleanPreferencesKey("google_drive_connected")
        val GOOGLE_DRIVE_EMAIL = stringPreferencesKey("google_drive_email")
        val ONEDRIVE_CONNECTED = booleanPreferencesKey("onedrive_connected")
        val ONEDRIVE_EMAIL = stringPreferencesKey("onedrive_email")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        val themeModeName = preferences[PreferencesKeys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
        val themeMode = try { AppThemeMode.valueOf(themeModeName) } catch (_: Exception) { AppThemeMode.SYSTEM }

        val presetName = preferences[PreferencesKeys.PRESET_COLOR] ?: PresetColor.PURPLE.name
        val preset = try { PresetColor.valueOf(presetName) } catch (_: Exception) { PresetColor.PURPLE }

        val primaryColorHex = preferences[PreferencesKeys.PRIMARY_COLOR_HEX] ?: PresetColor.PURPLE.colorHex
        val customColorHex = preferences[PreferencesKeys.CUSTOM_COLOR_HEX] ?: PresetColor.PURPLE.colorHex

        val readingSchemeName = preferences[PreferencesKeys.READING_COLOR_SCHEME] ?: ReadingColorScheme.BLANCO.name
        val readingScheme = try { ReadingColorScheme.valueOf(readingSchemeName) } catch (_: Exception) { ReadingColorScheme.BLANCO }

        val useCustomBrightness = preferences[PreferencesKeys.USE_CUSTOM_BRIGHTNESS] ?: false
        val brightnessValue = preferences[PreferencesKeys.BRIGHTNESS_VALUE] ?: 0.8f

        val googleDriveConnected = preferences[PreferencesKeys.GOOGLE_DRIVE_CONNECTED] ?: false
        val googleDriveEmail = preferences[PreferencesKeys.GOOGLE_DRIVE_EMAIL] ?: ""

        val oneDriveConnected = preferences[PreferencesKeys.ONEDRIVE_CONNECTED] ?: false
        val oneDriveEmail = preferences[PreferencesKeys.ONEDRIVE_EMAIL] ?: ""

        AppSettings(
            themeMode = themeMode,
            selectedColorPreset = preset,
            primaryColorHex = primaryColorHex,
            customColorHex = customColorHex,
            readingColorScheme = readingScheme,
            useCustomBrightness = useCustomBrightness,
            brightnessValue = brightnessValue,
            googleDriveConnected = googleDriveConnected,
            googleDriveEmail = googleDriveEmail,
            oneDriveConnected = oneDriveConnected,
            oneDriveEmail = oneDriveEmail
        )
    }

    suspend fun updateThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun updateColorPreset(preset: PresetColor, customColorHex: Int? = null) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PRESET_COLOR] = preset.name
            if (preset == PresetColor.CUSTOM && customColorHex != null) {
                preferences[PreferencesKeys.CUSTOM_COLOR_HEX] = customColorHex
                preferences[PreferencesKeys.PRIMARY_COLOR_HEX] = customColorHex
            } else {
                preferences[PreferencesKeys.PRIMARY_COLOR_HEX] = preset.colorHex
            }
        }
    }

    suspend fun updateCustomColor(colorHex: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PRESET_COLOR] = PresetColor.CUSTOM.name
            preferences[PreferencesKeys.CUSTOM_COLOR_HEX] = colorHex
            preferences[PreferencesKeys.PRIMARY_COLOR_HEX] = colorHex
        }
    }

    suspend fun updateReadingColorScheme(scheme: ReadingColorScheme) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.READING_COLOR_SCHEME] = scheme.name
        }
    }

    suspend fun updateBrightness(useCustom: Boolean, value: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_CUSTOM_BRIGHTNESS] = useCustom
            preferences[PreferencesKeys.BRIGHTNESS_VALUE] = value.coerceIn(0.1f, 1.0f)
        }
    }

    suspend fun updateGoogleDriveConnection(connected: Boolean, email: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GOOGLE_DRIVE_CONNECTED] = connected
            preferences[PreferencesKeys.GOOGLE_DRIVE_EMAIL] = email
        }
    }

    suspend fun updateOneDriveConnection(connected: Boolean, email: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONEDRIVE_CONNECTED] = connected
            preferences[PreferencesKeys.ONEDRIVE_EMAIL] = email
        }
    }

    companion object {
        @Volatile
        private var instance: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return instance ?: synchronized(this) {
                instance ?: SettingsRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
