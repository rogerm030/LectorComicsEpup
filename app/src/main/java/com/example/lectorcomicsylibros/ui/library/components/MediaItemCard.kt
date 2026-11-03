package com.example.lectorcomicsylibros.ui.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lectorcomicsylibros.data.model.MediaItem
import com.example.lectorcomicsylibros.ui.theme.LectorComicsyLibrosTheme
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaItemCard(
    item: MediaItem,
    onClick: () -> Unit,
    onAssignCategoryClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val formatColor = when (item.format.uppercase()) {
        "CBZ" -> Color(0xFFFF5722)
        "CBR" -> Color(0xFFE91E63)
        "EPUB" -> Color(0xFF00BCD4)
        "PDF" -> Color(0xFFF44336)
        else -> Color(0xFF607D8B)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.7f)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                // Cover Image or Placeholder
                val hasCover = !item.coverPath.isNullOrBlank() && File(item.coverPath).exists()
                if (hasCover) {
                    AsyncImage(
                        model = File(item.coverPath!!),
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = formatColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // Format Badge (Top Left)
                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(8.dp),
                    color = formatColor,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = item.format.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = Color.White
                    )
                }

                // Overflow Menu Button (Top Right)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    IconButton(
                        onClick = { showMenu = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opciones"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Asignar Categoría") },
                            leadingIcon = {
                                Icon(Icons.Default.FolderSpecial, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                onAssignCategoryClick()
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            }
                        )
                    }
                }

                // Progress Indicator Bar at Bottom of Cover
                val progress = if (item.totalPages > 0) {
                    item.lastReadPage.toFloat() / item.totalPages
                } else item.readingProgress

                if (progress > 0) {
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .align(Alignment.BottomCenter),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.Black.copy(alpha = 0.3f)
                    )
                }
            }

            // Info Section below Cover
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                val progressText = if (item.totalPages > 0) {
                    if (item.lastReadPage == 0) "Sin empezar"
                    else "Pág. ${item.lastReadPage} / ${item.totalPages}"
                } else {
                    if (item.readingProgress <= 0f) "Sin empezar"
                    else "${(item.readingProgress * 100).toInt()}%"
                }

                Text(
                    text = progressText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MediaItemCardPreview() {
    LectorComicsyLibrosTheme {
        Box(modifier = Modifier.padding(16.dp).width(160.dp)) {
            MediaItemCard(
                item = MediaItem(
                    id = 1,
                    title = "Spider-Man: Spider-Verse #1",
                    fileUri = "",
                    format = "CBZ",
                    lastReadPage = 12,
                    totalPages = 32
                ),
                onClick = {},
                onAssignCategoryClick = {},
                onDeleteClick = {}
            )
        }
    }
}
