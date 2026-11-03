package com.example.lectorcomicsylibros

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.lectorcomicsylibros.data.settings.AppSettings
import com.example.lectorcomicsylibros.data.settings.SettingsRepository
import com.example.lectorcomicsylibros.ui.navigation.AppNavigation
import com.example.lectorcomicsylibros.ui.theme.LectorComicsyLibrosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsRepo = SettingsRepository.getInstance(applicationContext)

        setContent {
            val settings by settingsRepo.settingsFlow.collectAsState(initial = AppSettings())

            SideEffect {
                val layoutParams = window.attributes
                if (settings.useCustomBrightness) {
                    layoutParams.screenBrightness = settings.brightnessValue
                } else {
                    layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                }
                window.attributes = layoutParams
            }

            LectorComicsyLibrosTheme(
                themeMode = settings.themeMode,
                primaryColorHex = settings.primaryColorHex
            ) {
                AppNavigation()
            }
        }
    }
}
