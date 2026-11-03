package com.example.lectorcomicsylibros.data.settings

import androidx.compose.ui.graphics.Color

enum class AppThemeMode(val displayName: String) {
    SYSTEM("Sistema"),
    LIGHT("Claro"),
    DARK("Oscuro")
}

enum class PresetColor(val displayName: String, val colorHex: Int) {
    PURPLE("Púrpura", 0xFF6750A4.toInt()),
    TEAL("Turquesa", 0xFF006A60.toInt()),
    BLUE("Azul", 0xFF0061A4.toInt()),
    GREEN("Verde", 0xFF2E6C00.toInt()),
    ORANGE("Naranja", 0xFF8B5000.toInt()),
    RED("Rojo", 0xFF904A42.toInt()),
    CUSTOM("Personalizado", 0xFF6750A4.toInt())
}

enum class ReadingColorScheme(
    val displayName: String,
    val backgroundColor: Long,
    val textColor: Long,
    val description: String
) {
    BLANCO("Blanco", 0xFFFFFFFF, 0xFF121212, "Fondo claro con texto oscuro"),
    SEPIA("Sepia", 0xFFFBF0D9, 0xFF362819, "Tono cálido para lecturas relajantes"),
    GRIS("Gris", 0xFF2D2D2D, 0xFFE0E0E0, "Contraste suave de tono medio"),
    OSCURO("Oscuro", 0xFF121212, 0xFFFFFFFF, "Fondo oscuro para entornos nocturnos")
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val selectedColorPreset: PresetColor = PresetColor.PURPLE,
    val primaryColorHex: Int = 0xFF6750A4.toInt(),
    val customColorHex: Int = 0xFF6750A4.toInt(),
    val readingColorScheme: ReadingColorScheme = ReadingColorScheme.BLANCO,
    val useCustomBrightness: Boolean = false,
    val brightnessValue: Float = 0.8f,
    val googleDriveConnected: Boolean = false,
    val googleDriveEmail: String = "",
    val oneDriveConnected: Boolean = false,
    val oneDriveEmail: String = ""
)
