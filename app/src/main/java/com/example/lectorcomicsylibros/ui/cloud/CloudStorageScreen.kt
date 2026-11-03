package com.example.lectorcomicsylibros.ui.cloud

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lectorcomicsylibros.data.cloud.CloudFile
import com.example.lectorcomicsylibros.data.cloud.CloudStorageProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudStorageScreen(
    onBack: () -> Unit,
    initialProvider: CloudStorageProvider? = null,
    viewModel: CloudStorageViewModel = viewModel(
        factory = CloudStorageViewModel.Factory(
            application = LocalContext.current.applicationContext as Application,
            initialProvider = initialProvider
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showConnectDialog by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbarMessage()
        }
    }

    if (showConnectDialog) {
        AlertDialog(
            onDismissRequest = { showConnectDialog = false },
            title = { Text("Conectar a ${uiState.selectedProvider.displayName}") },
            text = {
                Column {
                    Text("Ingresa tu correo electrónico para simular el inicio de sesión OAuth:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Correo Electrónico") },
                        singleLine = true,
                        placeholder = {
                            Text(
                                if (uiState.selectedProvider == CloudStorageProvider.GOOGLE_DRIVE)
                                    "usuario@gmail.com" else "usuario@outlook.com"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.connectAccount(emailInput)
                        showConnectDialog = false
                        emailInput = ""
                    }
                ) {
                    Text("Conectar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConnectDialog = false
                    emailInput = ""
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Downloading Modal Dialog Overlay
    if (uiState.isDownloading) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = { Text("Importando desde la Nube") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.downloadProgressMessage ?: "Descargando archivo...",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Almacenamiento en la Nube",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Provider Tab Row
            TabRow(
                selectedTabIndex = uiState.selectedProvider.ordinal,
                modifier = Modifier.fillMaxWidth()
            ) {
                CloudStorageProvider.entries.forEach { provider ->
                    Tab(
                        selected = uiState.selectedProvider == provider,
                        onClick = { viewModel.selectProvider(provider) },
                        text = {
                            Text(
                                text = provider.displayName,
                                fontWeight = if (uiState.selectedProvider == provider) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = { Icon(Icons.Default.Cloud, contentDescription = null) }
                    )
                }
            }

            // Connection Status Banner Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isConnected)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.selectedProvider.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (uiState.isConnected) "Conectado como ${uiState.accountEmail}" else "Cuenta no vinculada",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (uiState.isConnected) {
                        OutlinedButton(
                            onClick = { viewModel.disconnectAccount() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Desconectar")
                        }
                    } else {
                        Button(
                            onClick = { showConnectDialog = true }
                        ) {
                            Text("Conectar")
                        }
                    }
                }
            }

            if (!uiState.isConnected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Cloud,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Conecta tu cuenta de ${uiState.selectedProvider.displayName} para explorar tus archivos remotos.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(onClick = { showConnectDialog = true }) {
                            Text("Conectar ${uiState.selectedProvider.displayName}")
                        }
                    }
                }
                return@Scaffold
            }

            // Folder Breadcrumb Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(uiState.folderHistory) { index, crumb ->
                        Text(
                            text = crumb.first,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (index == uiState.folderHistory.lastIndex) FontWeight.Bold else FontWeight.Normal,
                                color = if (index == uiState.folderHistory.lastIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { viewModel.navigateToBreadcrumb(index) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                        if (index < uiState.folderHistory.lastIndex) {
                            Icon(
                                Icons.AutoMirrored.Filled.NavigateNext,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Search Bar Filter
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Buscar en esta carpeta...") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Files & Folders List
            val filteredFiles = remember(uiState.files, uiState.searchQuery) {
                if (uiState.searchQuery.isBlank()) {
                    uiState.files
                } else {
                    uiState.files.filter { it.name.contains(uiState.searchQuery, ignoreCase = true) }
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredFiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.searchQuery.isNotEmpty()) "No se encontraron resultados" else "Carpeta vacía",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredFiles, key = { it.id }) { file ->
                        CloudItemRow(
                            file = file,
                            onItemClick = {
                                if (file.isFolder) {
                                    viewModel.openFolder(file)
                                } else {
                                    viewModel.downloadAndImport(file)
                                }
                            },
                            onImportClick = { viewModel.downloadAndImport(file) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CloudItemRow(
    file: CloudFile,
    onItemClick: () -> Unit,
    onImportClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (file.isFolder) {
            Icon(
                Icons.Default.Folder,
                contentDescription = "Carpeta",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        } else {
            Icon(
                Icons.Default.InsertDriveFile,
                contentDescription = "Archivo",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (file.isFolder) {
                    Text(
                        text = "Carpeta",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    AssistChip(
                        onClick = {},
                        label = { Text(file.format.name, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${file.sizeBytes / (1024 * 1024)} MB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (file.isFolder) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            IconButton(onClick = onImportClick) {
                Icon(
                    Icons.Default.CloudDownload,
                    contentDescription = "Importar archivo",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
