package com.example.lectorcomicsylibros.reader.comic

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicReaderScreen(
    itemId: Long,
    onBack: () -> Unit,
    viewModel: ComicReaderViewModel = viewModel(
        factory = ComicReaderViewModel.Factory(
            application = LocalContext.current.applicationContext as Application,
            itemId = itemId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

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
                Text(
                    text = "Cargando cómic...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
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
                    text = "Error al cargar cómic",
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

    val pageFiles = uiState.pageFiles
    val totalPages = uiState.totalPages

    if (pageFiles.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text("No se encontraron páginas en este cómic.")
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = uiState.currentPage.coerceIn(0, totalPages - 1),
        pageCount = { totalPages }
    )

    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = uiState.currentPage.coerceIn(0, totalPages - 1)
    )

    // Sync Horizontal Pager -> ViewModel
    LaunchedEffect(pagerState.currentPage, uiState.scrollMode) {
        if (uiState.scrollMode == ComicScrollMode.HORIZONTAL) {
            viewModel.onPageChanged(pagerState.currentPage)
        }
    }

    // Sync Vertical LazyColumn -> ViewModel
    LaunchedEffect(lazyListState, uiState.scrollMode) {
        if (uiState.scrollMode == ComicScrollMode.VERTICAL) {
            snapshotFlow { lazyListState.firstVisibleItemIndex }.collect { index ->
                viewModel.onPageChanged(index)
            }
        }
    }

    // Sync ViewModel -> pagerState & lazyListState
    LaunchedEffect(uiState.currentPage) {
        if (uiState.scrollMode == ComicScrollMode.HORIZONTAL && pagerState.currentPage != uiState.currentPage) {
            pagerState.scrollToPage(uiState.currentPage)
        } else if (uiState.scrollMode == ComicScrollMode.VERTICAL && lazyListState.firstVisibleItemIndex != uiState.currentPage) {
            lazyListState.scrollToItem(uiState.currentPage)
        }
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = uiState.showControls,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = uiState.item?.title ?: "Cómic",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Página ${uiState.currentPage + 1} de $totalPages",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleScrollMode() }) {
                            Icon(
                                imageVector = if (uiState.scrollMode == ComicScrollMode.HORIZONTAL)
                                    Icons.Default.SwapVert
                                else
                                    Icons.AutoMirrored.Filled.ViewList,
                                contentDescription = if (uiState.scrollMode == ComicScrollMode.HORIZONTAL)
                                    "Modo Desplazamiento Vertical"
                                else
                                    "Modo Paginación Horizontal"
                            )
                        }
                        IconButton(onClick = { viewModel.toggleGallerySheet() }) {
                            Icon(Icons.Default.GridView, contentDescription = "Galería de páginas")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = uiState.showControls,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = {
                                    val target = (uiState.currentPage - 1).coerceAtLeast(0)
                                    scope.launch {
                                        if (uiState.scrollMode == ComicScrollMode.HORIZONTAL) {
                                            pagerState.animateScrollToPage(target)
                                        } else {
                                            lazyListState.animateScrollToItem(target)
                                        }
                                    }
                                },
                                enabled = uiState.currentPage > 0
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Página anterior")
                            }

                            Slider(
                                value = uiState.currentPage.toFloat(),
                                onValueChange = { newPage ->
                                    val target = newPage.toInt()
                                    scope.launch {
                                        if (uiState.scrollMode == ComicScrollMode.HORIZONTAL) {
                                            pagerState.scrollToPage(target)
                                        } else {
                                            lazyListState.scrollToItem(target)
                                        }
                                    }
                                },
                                valueRange = 0f..(totalPages - 1).coerceAtLeast(1).toFloat(),
                                steps = maxOf(0, totalPages - 2),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                            )

                            IconButton(
                                onClick = {
                                    val target = (uiState.currentPage + 1).coerceAtMost(totalPages - 1)
                                    scope.launch {
                                        if (uiState.scrollMode == ComicScrollMode.HORIZONTAL) {
                                            pagerState.animateScrollToPage(target)
                                        } else {
                                            lazyListState.animateScrollToItem(target)
                                        }
                                    }
                                },
                                enabled = uiState.currentPage < totalPages - 1
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Página siguiente")
                            }
                        }

                        Text(
                            text = "Página ${uiState.currentPage + 1} de $totalPages",
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        containerColor = Color(uiState.readingColorScheme.backgroundColor)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.scrollMode == ComicScrollMode.HORIZONTAL) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    if (page in pageFiles.indices) {
                        ZoomableImage(
                            file = pageFiles[page],
                            onTap = { viewModel.toggleControls() }
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(pageFiles) { _, file ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            ZoomableImage(
                                file = file,
                                onTap = { viewModel.toggleControls() }
                            )
                        }
                    }
                }
            }
        }

        // Page Gallery Sheet
        if (uiState.showGallerySheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeGallerySheet() }
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
                            text = "Galería de Páginas ($totalPages)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = { viewModel.closeGallerySheet() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar galeria")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 100.dp),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxHeight(0.7f)
                    ) {
                        itemsIndexed(pageFiles) { index, file ->
                            val isSelected = index == uiState.currentPage
                            Box(
                                modifier = Modifier
                                    .aspectRatio(0.7f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        scope.launch {
                                            if (uiState.scrollMode == ComicScrollMode.HORIZONTAL) {
                                                pagerState.scrollToPage(index)
                                            } else {
                                                lazyListState.scrollToItem(index)
                                            }
                                        }
                                        viewModel.closeGallerySheet()
                                    }
                            ) {
                                AsyncImage(
                                    model = file,
                                    contentDescription = "Miniatura página ${index + 1}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .background(
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.7f),
                                            shape = RoundedCornerShape(topStart = 6.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
