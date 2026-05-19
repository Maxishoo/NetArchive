package com.example.netarchive.ui.screens.contacts_list_screen

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import com.example.netarchive.R
import com.example.netarchive.data.local.db.entity.CategoryEntity

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

    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(id = R.color.top_bar_background).copy(alpha = 0.95f)
            )
            .padding(
                top = dimensionResource(id = R.dimen.top_bar_padding_top),
                bottom = dimensionResource(id = R.dimen.top_bar_padding_bottom),
                start = dimensionResource(id = R.dimen.padding_horizontal_screen),
                end = dimensionResource(id = R.dimen.padding_horizontal_screen)
            )
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
                                    contentDescription = stringResource(R.string.search_hint)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (vibrator.hasVibrator()) {
                                        vibrator.vibrate(
                                            VibrationEffect.createOneShot(
                                                R.integer.vibration_duration.toLong(),
                                                VibrationEffect.DEFAULT_AMPLITUDE
                                            )
                                        )
                                    }
                                    showSearchField = false
                                    onQueryChange("")
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = stringResource(R.string.close)
                                    )
                                }
                            },
                            placeholder = { Text(stringResource(R.string.search_hint)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.search_field_corner_radius)),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = colorResource(id = R.color.search_background),
                                unfocusedContainerColor = colorResource(id = R.color.search_background),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        if (allCategories.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_small)))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_small)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedCategoryId == null,
                                        onClick = { onCategoryFilterSelected(null) },
                                        label = { Text(stringResource(R.string.all_categories)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }

                                items(allCategories) { category ->
                                    FilterChip(
                                        selected = selectedCategoryId == category.id,
                                        onClick = { onCategoryFilterSelected(category.id) },
                                        label = { Text(category.name) },
                                        leadingIcon = if (category.isDefault) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Outlined.Star,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_small))
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
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_small)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (vibrator.hasVibrator()) {
                                        vibrator.vibrate(
                                            VibrationEffect.createOneShot(
                                                R.integer.vibration_duration.toLong(),
                                                VibrationEffect.DEFAULT_AMPLITUDE
                                            )
                                        )
                                    }
                                    showSearchField = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = stringResource(R.string.search_icon),
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}