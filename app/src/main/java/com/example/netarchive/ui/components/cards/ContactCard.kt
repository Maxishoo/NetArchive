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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.netarchive.domain.model.Contact
import com.example.netarchive.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.AssistChipDefaults

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.netarchive.data.local.db.entity.CategoryEntity
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlin.math.abs
import com.example.netarchive.R

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

    val backgroundWidth = 160.dp
    val backgroundWidthPx = with(density) { backgroundWidth.toPx() }

    val swipeThreshold = 120.dp
    val swipeThresholdPx = with(density) { swipeThreshold.toPx() }
    var hasHorizontalDragThresholdGot by remember { mutableStateOf(false) }

    val offsetY = remember { Animatable(0f) }
    var hasVerticalDragThresholdGot by remember { mutableStateOf(false) }
    var isVerticalMove by remember { mutableStateOf(false) }

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

                        if (abs(newOffsetY) > 300f) {
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
                        scope.launch { offsetY.animateTo(targetValue = 0f, animationSpec = tween(200)) }
                    },
                )
            }
            .zIndex(if (isVerticalMove) 1f else 0f)
    } else {
        Modifier
    }

    Box(
        modifier = verticalDragModifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (contact.pinnedOrder > 0) "ОТКРЕПИТЬ" else "ЗАКРЕПИТЬ",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )
            Icon(
                painter = painterResource(
                    id = if (contact.pinnedOrder > 0)
                        R.drawable.unpinicon
                    else
                        R.drawable.pin_icon
                ),
                contentDescription = "Pin contact",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
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
                                    animationSpec = tween(durationMillis = 200)
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
                    if (abs(offsetX.value) < 5f) {
                        onClick()
                    }
                },
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (contact.avatar != null) {
                    AsyncImage(
                        model = contact.avatar,
                        contentDescription = "Avatar of ${contact.username}",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(color = Color(0xFFDBE0F7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contact.username.firstOrNull()?.uppercaseChar()?.toString()
                                ?: "?",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f)
                )  {
                    Text(
                        text = contact.username,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (categories.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            categories.take(2).forEach { category ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(text = "#${category.name}", style = MaterialTheme.typography.labelMedium, maxLines = 1) },
                                    modifier = Modifier
                                        .border(width = 1.dp, color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(10.dp))
                                        .heightIn(max = 20.dp),
                                    border = null,
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = Color.Transparent,
                                        labelColor = MaterialTheme.colorScheme.primary,
                                        disabledLabelColor = MaterialTheme.colorScheme.primary
                                    ),
                                    enabled = false,
                                )
                            }
                            if (categories.size > 2) {
                                AssistChip(
                                    onClick = { },
                                    label = { Text(text = "+${categories.size - 3}", style = MaterialTheme.typography.labelMedium, maxLines = 1) },
                                    modifier = Modifier
                                        .border(width = 1.dp, color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(10.dp))
                                        .heightIn(max = 20.dp),
                                    border = null,
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = Color.Transparent,
                                        labelColor = MaterialTheme.colorScheme.primary,
                                        disabledLabelColor = MaterialTheme.colorScheme.primary
                                    ),
                                    enabled = false,
                                )
                            }
                        }
                    }
                }
                if (contact.pinnedOrder > 0) {
                    Icon(
                        modifier = Modifier.size(30.dp),
                        painter = painterResource(
                            id = R.drawable.pin_icon
                        ),
                        contentDescription = "Pin contact",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Row(){
                        AnimatedVisibility(
                            visible = isVerticalMove,
                            enter = expandHorizontally(
                                expandFrom = Alignment.End,
                                animationSpec = tween(250)
                            ),
                            exit = shrinkHorizontally(
                                shrinkTowards = Alignment.End,
                                animationSpec = tween(200)
                            )
                        ) {
                            Icon(
                                modifier = Modifier.size(30.dp),
                                imageVector = Icons.Outlined.SwapVert,
                                contentDescription = "Swap",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}