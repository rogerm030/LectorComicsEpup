package com.example.lectorcomicsylibros.ui.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lectorcomicsylibros.data.model.Category
import com.example.lectorcomicsylibros.ui.theme.LectorComicsyLibrosTheme

@Composable
fun ManageCategoriesDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onAddCategoryClick: () -> Unit,
    onEditCategoryClick: (Category) -> Unit,
    onDeleteCategoryClick: (Category) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gestionar Categorías",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = onAddCategoryClick) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva Categoría")
                }
            }
        },
        text = {
            if (categories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay categorías creadas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(categories, key = { it.id }) { category ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(category.color))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { onEditCategoryClick(category) }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar categoría ${category.name}",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = { onDeleteCategoryClick(category) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar categoría ${category.name}",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ManageCategoriesDialogPreview() {
    LectorComicsyLibrosTheme {
        ManageCategoriesDialog(
            categories = listOf(
                Category(1, "Cómics", 0xFFFF5722),
                Category(2, "Libros", 0xFF2196F3),
                Category(3, "Manga", 0xFFE91E63)
            ),
            onDismiss = {},
            onAddCategoryClick = {},
            onEditCategoryClick = {},
            onDeleteCategoryClick = {}
        )
    }
}
