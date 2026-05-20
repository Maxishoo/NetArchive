package com.example.netarchive.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.example.netarchive.R
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
            text = stringResource(R.string.categories_label),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_small)))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_extra_small)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_extra_small)),
            modifier = Modifier.fillMaxWidth()
        ) {
            selectedCategories.forEach { category ->
                AssistChip(
                    onClick = {
                        val newList = selectedCategories - category
                        onCategoriesChanged(newList)
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
                            contentDescription = stringResource(R.string.category_remove),
                            modifier = Modifier.size(dimensionResource(id = R.dimen.category_chip_icon_size))
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

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_medium)))

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
                label = { Text(stringResource(R.string.category_add)) },
                placeholder = { Text(stringResource(R.string.category_search_hint)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            expanded = false
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.clear)
                            )
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
                                        text = stringResource(R.string.category_default),
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
                        text = { Text(stringResource(R.string.category_create, searchQuery)) },
                        onClick = {
                            onCreateCategory(searchQuery)
                            searchQuery = ""
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.create))
                        }
                    )
                }

                if (filteredCategories.isEmpty() && searchQuery.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.category_search_empty)) },
                        onClick = { },
                        enabled = false
                    )
                }
            }
        }
    }
}