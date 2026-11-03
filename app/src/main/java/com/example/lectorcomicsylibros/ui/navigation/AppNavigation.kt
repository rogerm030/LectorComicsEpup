package com.example.lectorcomicsylibros.ui.navigation

import androidx.compose.runtime.*
import com.example.lectorcomicsylibros.data.cloud.CloudStorageProvider
import com.example.lectorcomicsylibros.data.model.MediaFormat
import com.example.lectorcomicsylibros.reader.comic.ComicReaderScreen
import com.example.lectorcomicsylibros.reader.epub.EpubReaderScreen
import com.example.lectorcomicsylibros.ui.cloud.CloudStorageScreen
import com.example.lectorcomicsylibros.ui.library.LibraryScreen
import com.example.lectorcomicsylibros.ui.settings.SettingsScreen

sealed interface Screen {
    data object Library : Screen
    data class ComicReader(val itemId: Long) : Screen
    data class EpubReader(val itemId: Long) : Screen
    data object Settings : Screen
    data class CloudStorage(val initialProvider: CloudStorageProvider? = null) : Screen
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Library) }

    when (val screen = currentScreen) {
        is Screen.Library -> {
            LibraryScreen(
                onItemClick = { item ->
                    val format = MediaFormat.fromFileNameOrMime(item.title, null)
                    val formatName = item.format.uppercase()
                    if (formatName == "EPUB" || format == MediaFormat.EPUB) {
                        currentScreen = Screen.EpubReader(item.id)
                    } else {
                        currentScreen = Screen.ComicReader(item.id)
                    }
                },
                onNavigateToSettings = {
                    currentScreen = Screen.Settings
                },
                onNavigateToCloud = {
                    currentScreen = Screen.CloudStorage()
                }
            )
        }
        is Screen.ComicReader -> {
            ComicReaderScreen(
                itemId = screen.itemId,
                onBack = { currentScreen = Screen.Library }
            )
        }
        is Screen.EpubReader -> {
            EpubReaderScreen(
                itemId = screen.itemId,
                onBack = { currentScreen = Screen.Library }
            )
        }
        is Screen.Settings -> {
            SettingsScreen(
                onBack = { currentScreen = Screen.Library },
                onNavigateToCloud = { provider ->
                    currentScreen = Screen.CloudStorage(provider)
                }
            )
        }
        is Screen.CloudStorage -> {
            CloudStorageScreen(
                onBack = { currentScreen = Screen.Library },
                initialProvider = screen.initialProvider
            )
        }
    }
}
