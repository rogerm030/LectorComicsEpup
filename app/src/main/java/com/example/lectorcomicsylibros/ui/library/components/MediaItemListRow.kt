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

@Composable
fun MediaItemListRow(
    item: MediaItem,
    categoryName: String? = null,
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
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cover Thumbnail
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 76.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                val coverPath = item.coverPath
                val hasCover = !coverPath.isNullOrBlank() && File(coverPath).exists()
                if (hasCover && coverPath != null) {
                    AsyncImage(
                        model = File(coverPath),
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = formatColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = formatColor
                    ) {
                        Text(
                            text = item.format.uppercase(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = Color.White
                        )
                    }

                    if (!categoryName.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = categoryName,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp
                                ),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                val progress = if (item.totalPages > 0) {
                    item.lastReadPage.toFloat() / item.totalPages
                } else item.readingProgress

                val progressText = if (item.totalPages > 0) {
                    if (item.lastReadPage == 0) "Sin empezar"
                    else "Pág. ${item.lastReadPage} / ${item.totalPages}"
                } else {
                    if (item.readingProgress <= 0f) "Sin empezar"
                    else "${(item.readingProgress * 100).toInt()}%"
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = progressText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
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
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MediaItemListRowPreview() {
    LectorComicsyLibrosTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            MediaItemListRow(
                item = MediaItem(
                    id = 1,
                    title = "Don Quijote de la Mancha",
                    fileUri = "",
                    format = "EPUB",
                    lastReadPage = 45,
                    totalPages = 420
                ),
                categoryName = "Libros",
                onClick = {},
                onAssignCategoryClick = {},
                onDeleteClick = {}
            )
        }
    }
}
