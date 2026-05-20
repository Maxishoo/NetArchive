package com.example.netarchive.ui.components.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.netarchive.R
import com.example.netarchive.data.local.db.entity.CategoryEntity
import com.example.netarchive.domain.model.Contact
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun ContactCard(
    contact: Contact,
    categories: List<CategoryEntity> = emptyList(),
    onClick: () -> Unit = {},
    onDragSwipeThreshold: () -> Unit,
    onDragEnd: () -> Unit = {},
    onVerticalDragStart: () -> Unit = {},
    onVerticalDragEnd: (_: Boolean) -> Unit = {}
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val backgroundWidth = dimensionResource(id = R.dimen.contact_card_background_width)
    val backgroundWidthPx = with(density) { backgroundWidth.toPx() }

    val swipeThreshold = dimensionResource(id = R.dimen.contact_card_swipe_threshold)
    val swipeThresholdPx = with(density) { swipeThreshold.toPx() }
    var hasHorizontalDragThresholdGot by remember { mutableStateOf(false) }

    val offsetY = remember { Animatable(0f) }
    val verticalDragThreshold = dimensionResource(id = R.dimen.contact_card_vertical_drag_threshold)
    val verticalDragThresholdPx = with(density) { verticalDragThreshold.toPx() }
    var hasVerticalDragThresholdGot by remember { mutableStateOf(false) }
    var isVerticalMove by remember { mutableStateOf(false) }
    val clickThresholdPx = with(density) {
        dimensionResource(id = R.dimen.contact_card_click_threshold).toPx()
    }
    val verticalDragModifier = if (contact.pinnedOrder > 0) {
        Modifier
            .offset { IntOffset(0, offsetY.value.toInt()) }
            .pointerInput(contact.id, contact.pinnedOrder) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        hasVerticalDragThresholdGot = false
                        isVerticalMove = true
                        scope.launch { offsetY.snapTo(0f) }
                        onVerticalDragStart()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (hasVerticalDragThresholdGot) return@detectDragGesturesAfterLongPress

                        val newOffsetY = offsetY.value + dragAmount.y

                        if (abs(newOffsetY) > verticalDragThresholdPx) {
                            hasVerticalDragThresholdGot = true
                            isVerticalMove = false
                            onVerticalDragEnd(newOffsetY > 0f)
                            scope.launch { offsetY.snapTo(0f) }
                        } else {
                            scope.launch { offsetY.snapTo(newOffsetY) }
                        }
                    },
                    onDragEnd = {
                        hasVerticalDragThresholdGot = false
                        isVerticalMove = false
                        scope.launch {
                            offsetY.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(durationMillis = R.integer.animation_duration_short)
                            )
                        }
                    }
                )
            }
            .zIndex(if (isVerticalMove) 1f else 0f)
    } else {
        Modifier
    }

    Box(modifier = verticalDragModifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.contact_card_height))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(end = dimensionResource(id = R.dimen.card_padding)),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (contact.pinnedOrder > 0)
                    stringResource(R.string.action_unpin)
                else
                    stringResource(R.string.action_pin),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = dimensionResource(id = R.dimen.padding_small))
            )
            Icon(
                painter = painterResource(
                    id = if (contact.pinnedOrder > 0)
                        R.drawable.unpinicon
                    else
                        R.drawable.pin_icon
                ),
                contentDescription = stringResource(R.string.content_description_pin),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_medium))
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.toInt(), 0) }
                .pointerInput(contact.id, contact.pinnedOrder) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (abs(offsetX.value) >= swipeThresholdPx) {
                                    onDragEnd()
                                }
                                hasHorizontalDragThresholdGot = false
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(durationMillis = R.integer.animation_duration_short)
                                )
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()

                            val newOffset = (offsetX.value + dragAmount).coerceIn(
                                minimumValue = -backgroundWidthPx,
                                maximumValue = 0f
                            )

                            if (!hasHorizontalDragThresholdGot && abs(newOffset) >= swipeThresholdPx) {
                                onDragSwipeThreshold()
                                hasHorizontalDragThresholdGot = true
                            }

                            scope.launch {
                                offsetX.snapTo(newOffset)
                            }
                        }
                    )
                }
                .clickable {
                    if (abs(offsetX.value) <  clickThresholdPx) {
                        onClick()
                    }
                },
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_zero)),
            colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.card_background)),
            elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.contact_card_elevation))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.card_padding)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_medium))
            ) {
                if (contact.avatar != null) {
                    AsyncImage(
                        model = contact.avatar,
                        contentDescription = stringResource(R.string.contact_avatar_description, contact.username),
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.contact_avatar_size))
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.contact_avatar_size))
                            .clip(CircleShape)
                            .background(color = colorResource(id = R.color.reminder_contact_avatar_placeholder)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contact.username.firstOrNull()?.uppercaseChar()?.toString()
                                ?: stringResource(R.string.default_avatar),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = contact.username,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = dimensionResource(id = R.dimen.contact_card_name_font_size).value.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (categories.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.category_chip_spacing)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categories.take(2).forEach { category ->
                                AssistChip(
                                    onClick = { },
                                    label = {
                                        Text(
                                            text = stringResource(R.string.category_hash) + category.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 1
                                        )
                                    },
                                    modifier = Modifier
                                        .border(
                                            width = dimensionResource(id = R.dimen.chip_border_width),
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.chip_corner_radius))
                                        )
                                        .heightIn(max = dimensionResource(id = R.dimen.chip_height_max)),
                                    border = null,
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = colorResource(id = R.color.transparent),
                                        labelColor = MaterialTheme.colorScheme.primary,
                                        disabledLabelColor = MaterialTheme.colorScheme.primary
                                    ),
                                    enabled = false
                                )
                            }
                            // ✅ Исправлена логика: было (size - 3), стало (size - 2), так как показано 2 элемента
                            if (categories.size > 2) {
                                AssistChip(
                                    onClick = { },
                                    label = {
                                        Text(
                                            text = "+${categories.size - 2}",
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 1
                                        )
                                    },
                                    modifier = Modifier
                                        .border(
                                            width = dimensionResource(id = R.dimen.chip_border_width),
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.chip_corner_radius))
                                        )
                                        .heightIn(max = dimensionResource(id = R.dimen.chip_height_max)),
                                    border = null,
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = colorResource(id = R.color.transparent),
                                        labelColor = MaterialTheme.colorScheme.primary,
                                        disabledLabelColor = MaterialTheme.colorScheme.primary
                                    ),
                                    enabled = false
                                )
                            }
                        }
                    }
                }
                if (contact.pinnedOrder > 0) {
                    Icon(
                        modifier = Modifier.size(dimensionResource(id = R.dimen.contact_card_icon_size)),
                        painter = painterResource(
                            id = R.drawable.pin_icon
                        ),
                        contentDescription = stringResource(R.string.content_description_pin),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Row {
                        AnimatedVisibility(
                            visible = isVerticalMove,
                            enter = expandHorizontally(
                                expandFrom = Alignment.End,
                                animationSpec = tween(R.integer.animation_duration_medium)
                            ),
                            exit = shrinkHorizontally(
                                shrinkTowards = Alignment.End,
                                animationSpec = tween(durationMillis = R.integer.animation_duration_short)
                            )
                        ) {
                            Icon(
                                modifier = Modifier.size(dimensionResource(id = R.dimen.contact_card_icon_size)),
                                imageVector = Icons.Outlined.SwapVert,
                                contentDescription = stringResource(R.string.content_description_swap),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Dp.toPx() {
    TODO("Not yet implemented")
}
