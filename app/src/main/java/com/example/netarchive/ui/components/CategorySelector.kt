package com.example.netarchive.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.netarchive.data.local.db.entity.CategoryEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    allCategories: List<CategoryEntity>,
    selectedCategories: List<CategoryEntity>,
    onCategoriesChanged: (List<CategoryEntity>) -> Unit,
    onCreateCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    var searchQuery by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }


    Column(modifier = modifier) {
        Text(
            text = "Категории:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            selectedCategories.forEach { category ->
                AssistChip(
                    onClick = {
                        val newList = selectedCategories - category
                        if (newList.isNotEmpty() || selectedCategories.size > 1) {
                            onCategoriesChanged(newList)
                        }
                    },
                    label = {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Удалить категорию",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (category.isDefault) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    expanded = it.isNotEmpty()
                },
                label = { Text("Добавить категорию") },
                placeholder = { Text("Начните вводить...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            expanded = false
                        }) {
                            Icon(Icons.Default.Clear, "Очистить")
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                val filteredCategories = allCategories.filter { category ->
                    category.name.contains(searchQuery, ignoreCase = true) &&
                            category !in selectedCategories
                }

                filteredCategories.take(5).forEach { category ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(text = category.name)
                                if (category.isDefault) {
                                    Text(
                                        text = "Стандартная",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        onClick = {
                            onCategoriesChanged(selectedCategories + category)
                            searchQuery = ""
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }

                if (searchQuery.isNotEmpty() &&
                    allCategories.none { it.name.equals(searchQuery, ignoreCase = true) }
                ) {
                    if (filteredCategories.isNotEmpty()) {
                        HorizontalDivider()
                    }

                    DropdownMenuItem(
                        text = { Text("Создать \"$searchQuery\"") },
                        onClick = {
                            onCreateCategory(searchQuery)
                            searchQuery = ""
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Add, "Создать")
                        }
                    )
                }

                if (filteredCategories.isEmpty() && searchQuery.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Начните вводить для поиска...") },
                        onClick = { },
                        enabled = false
                    )
                }
            }
        }
    }
}