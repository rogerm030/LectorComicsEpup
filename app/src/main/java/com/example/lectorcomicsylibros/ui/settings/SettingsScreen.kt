package com.example.lectorcomicsylibros.ui.settings

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lectorcomicsylibros.data.cloud.CloudStorageProvider
import com.example.lectorcomicsylibros.data.settings.AppThemeMode
import com.example.lectorcomicsylibros.data.settings.PresetColor
import com.example.lectorcomicsylibros.data.settings.ReadingColorScheme
import com.example.lectorcomicsylibros.ui.library.components.ScanProgressCard
import com.example.lectorcomicsylibros.ui.settings.components.CustomColorPickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToCloud: (CloudStorageProvider?) -> Unit,
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(LocalContext.current.applicationContext as Application)
    )
) {
    val context = LocalContext.current
    val settingsState by viewModel.settingsState.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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

    LaunchedEffect(scanProgress.resultMessage) {
        scanProgress.resultMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearScanResultMessage()
        }
    }

    var showColorPickerDialog by remember { mutableStateOf(false) }
    var connectEmailDialogProvider by remember { mutableStateOf<CloudStorageProvider?>(null) }
    var emailInputText by remember { mutableStateOf("") }

    if (showColorPickerDialog) {
        CustomColorPickerDialog(
            initialColorHex = settingsState.customColorHex,
            onDismissRequest = { showColorPickerDialog = false },
            onColorSelected = { newColor ->
                viewModel.updateCustomColor(newColor)
            }
        )
    }

    // Email Input Dialog for Cloud Connection
    connectEmailDialogProvider?.let { provider ->
        AlertDialog(
            onDismissRequest = { connectEmailDialogProvider = null },
            title = { Text("Conectar a ${provider.displayName}") },
            text = {
                Column {
                    Text("Introduce la cuenta de correo para iniciar sesión en ${provider.displayName}:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = emailInputText,
                        onValueChange = { emailInputText = it },
                        label = { Text("Correo Electrónico") },
                        singleLine = true,
                        placeholder = {
                            Text(
                                if (provider == CloudStorageProvider.GOOGLE_DRIVE) "usuario@gmail.com" else "usuario@outlook.com"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val input = emailInputText.ifBlank {
                            if (provider == CloudStorageProvider.GOOGLE_DRIVE) "usuario.drive@gmail.com" else "lector.onedrive@outlook.com"
                        }
                        if (provider == CloudStorageProvider.GOOGLE_DRIVE) {
                            viewModel.toggleGoogleDriveConnection(false, input)
                        } else {
                            viewModel.toggleOneDriveConnection(false, input)
                        }
                        connectEmailDialogProvider = null
                        emailInputText = ""
                    }
                ) {
                    Text("Iniciar Sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    connectEmailDialogProvider = null
                    emailInputText = ""
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ajustes y Personalización",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Section: Escanear Dispositivo
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Escanear",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Text(
                            text = "Escanea el dispositivo para detectar automáticamente nuevos cómics (CBZ, CBR) y libros (EPUB, PDF).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        ScanProgressCard(scanProgress = scanProgress)

                        Button(
                            onClick = onEscanearClick,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !scanProgress.isScanning
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (scanProgress.isScanning) "Escaneando..." else "Escanear")
                        }
                    }
                }
            }

            // Section 1: Global App Theme
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Brightness6,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Tema Global de la App",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Text(
                            text = "Selecciona la apariencia general entre modo claro, oscuro o del sistema.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppThemeMode.entries.forEach { mode ->
                                val selected = settingsState.themeMode == mode
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.updateThemeMode(mode) },
                                    label = { Text(mode.displayName) },
                                    leadingIcon = if (selected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: App Color Palette Customization
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Paleta de Color de la App",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Text(
                            text = "Personaliza el color primario semilla de la aplicación o elige tu propio color.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Preset Circles Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(
                                PresetColor.PURPLE,
                                PresetColor.TEAL,
                                PresetColor.BLUE,
                                PresetColor.GREEN,
                                PresetColor.ORANGE,
                                PresetColor.RED
                            ).forEach { preset ->
                                val selected = settingsState.selectedColorPreset == preset
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(preset.colorHex))
                                        .border(
                                            width = if (selected) 3.dp else 1.dp,
                                            color = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { viewModel.updateColorPreset(preset) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            // Custom Color Button
                            val isCustomSelected = settingsState.selectedColorPreset == PresetColor.CUSTOM
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(settingsState.customColorHex))
                                    .border(
                                        width = if (isCustomSelected) 3.dp else 1.dp,
                                        color = if (isCustomSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        viewModel.updateColorPreset(PresetColor.CUSTOM)
                                        showColorPickerDialog = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ColorLens,
                                    contentDescription = "Color Personalizado",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Button(
                            onClick = { showColorPickerDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ColorLens, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Abrir Selector de Color Personalizado (HSV / RGB)")
                        }
                    }
                }
            }

            // Section 3: Default Reading Background Color Scheme
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Esquema de Color de Lectura Predeterminado",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Text(
                            text = "Selecciona el esquema predeterminado para el fondo de lectura de cómics y libros EPUB.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ReadingColorScheme.entries.forEach { scheme ->
                                val isSelected = settingsState.readingColorScheme == scheme
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { viewModel.updateReadingColorScheme(scheme) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(scheme.backgroundColor)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = scheme.displayName,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(scheme.textColor)
                                            )
                                            Text(
                                                text = scheme.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(scheme.textColor).copy(alpha = 0.8f)
                                            )
                                        }

                                        if (isSelected) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Seleccionado",
                                                tint = Color(scheme.textColor),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 4: In-App Screen Brightness Control
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Brillo de Pantalla Integrado",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Personalizar Brillo en la App",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Ajusta el nivel de brillo de la pantalla directamente dentro de la aplicación.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = settingsState.useCustomBrightness,
                                onCheckedChange = { checked ->
                                    viewModel.updateBrightness(checked, settingsState.brightnessValue)
                                }
                            )
                        }

                        if (settingsState.useCustomBrightness) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Nivel de Brillo", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = "${(settingsState.brightnessValue * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                Slider(
                                    value = settingsState.brightnessValue,
                                    onValueChange = { valNew ->
                                        viewModel.updateBrightness(true, valNew)
                                    },
                                    valueRange = 0.1f..1.0f
                                )
                            }
                        }
                    }
                }
            }

            // Section 5: Cloud Storage Integration
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Cloud,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Almacenamiento en la Nube",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Text(
                            text = "Conecta tus cuentas de Google Drive o Microsoft OneDrive para explorar e importar cómics y libros directamente.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Google Drive Card
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Google Drive",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )

                                    AssistChip(
                                        onClick = {},
                                        label = {
                                            Text(if (settingsState.googleDriveConnected) "Conectado" else "Desconectado")
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = if (settingsState.googleDriveConnected)
                                                MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    )
                                }

                                if (settingsState.googleDriveConnected) {
                                    Text(
                                        text = "Cuenta: ${settingsState.googleDriveEmail}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (settingsState.googleDriveConnected) {
                                                viewModel.toggleGoogleDriveConnection(true)
                                            } else {
                                                connectEmailDialogProvider = CloudStorageProvider.GOOGLE_DRIVE
                                            }
                                        },
                                        colors = if (settingsState.googleDriveConnected)
                                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                        else ButtonDefaults.buttonColors(),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (settingsState.googleDriveConnected) "Desconectar" else "Conectar")
                                    }

                                    OutlinedButton(
                                        onClick = { onNavigateToCloud(CloudStorageProvider.GOOGLE_DRIVE) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Explorar")
                                    }
                                }
                            }
                        }

                        // Microsoft OneDrive Card
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Microsoft OneDrive",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )

                                    AssistChip(
                                        onClick = {},
                                        label = {
                                            Text(if (settingsState.oneDriveConnected) "Conectado" else "Desconectado")
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = if (settingsState.oneDriveConnected)
                                                MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    )
                                }

                                if (settingsState.oneDriveConnected) {
                                    Text(
                                        text = "Cuenta: ${settingsState.oneDriveEmail}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (settingsState.oneDriveConnected) {
                                                viewModel.toggleOneDriveConnection(true)
                                            } else {
                                                connectEmailDialogProvider = CloudStorageProvider.ONEDRIVE
                                            }
                                        },
                                        colors = if (settingsState.oneDriveConnected)
                                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                        else ButtonDefaults.buttonColors(),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (settingsState.oneDriveConnected) "Desconectar" else "Conectar")
                                    }

                                    OutlinedButton(
                                        onClick = { onNavigateToCloud(CloudStorageProvider.ONEDRIVE) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Explorar")
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { onNavigateToCloud(null) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Cloud, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Abrir Pantalla Principal de la Nube")
                        }
                    }
                }
            }
        }
    }
}
