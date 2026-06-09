package com.example.netarchive.ui.screens.contacts_list_screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.netarchive.R
import com.example.netarchive.data.local.db.entity.CategoryEntity
import com.example.netarchive.ui.theme.AppTheme
import com.example.netarchive.ui.theme.LightBlue
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.runtime.MutableState
import com.example.netarchive.utils.rememberDefaultVibrator
import com.example.netarchive.utils.vibrateOneShotShort

@Composable
fun ContactsTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isSelectionMode: Boolean = false,
    allCategories: List<CategoryEntity> = emptyList(),
    selectedCategoryId: Int? = null,
    onCategoryFilterSelected: (Int?) -> Unit = {},
    showSearchFieldState: MutableState<Boolean>,
    onReminderClick: () -> Unit
) {
    var showSearchField by showSearchFieldState

    val vibrator = rememberDefaultVibrator()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = AppTheme.topBarBackground())
            .padding(top = 35.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            AnimatedContent(
                targetState = showSearchField,
                transitionSpec = {
                    if (targetState) {
                        slideInVertically(initialOffsetY = { it }) + fadeIn() togetherWith
                                slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                    } else {
                        slideInVertically(initialOffsetY = { -it }) + fadeIn() togetherWith
                                slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    }
                },
                label = "searchBarAnimation"
            ) { targetState ->
                if (targetState) {
                    Column {
                        TextField(
                            value = query,
                            onValueChange = onQueryChange,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = stringResource(R.string.search)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    vibrator.vibrateOneShotShort()
                                    showSearchField = false
                                    onQueryChange("")
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = stringResource(R.string.close)
                                    )
                                }
                            },
                            placeholder = { Text(stringResource(R.string.search_contacts_placeholder)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = LightBlue,
                                unfocusedContainerColor = LightBlue,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        if (allCategories.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedCategoryId == null,
                                        onClick = {
                                            onCategoryFilterSelected(null)
                                                  },
                                        label = { Text(stringResource(R.string.filter_all)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }

                                items(allCategories) { category ->
                                    FilterChip(
                                        selected = selectedCategoryId == category.id,
                                        onClick = {
                                            onCategoryFilterSelected(category.id)
                                                  },
                                        label = { Text(category.name) },
                                        leadingIcon = if (category.isDefault) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Outlined.Star,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        } else {
                                            null
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }


                } else {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isSelectionMode) stringResource(R.string.select_contact_title) else stringResource(R.string.contacts_title),
                            style = MaterialTheme.typography.headlineLarge,
                            color = AppTheme.colors.onSurface
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    vibrator.vibrateOneShotShort()
                                    showSearchField = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = stringResource(R.string.search),
                                    tint = AppTheme.colors.onSurface
                                )
                            }
                        }
                    }

                }

            }
        }
    }
}