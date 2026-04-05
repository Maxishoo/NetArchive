package com.example.netarchive.ui.screens.analytics_screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.netarchive.data.local.db.dao.CategoryWithCount
import com.example.netarchive.domain.model.Contact
import com.example.netarchive.domain.model.OverallStats

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel(),
    onContactClick: (Int) -> Unit = {}
) {
    // 🔄 Собираем каждый Flow отдельно
    val overallStats by viewModel.overallStats.collectAsState()
    val topContacts by viewModel.topContacts.collectAsState()
    val contactsToWrite by viewModel.contactsToWrite.collectAsState()
    val forgottenContacts by viewModel.forgottenContacts.collectAsState()
    val categories by viewModel.categoriesWithCount.collectAsState()
    val monthlyActivity by viewModel.monthlyActivity.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 140.dp)
    ) {
        item {
            Text(
                text = "📊 Аналитика",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            OverallStatsCard(stats = overallStats)
        }


        item {
            SectionTitle("🔥 Топ контактов")
            if (topContacts.isEmpty()) {
                EmptyStateText("Пока нет данных")
            } else {
                topContacts.take(5).forEach { contact ->
                    ContactListItem(contact = contact,onClick = { onContactClick(contact.id) })
                }
            }
        }

        item {
            var showAllWriteSoon by remember { mutableStateOf(false) }

            SectionTitle("💬 Пора написать!")

            if (contactsToWrite.isEmpty()) {
                EmptyStateText("Все контакты в порядке! 🎉")
            } else {
                val displayedList = if (showAllWriteSoon) contactsToWrite else contactsToWrite.take(5)

                displayedList.forEach { contact ->
                    ContactListItem(
                        contact = contact,
                        highlight = true,
                        onClick = { onContactClick(contact.id) }
                    )
                }

                if (contactsToWrite.size > 5) {
                    TextButton(
                        onClick = { showAllWriteSoon = !showAllWriteSoon },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (showAllWriteSoon) "Свернуть ▲"
                            else "Показать всех (${contactsToWrite.size - 5} ещё) ▼",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        item {
            SectionTitle("📁 Распределение по категориям")
            CategoryPieChart(categories = categories)
        }
    }
}


@Composable
fun OverallStatsCard(stats: OverallStats?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("📈 Общая статистика", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                StatItem("Всего", stats?.totalContacts?.toString() ?: "–")
                StatItem("Новых за мес.", stats?.newContactsThisMonth?.toString() ?: "–")
                StatItem("Активных", "${stats?.activeContactsPercent?.toInt() ?: 0}%")
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
            color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(label, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
fun EmptyStateText(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        modifier = Modifier.padding(vertical = 12.dp))
}

@Composable
fun ContactListItem(
    contact: Contact,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight)
                MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Аватар-заглушка
            if (contact.avatar != null) {
                AsyncImage(
                    model = contact.avatar,
                    contentDescription = "Avatar of ${contact.username}",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
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
            Column (modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.username,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (highlight) FontWeight.SemiBold else FontWeight.Normal
                )
                if (highlight) {
                    Text(
                        text = "Давно не общались",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}


@Composable
fun CategoryPieChart(categories: List<CategoryWithCount>) {
    if (categories.isEmpty()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.padding(24.dp)) {
                EmptyStateText("Нет данных для графика")
            }
        }
        return
    }

    val total = categories.sumOf { it.contactCount.toDouble() }
    if (total == 0.0) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.padding(24.dp)) {
                EmptyStateText("Нет контактов в категориях")
            }
        }
        return
    }

    val palette = listOf(
        Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFFF9800),
        Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF00BCD4),
        Color(0xFF795548), Color(0xFF607D8B)
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                var startAngle = 0f
                categories.forEachIndexed { index, cat ->
                    val sweepAngle = (cat.contactCount / total * 360).toFloat()
                    drawArc(
                        color = palette[index % palette.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true
                    )
                    startAngle += sweepAngle
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            categories.forEachIndexed { index, cat ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(palette[index % palette.size], RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${cat.name} (${cat.contactCount})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}