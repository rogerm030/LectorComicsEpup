package com.example.lectorcomicsylibros.ui.library

import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lectorcomicsylibros.data.model.MediaItem
import com.example.lectorcomicsylibros.ui.library.components.*
import com.example.lectorcomicsylibros.ui.theme.LectorComicsyLibrosTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel(factory = LibraryViewModel.Factory(LocalContext.current.applicationContext as android.app.Application)),
    onItemClick: (MediaItem) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToCloud: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // SAF Document Picker Launcher
    val safLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.importFiles(uris, context)
        }
    }

    // Permission Launcher for Device Scan
    val scanPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.startFullDeviceScan()
    }

    val onEscanearClick: () -> Unit = {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            scanPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            viewModel.startFullDeviceScan()
        }
    }

    // Handle Snackbar Messages for Imports
    LaunchedEffect(uiState.importMessage) {
        uiState.importMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearImportMessage()
        }
    }

    // Handle Snackbar Messages for Scanning
    LaunchedEffect(uiState.scanProgress.resultMessage) {
        uiState.scanProgress.resultMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearScanResultMessage()
        }
    }

    var isSearchExpanded by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchExpanded) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Buscar cómics o libros...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = {
                                    viewModel.updateSearchQuery("")
                                    isSearchExpanded = false
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Cerrar búsqueda")
                                }
                            }
                        )
                    } else {
                        Text(
                            text = "Mi Biblioteca",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                actions = {
                    if (!isSearchExpanded) {
                        IconButton(onClick = { isSearchExpanded = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        }
                    }
                    IconButton(onClick = onEscanearClick) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Escanear")
                    }
                    IconButton(onClick = onNavigateToCloud) {
                        Icon(Icons.Default.Cloud, contentDescription = "Almacenamiento en la Nube")
                    }
                    IconButton(onClick = { viewModel.toggleViewMode() }) {
                        Icon(
                            imageVector = if (uiState.isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                            contentDescription = if (uiState.isGridView) "Vista de Lista" else "Vista de Cuadrícula"
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes y Tema")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            Box {
                ExtendedFloatingActionButton(
                    onClick = { showFabMenu = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Escanear") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )

                DropdownMenu(
                    expanded = showFabMenu,
                    onDismissRequest = { showFabMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Escanear") },
                        leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
                        onClick = {
                            showFabMenu = false
                            onEscanearClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Importar Archivos Manuales") },
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                        onClick = {
                            showFabMenu = false
                            safLauncher.launch(
                                arrayOf(
                                    "application/x-cbz",
                                    "application/x-cbr",
                                    "application/epub+zip",
                                    "application/pdf",
                                    "application/zip",
                                    "*/*"
                                )
                            )
                        }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category & Format Filter Chips
            CategoryChipRow(
                selectedFormatFilter = uiState.selectedFormatFilter,
                onFormatFilterSelected = { viewModel.selectFormatFilter(it) },
                categories = uiState.categories,
                selectedCategoryId = uiState.selectedCategoryId,
                onCategorySelected = { viewModel.selectCategory(it) },
                onAddCategoryClick = { viewModel.openAddCategoryDialog() },
                onEditCategoryClick = { viewModel.openEditCategoryDialog(it) },
                onDeleteCategoryClick = { viewModel.openDeleteCategoryDialog(it) },
                onManageCategoriesClick = { viewModel.openManageCategoriesDialog() }
            )

            // Import Progress Indicator
            AnimatedVisibility(
                visible = uiState.isImporting,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Scanner Progress Card
            ScanProgressCard(scanProgress = uiState.scanProgress)

            // Main Content Area
            if (uiState.items.isEmpty()) {
                EmptyLibraryContent(
                    isFiltered = uiState.searchQuery.isNotBlank() || uiState.selectedCategoryId != null || uiState.selectedFormatFilter != FormatFilterOption.ALL,
                    onScanFolderClick = onEscanearClick,
                    onImportClick = {
                        safLauncher.launch(
                            arrayOf(
                                "application/x-cbz",
                                "application/x-cbr",
                                "application/epub+zip",
                                "application/pdf",
                                "*/*"
                            )
                        )
                    }
                )
            } else {
                if (uiState.isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = uiState.items,
                            key = { it.id }
                        ) { item ->
                            MediaItemCard(
                                item = item,
                                onClick = { onItemClick(item) },
                                onAssignCategoryClick = { viewModel.openAssignCategoryDialog(item) },
                                onDeleteClick = { viewModel.deleteMediaItem(item) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = uiState.items,
                            key = { it.id }
                        ) { item ->
                            val categoryName = uiState.categories.find { it.id == item.categoryId }?.name
                            MediaItemListRow(
                                item = item,
                                categoryName = categoryName,
                                onClick = { onItemClick(item) },
                                onAssignCategoryClick = { viewModel.openAssignCategoryDialog(item) },
                                onDeleteClick = { viewModel.deleteMediaItem(item) }
                            )
                        }
                    }
                }
            }
        }

        // Dialogs
        if (uiState.showAddCategoryDialog) {
            AddCategoryDialog(
                onDismiss = { viewModel.closeAddCategoryDialog() },
                onConfirm = { name, color -> viewModel.createCategory(name, color) }
            )
        }

        uiState.categoryToEdit?.let { category ->
            EditCategoryDialog(
                category = category,
                onDismiss = { viewModel.closeEditCategoryDialog() },
                onConfirm = { updatedCategory -> viewModel.updateCategory(updatedCategory) }
            )
        }

        uiState.categoryToDelete?.let { category ->
            DeleteCategoryDialog(
                category = category,
                onDismiss = { viewModel.closeDeleteCategoryDialog() },
                onConfirm = { categoryToDelete -> viewModel.deleteCategory(categoryToDelete) }
            )
        }

        if (uiState.showManageCategoriesDialog) {
            ManageCategoriesDialog(
                categories = uiState.categories,
                onDismiss = { viewModel.closeManageCategoriesDialog() },
                onAddCategoryClick = { viewModel.openAddCategoryDialog() },
                onEditCategoryClick = { viewModel.openEditCategoryDialog(it) },
                onDeleteCategoryClick = { viewModel.openDeleteCategoryDialog(it) }
            )
        }

        uiState.itemToAssignCategory?.let { item ->
            AssignCategoryDialog(
                item = item,
                categories = uiState.categories,
                onDismiss = { viewModel.closeAssignCategoryDialog() },
                onCategorySelected = { categoryId -> viewModel.assignCategory(item.id, categoryId) }
            )
        }
    }
}

@Composable
fun EmptyLibraryContent(
    isFiltered: Boolean,
    modifier: Modifier = Modifier,
    onScanFolderClick: () -> Unit = {},
    onImportClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isFiltered) Icons.AutoMirrored.Filled.LibraryBooks else Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isFiltered) "No se encontraron cómics o libros" else "Tu biblioteca está vacía",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isFiltered)
                    "Intenta cambiar el filtro o término de búsqueda"
                else
                    "Escanea una carpeta de tu dispositivo para detectar cómics y libros automáticamente o impórtalos manualmente.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (!isFiltered) {
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onScanFolderClick,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Escanear")
                    }

                    OutlinedButton(
                        onClick = onImportClick,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Importar Manual")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LibraryScreenPreview() {
    LectorComicsyLibrosTheme {
        LibraryScreen()
    }
}
