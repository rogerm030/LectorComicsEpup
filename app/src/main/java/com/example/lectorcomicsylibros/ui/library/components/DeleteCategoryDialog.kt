package com.example.lectorcomicsylibros.ui.library.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.lectorcomicsylibros.data.model.Category
import com.example.lectorcomicsylibros.ui.theme.LectorComicsyLibrosTheme

@Composable
fun DeleteCategoryDialog(
    category: Category,
    onDismiss: () -> Unit,
    onConfirm: (Category) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "¿Eliminar categoría?",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                text = "¿Eliminar la categoría '${category.name}'? Los cómics/libros asignados no se borrarán, solo perderán esta categoría.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(category) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Eliminar")
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
fun DeleteCategoryDialogPreview() {
    LectorComicsyLibrosTheme {
        DeleteCategoryDialog(
            category = Category(id = 1, name = "Manga", color = 0xFFFF5722),
            onDismiss = {},
            onConfirm = {}
        )
    }
}
