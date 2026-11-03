package com.example.lectorcomicsylibros.reader.epub

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpubReaderScreen(
    itemId: Long,
    onBack: () -> Unit,
    viewModel: EpubReaderViewModel = viewModel(
        factory = EpubReaderViewModel.Factory(
            application = LocalContext.current.applicationContext as Application,
            itemId = itemId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Cargando libro EPUB...")
            }
        }
        return
    }

    if (uiState.errorMessage != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "Error al abrir el libro",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.errorMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onBack) {
                    Text("Volver")
                }
            }
        }
        return
    }

    val epubData = uiState.epubData
    val chapters = epubData?.chapters ?: emptyList()
    val totalChapters = chapters.size

    if (chapters.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text("No hay contenido disponible en este libro.")
        }
        return
    }

    val currentChapterIndex = uiState.currentChapterIndex.coerceIn(0, totalChapters - 1)
    val currentChapter = chapters[currentChapterIndex]

    val fontFamily = when (uiState.fontFamilyOption) {
        EpubFontFamily.SANS_SERIF -> FontFamily.SansSerif
        EpubFontFamily.SERIF -> FontFamily.Serif
        EpubFontFamily.MONOSPACE -> FontFamily.Monospace
    }

    val themeBgColor = Color(uiState.readerTheme.backgroundColor)
    val themeTextColor = Color(uiState.readerTheme.textColor)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = epubData?.title ?: "Libro",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentChapter.title,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = themeTextColor.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = themeTextColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleTocSheet() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.List,
                            contentDescription = "Tabla de contenidos",
                            tint = themeTextColor
                        )
                    }
                    IconButton(onClick = { viewModel.toggleSettingsSheet() }) {
                        Icon(
                            Icons.Default.FormatSize,
                            contentDescription = "Ajustes de texto",
                            tint = themeTextColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = themeBgColor,
                    titleContentColor = themeTextColor
                )
            )
        },
        bottomBar = {
            Surface(
                color = themeBgColor,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (currentChapterIndex > 0) {
                                viewModel.onChapterChanged(currentChapterIndex - 1)
                            }
                        },
                        enabled = currentChapterIndex > 0
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "Capítulo anterior",
                            tint = if (currentChapterIndex > 0) themeTextColor else themeTextColor.copy(alpha = 0.3f)
                        )
                    }

                    Text(
                        text = "Capítulo ${currentChapterIndex + 1} de $totalChapters",
                        style = MaterialTheme.typography.bodySmall,
                        color = themeTextColor
                    )

                    IconButton(
                        onClick = {
                            if (currentChapterIndex < totalChapters - 1) {
                                viewModel.onChapterChanged(currentChapterIndex + 1)
                            }
                        },
                        enabled = currentChapterIndex < totalChapters - 1
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Capítulo siguiente",
                            tint = if (currentChapterIndex < totalChapters - 1) themeTextColor else themeTextColor.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        },
        containerColor = themeBgColor
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                horizontal = uiState.marginDp.dp,
                vertical = 16.dp
            )
        ) {
            item {
                Text(
                    text = currentChapter.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontFamily
                    ),
                    color = themeTextColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                HorizontalDivider(
                    color = themeTextColor.copy(alpha = 0.2f),
                    modifier = Modifier.padding(bottom = 20.dp)
                )
            }

            items(currentChapter.paragraphs) { paragraph ->
                Text(
                    text = paragraph,
                    fontSize = uiState.fontSizeSp.sp,
                    lineHeight = (uiState.fontSizeSp * uiState.lineSpacingMultiplier).sp,
                    fontFamily = fontFamily,
                    color = themeTextColor,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (currentChapterIndex > 0) {
                        OutlinedButton(
                            onClick = { viewModel.onChapterChanged(currentChapterIndex - 1) }
                        ) {
                            Text("Anterior capítulo")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (currentChapterIndex < totalChapters - 1) {
                        Button(
                            onClick = { viewModel.onChapterChanged(currentChapterIndex + 1) }
                        ) {
                            Text("Siguiente capítulo")
                        }
                    }
                }
            }
        }

        // TOC Sheet
        if (uiState.showTocSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeTocSheet() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Índice de Contenidos",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = { viewModel.closeTocSheet() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar índice")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val tocItems = epubData?.toc ?: emptyList()
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(tocItems) { item ->
                            val isSelected = item.chapterIndex == currentChapterIndex
                            Surface(
                                onClick = {
                                    viewModel.onChapterChanged(item.chapterIndex)
                                    viewModel.closeTocSheet()
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.chapterIndex + 1}.",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Text Settings Sheet
        if (uiState.showSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeSettingsSheet() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ajustes de Lectura",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = { viewModel.closeSettingsSheet() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar ajustes")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Theme Selection
                    Text(
                        text = "Tema de color",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        EpubReaderTheme.entries.forEach { theme ->
                            val isSelected = uiState.readerTheme == theme
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateReaderTheme(theme) },
                                label = { Text(theme.displayName) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color(theme.backgroundColor))
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Font Family Selection
                    Text(
                        text = "Tipografía",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        EpubFontFamily.entries.forEach { font ->
                            val isSelected = uiState.fontFamilyOption == font
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateFontFamily(font) },
                                label = { Text(font.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Font Size Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tamaño de letra",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${uiState.fontSizeSp.toInt()} sp",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Slider(
                        value = uiState.fontSizeSp,
                        onValueChange = { viewModel.updateFontSize(it) },
                        valueRange = 12f..32f,
                        steps = 19
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Line Spacing Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Interlineado",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "%.1fx".format(uiState.lineSpacingMultiplier),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Slider(
                        value = uiState.lineSpacingMultiplier,
                        onValueChange = { viewModel.updateLineSpacing(it) },
                        valueRange = 1.0f..2.2f,
                        steps = 11
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Margins Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Márgenes laterales",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${uiState.marginDp} dp",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Slider(
                        value = uiState.marginDp.toFloat(),
                        onValueChange = { viewModel.updateMargin(it.toInt()) },
                        valueRange = 8f..36f,
                        steps = 6
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
