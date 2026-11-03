package com.example.lectorcomicsylibros.ui.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lectorcomicsylibros.data.model.Category
import com.example.lectorcomicsylibros.ui.library.FormatFilterOption
import com.example.lectorcomicsylibros.ui.theme.LectorComicsyLibrosTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChipRow(
    selectedFormatFilter: FormatFilterOption,
    onFormatFilterSelected: (FormatFilterOption) -> Unit,
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    onAddCategoryClick: () -> Unit,
    modifier: Modifier = Modifier,
    onEditCategoryClick: (Category) -> Unit = {},
    onDeleteCategoryClick: (Category) -> Unit = {},
    onManageCategoriesClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Format Filters (Todos, Cómics, Libros)
        FormatFilterOption.entries.forEach { option ->
            val isSelected = selectedFormatFilter == option
            FilterChip(
                selected = isSelected,
                onClick = { onFormatFilterSelected(option) },
                label = { Text(option.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }

        VerticalDivider(
            modifier = Modifier
                .height(24.dp)
                .padding(horizontal = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // All Categories Chip
        FilterChip(
            selected = selectedCategoryId == null,
            onClick = { onCategorySelected(null) },
            label = { Text("Todas Categorías") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        )

        // User Categories Chips
        categories.forEach { category ->
            val isSelected = selectedCategoryId == category.id
            val categoryColor = Color(category.color)
            var showMenu by remember { mutableStateOf(false) }

            Box {
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelected(category.id) },
                    label = { Text(category.name) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(categoryColor)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = categoryColor.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.pointerInput(category.id) {
                        detectTapGestures(
                            onTap = { onCategorySelected(category.id) },
                            onLongPress = { showMenu = true }
                        )
                    }
                )

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar categoría") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onEditCategoryClick(category)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar categoría") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            showMenu = false
                            onDeleteCategoryClick(category)
                        }
                    )
                }
            }
        }

        // Add Category Button Chip
        InputChip(
            selected = false,
            onClick = onAddCategoryClick,
            label = { Text("Nueva Categoría") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar Categoría",
                    modifier = Modifier.size(16.dp)
                )
            }
        )

        // Manage Categories Button Chip
        InputChip(
            selected = false,
            onClick = onManageCategoriesClick,
            label = { Text("Gestionar") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Gestionar Categorías",
                    modifier = Modifier.size(16.dp)
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryChipRowPreview() {
    LectorComicsyLibrosTheme {
        CategoryChipRow(
            selectedFormatFilter = FormatFilterOption.ALL,
            onFormatFilterSelected = {},
            categories = listOf(
                Category(1, "Cómics", 0xFFFF5722),
                Category(2, "Libros", 0xFF2196F3)
            ),
            selectedCategoryId = 1,
            onCategorySelected = {},
            onAddCategoryClick = {},
            onEditCategoryClick = {},
            onDeleteCategoryClick = {},
            onManageCategoriesClick = {}
        )
    }
}
