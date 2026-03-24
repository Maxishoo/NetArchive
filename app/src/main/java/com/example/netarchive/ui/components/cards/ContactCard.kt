package com.example.netarchive.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.netarchive.domain.model.Contact
import com.example.netarchive.ui.theme.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChipDefaults

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.netarchive.data.local.db.entity.CategoryEntity
import coil.compose.AsyncImage

@Composable
fun ContactCard(
    contact: Contact,
    modifier: Modifier = Modifier,
    categories: List<CategoryEntity> = emptyList(),
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Аватар или инициалы
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
                            text = contact.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
                Column{
                    // Имя контакта
                    Text(
                        text = contact.username,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
//                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // 🔥 Категории (если есть)
                    if (categories.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categories.take(3)) { category ->  // Показываем максимум 3 категории
                                AssistChip(
                                    onClick = { },
                                    label = {
                                        Text(
                                            text = "#${category.name}",
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 1
                                        )
                                    },
                                    modifier = Modifier
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .heightIn(max = 20.dp),
                                    border = null,
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = Color.Transparent, // или любой другой цвет фона
                                        labelColor = MaterialTheme.colorScheme.primary, // яркий цвет для текста
                                        disabledLabelColor = MaterialTheme.colorScheme.primary // для отключенного состояния
                                    ),
                                    enabled = false,
                                )
                            }

                            if (categories.size > 3) {
                                item {
                                    AssistChip(
                                        onClick = { },
                                        label = {
                                            Text(
                                                text = "+${categories.size - 3}",
                                                style = MaterialTheme.typography.labelMedium,
                                                maxLines = 1
                                            )
                                        },
                                        modifier = Modifier
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .heightIn(max = 20.dp),
                                        border = null,
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = Color.Transparent, // или любой другой цвет фона
                                            labelColor = MaterialTheme.colorScheme.primary, // яркий цвет для текста
                                            disabledLabelColor = MaterialTheme.colorScheme.primary // для отключенного состояния
                                        ),
                                        enabled = false,
                                    )
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}