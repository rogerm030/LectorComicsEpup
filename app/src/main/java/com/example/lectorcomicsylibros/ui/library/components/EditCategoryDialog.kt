package com.example.lectorcomicsylibros.ui.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.lectorcomicsylibros.ui.theme.LectorComicsyLibrosTheme

@Composable
fun EditCategoryDialog(
    category: Category,
    onDismiss: () -> Unit,
    onConfirm: (Category) -> Unit
) {
    var name by remember(category) { mutableStateOf(category.name) }
    var selectedColor by remember(category) { mutableLongStateOf(category.color) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Editar Categoría",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        showError = false
                    },
                    label = { Text("Nombre de categoría") },
                    singleLine = true,
                    isError = showError,
                    supportingText = {
                        if (showError) {
                            Text("El nombre no puede estar vacío")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Seleccionar color:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categoryColorPresets.forEach { colorValue ->
                        val color = Color(colorValue)
                        val isSelected = selectedColor == colorValue
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (isSelected) {
                                        Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    } else Modifier
                                )
                                .clickable { selectedColor = colorValue }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        showError = true
                    } else {
                        onConfirm(category.copy(name = name.trim(), color = selectedColor))
                    }
                }
            ) {
                Text("Guardar Cambios")
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
fun EditCategoryDialogPreview() {
    LectorComicsyLibrosTheme {
        EditCategoryDialog(
            category = Category(id = 1, name = "Cómics", color = 0xFFFF5722),
            onDismiss = {},
            onConfirm = {}
        )
    }
}
