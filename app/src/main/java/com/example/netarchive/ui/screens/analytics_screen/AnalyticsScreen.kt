package com.example.netarchive.ui.screens.analytics_screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.netarchive.R
import com.example.netarchive.data.local.db.dao.CategoryWithCount
import com.example.netarchive.domain.model.Contact
import com.example.netarchive.domain.model.OverallStats
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel(),
    onContactClick: (Int) -> Unit = {}
) {
    val overallStats by viewModel.overallStats.collectAsState()
    val topContacts by viewModel.topContacts.collectAsState()
    val contactsToWrite by viewModel.contactsToWrite.collectAsState()
    val forgottenContacts by viewModel.forgottenContacts.collectAsState()
    val categories by viewModel.categoriesWithCount.collectAsState()
    val monthlyActivity by viewModel.monthlyActivity.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.analytics_title),
                        style = MaterialTheme.typography.headlineLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.top_bar_background).copy(alpha = 0.95f)
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_horizontal_screen)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_large)),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + dimensionResource(id = R.dimen.analytics_content_padding_top_offset),
                bottom = dimensionResource(id = R.dimen.analytics_content_padding_bottom)
            )
        ) {
            item {
                SectionTitle(stringResource(R.string.section_overall_stats))
                OverallStatsCard(stats = overallStats)
            }
            item {
                SectionTitle(stringResource(R.string.section_top_contacts))
                if (topContacts.isEmpty()) {
                    EmptyStateText(stringResource(R.string.empty_no_data))
                } else {
                    topContacts.take(5).forEach { contact ->
                        ContactListItem(contact = contact, onClick = { onContactClick(contact.id) })
                    }
                }
            }
            item {
                var showAllWriteSoon by remember { mutableStateOf(false) }
                SectionTitle(stringResource(R.string.section_write_soon))
                if (contactsToWrite.isEmpty()) {
                    EmptyStateText(stringResource(R.string.empty_all_ok))
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
                                text = if (showAllWriteSoon) stringResource(R.string.collapse)
                                else stringResource(R.string.show_all, contactsToWrite.size - 5),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            item {
                SectionTitle(stringResource(R.string.section_categories))
                CategoryPieChart(categories = categories)
            }
        }
    }
}

@Composable
fun OverallStatsCard(stats: OverallStats?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.card_background))
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_medium))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(stringResource(R.string.stat_total), stats?.totalContacts?.toString() ?: "–")
                StatItem(stringResource(R.string.stat_new_this_month), stats?.newContactsThisMonth?.toString() ?: "–")
                StatItem(stringResource(R.string.stat_active), "${stats?.activeContactsPercent?.toInt() ?: 0}%")
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = dimensionResource(id = R.dimen.text_headline).value.sp
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            label, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.analytics_section_padding_vertical))
    )
}

@Composable
fun EmptyStateText(text: String) {
    Text(
        text, style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.analytics_empty_padding_vertical))
    )
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
            .padding(vertical = dimensionResource(id = R.dimen.spacing_extra_small))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.card_background)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensionResource(id = R.dimen.analytics_contact_padding_horizontal),
                    vertical = dimensionResource(id = R.dimen.analytics_contact_padding_vertical)
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_medium))
        ) {
            if (contact.avatar != null) {
                AsyncImage(
                    model = contact.avatar,
                    contentDescription = stringResource(R.string.contact_avatar_description, contact.username),
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.analytics_contact_avatar_size))
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.analytics_contact_avatar_size))
                        .clip(CircleShape)
                        .background(color = colorResource(id = R.color.avatar_placeholder_background)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.username.firstOrNull()?.uppercaseChar()?.toString() ?: stringResource(R.string.default_avatar),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.username,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (highlight) {
                    Text(
                        text = stringResource(R.string.contact_long_not_written),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorResource(id = R.color.highlight_text)
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
            Box(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                EmptyStateText(stringResource(R.string.empty_no_chart_data))
            }
        }
        return
    }
    val total = categories.sumOf { it.contactCount.toDouble() }
    if (total == 0.0) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
                EmptyStateText(stringResource(R.string.empty_no_categories))
            }
        }
        return
    }
    val palette = listOf(
        colorResource(id = R.color.chart_green),
        colorResource(id = R.color.chart_blue),
        colorResource(id = R.color.chart_orange),
        colorResource(id = R.color.chart_pink),
        colorResource(id = R.color.chart_purple),
        colorResource(id = R.color.chart_cyan),
        colorResource(id = R.color.chart_brown),
        colorResource(id = R.color.chart_gray)
    )
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Canvas(
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.analytics_chart_size))
                    .padding(bottom = dimensionResource(id = R.dimen.analytics_chart_padding_bottom))
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.analytics_legend_padding_horizontal)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                categories.chunked(2).forEachIndexed { rowIndex, rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_large)),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        rowItems.forEach { cat ->
                            val colorIndex = (rowIndex * 2 + rowItems.indexOf(cat)) % palette.size
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(dimensionResource(id = R.dimen.analytics_legend_box_size))
                                        .background(
                                            palette[colorIndex],
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_small)))
                                Text(
                                    text = "${cat.name} (${cat.contactCount})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_small)))
                }
            }
        }
    }
}