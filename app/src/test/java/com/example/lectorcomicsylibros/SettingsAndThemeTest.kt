package com.example.lectorcomicsylibros

import com.example.lectorcomicsylibros.data.settings.AppSettings
import com.example.lectorcomicsylibros.data.settings.AppThemeMode
import com.example.lectorcomicsylibros.data.settings.PresetColor
import com.example.lectorcomicsylibros.data.settings.ReadingColorScheme
import com.example.lectorcomicsylibros.ui.theme.createDynamicM3ColorScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SettingsAndThemeTest {

    @Test
    fun testDefaultAppSettings() {
        val settings = AppSettings()
        assertEquals(AppThemeMode.SYSTEM, settings.themeMode)
        assertEquals(PresetColor.PURPLE, settings.selectedColorPreset)
        assertEquals(ReadingColorScheme.BLANCO, settings.readingColorScheme)
        assertEquals(false, settings.useCustomBrightness)
        assertEquals(0.8f, settings.brightnessValue, 0.01f)
    }

    @Test
    fun testPresetColors() {
        assertEquals(6, PresetColor.entries.size - 1) // 6 presets + CUSTOM
        assertEquals(0xFF6750A4.toInt(), PresetColor.PURPLE.colorHex)
        assertEquals(0xFF006A60.toInt(), PresetColor.TEAL.colorHex)
        assertEquals(0xFF0061A4.toInt(), PresetColor.BLUE.colorHex)
        assertEquals(0xFF2E6C00.toInt(), PresetColor.GREEN.colorHex)
        assertEquals(0xFF8B5000.toInt(), PresetColor.ORANGE.colorHex)
        assertEquals(0xFF904A42.toInt(), PresetColor.RED.colorHex)
    }

    @Test
    fun testReadingColorSchemes() {
        assertEquals(4, ReadingColorScheme.entries.size)
        val blanco = ReadingColorScheme.BLANCO
        assertEquals(0xFFFFFFFF, blanco.backgroundColor)
        assertEquals(0xFF121212, blanco.textColor)

        val sepia = ReadingColorScheme.SEPIA
        assertEquals(0xFFFBF0D9, sepia.backgroundColor)

        val gris = ReadingColorScheme.GRIS
        assertEquals(0xFF2D2D2D, gris.backgroundColor)

        val oscuro = ReadingColorScheme.OSCURO
        assertEquals(0xFF121212, oscuro.backgroundColor)
    }

    @Test
    fun testDynamicColorSchemeCreation() {
        val lightScheme = createDynamicM3ColorScheme(PresetColor.BLUE.colorHex, isDark = false)
        assertNotNull(lightScheme)

        val darkScheme = createDynamicM3ColorScheme(PresetColor.TEAL.colorHex, isDark = true)
        assertNotNull(darkScheme)
    }
}
