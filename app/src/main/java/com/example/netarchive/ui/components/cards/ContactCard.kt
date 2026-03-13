package com.example.netarchive.ui.components.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.netarchive.domain.model.Contact
import com.example.netarchive.ui.theme.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChipDefaults

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
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
                    // аватарка
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
                // Имя контакта
                Text(
                    text = contact.username,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 🔥 Категории (если есть)
            if (categories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories.take(3)) { category ->  // Показываем максимум 3 категории
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1
                                )
                            },
                            leadingIcon = if (category.isDefault) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else {
                                null
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (category.isDefault) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                } else {
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                },
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = null,
                            enabled = false
                        )
                    }

                    if (categories.size > 3) {
                        item {
                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(
                                        text = "+${categories.size - 3}",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ),
                                border = null,
                                enabled = false
                            )
                        }
                    }
                }
            }

//            Column(
//                modifier = Modifier.fillMaxWidth(),
//                verticalArrangement = Arrangement.spacedBy(2.dp)
//            ) {
//                contact.phone?.takeIf { it.isNotBlank() }?.let { phone ->
//                    Text(
//                        text = phone,
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                }
//
//                contact.email?.takeIf { it.isNotBlank() }?.let { email ->
//                    Text(
//                        text = email,
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                }
//
//                contact.job?.takeIf { it.isNotBlank() }?.let { job ->
//                    Text(
//                        text = job,
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                }
//            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContactCardPreview() {
    NetArchiveTheme {
        ContactCard(
            contact = Contact(
                id = 1,
                username = "Иванов Иван",
                phone = null,
                telegram = null,
                max = null,
                email = null,
                job = null,
                avatar = null
            )
        )
    }
}