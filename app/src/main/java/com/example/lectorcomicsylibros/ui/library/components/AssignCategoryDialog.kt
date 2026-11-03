package com.example.lectorcomicsylibros.ui.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lectorcomicsylibros.data.model.Category
import com.example.lectorcomicsylibros.data.model.MediaItem
import com.example.lectorcomicsylibros.ui.theme.LectorComicsyLibrosTheme

@Composable
fun AssignCategoryDialog(
    item: MediaItem,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onCategorySelected: (categoryId: Long?) -> Unit
) {
    var selectedCategoryId by remember { mutableStateOf(item.categoryId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Asignar Categoría",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Option: Sin Categoría (None)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickable { selectedCategoryId = null }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCategoryId == null,
                            onClick = { selectedCategoryId = null }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Sin Categoría",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                items(categories) { category ->
                    val isSelected = selectedCategoryId == category.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickable { selectedCategoryId = category.id }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedCategoryId = category.id }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(category.color))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCategorySelected(selectedCategoryId)
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AssignCategoryDialogPreview() {
    LectorComicsyLibrosTheme {
        AssignCategoryDialog(
            item = MediaItem(id = 1, title = "Ejemplo de Libro", fileUri = "", format = "EPUB"),
            categories = listOf(
                Category(1, "Cómics", 0xFFFF5722),
                Category(2, "Libros", 0xFF2196F3)
            ),
            onDismiss = {},
            onCategorySelected = {}
        )
    }
}
