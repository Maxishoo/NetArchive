package com.example.netarchive.ui.screens.reminder_list_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.netarchive.R
import com.example.netarchive.domain.model.ReminderContact
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeApi::class)
@Composable
fun ReminderListScreen(
    modifier: Modifier = Modifier,
    viewModel: ReminderListViewModel = hiltViewModel(),
    onReminderClick: (ReminderContact) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sortingMode by viewModel.sortingMode.collectAsStateWithLifecycle()

    var selectionMode by remember { mutableStateOf(false) }
    val selectedReminderIds = remember { mutableStateSetOf<Int>() }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Режим группировки по контакту: теперь переключается (true/false)
    var groupByContact by remember { mutableStateOf(false) }
    val deleteMessage = stringResource(R.string.reminders_deleted)

    Scaffold(
        topBar = {
            ReminderListTopBar(
                isSelectionMode = selectionMode,
                selectedCount = selectedReminderIds.size,
                onCloseSelection = {
                    selectionMode = false
                    selectedReminderIds.clear()
                },
                onDeleteSelected = {
                    scope.launch {
                        viewModel.deleteReminders(selectedReminderIds.toList())
                        selectionMode = false
                        selectedReminderIds.clear()
                        snackbarHostState.showSnackbar(deleteMessage)
                    }
                },
                onSortClick = { groupByContact = !groupByContact }, // переключение группировки
                onDeleteModeClick = { selectionMode = true }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = modifier.fillMaxSize().padding(paddingValues)) {
            when (state) {
                is LoadState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is LoadState.Error -> {
                    Text(
                        text = stringResource(R.string.error_reminders_load),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is LoadState.Empty -> {
                    Text(
                        text = stringResource(R.string.reminders_empty),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is LoadState.Success -> {
                    val reminders = (state as LoadState.Success<List<ReminderContact>>).data
                    if (groupByContact) {
                        // Группированное отображение
                        GroupedRemindersList(
                            reminders = reminders,
                            selectionMode = selectionMode,
                            selectedIds = selectedReminderIds,
                            onReminderClick = { reminderWithContact ->
                                if (selectionMode) {
                                    val id = reminderWithContact.reminder.id
                                    if (selectedReminderIds.contains(id)) {
                                        selectedReminderIds.remove(id)
                                    } else {
                                        selectedReminderIds.add(id)
                                    }
                                } else {
                                    onReminderClick(reminderWithContact)
                                }
                            }
                        )
                    } else {
                        // Обычный плоский список
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(reminders, key = { it.reminder.id }) { reminderWithContact ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(5.dp)
                                        ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column {
                                        ContactHeader(contact = reminderWithContact.contact)
                                        ReminderCard(
                                            reminderWithContact = reminderWithContact,
                                            onClick = {
                                                if (selectionMode) {
                                                    val id = reminderWithContact.reminder.id
                                                    if (selectedReminderIds.contains(id)) selectedReminderIds.remove(id)
                                                    else selectedReminderIds.add(id)
                                                } else {
                                                    onReminderClick(reminderWithContact)
                                                }
                                            },
                                            isSelectionMode = selectionMode,
                                            isSelected = reminderWithContact.reminder.id in selectedReminderIds
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                    }
                }
                }
            }
        }
    }

/**
 * Список напоминаний, сгруппированный по контакту.
 * Каждая группа начинается с заголовка (аватарка + имя контакта),
 * затем идут карточки напоминаний этого контакта.
 */
@Composable
private fun GroupedRemindersList(
    reminders: List<ReminderContact>,
    selectionMode: Boolean,
    selectedIds: Set<Int>,
    onReminderClick: (ReminderContact) -> Unit
) {
    val grouped = remember(reminders) {
        reminders.groupBy { it.contact?.id ?: Int.MIN_VALUE }
            .map { (_, items) -> GroupedData(items.firstOrNull()?.contact, items) }
            .sortedBy { it.contact?.username?.lowercase() ?: "" }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        grouped.forEach { group ->
            item(key = "group_${group.contact?.id ?: "no_contact"}") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(5.dp)
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column {
                        ContactHeader(contact = group.contact)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            group.items.forEach { reminderWithContact ->
                                ReminderCard(
                                    reminderWithContact = reminderWithContact,
                                    onClick = { onReminderClick(reminderWithContact) },
                                    isSelectionMode = selectionMode,
                                    isSelected = reminderWithContact.reminder.id in selectedIds
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

/**
 * Заголовок контакта: аватарка (иконка или заглушка) и имя.
 */
@Composable
private fun ContactHeader(contact: com.example.netarchive.domain.model.Contact?) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically

        ) {
            if (contact?.avatar != null) {
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
                        text = contact?.username?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = contact?.username ?: "?",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
//                        modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
}

/**
 * Вспомогательный класс для хранения сгруппированных данных.
 */
private data class GroupedData(
    val contact: com.example.netarchive.domain.model.Contact?,
    val items: List<ReminderContact>
)


@Composable
fun ReminderCard(
    reminderWithContact: ReminderContact,
    onClick: () -> Unit,
    isSelectionMode: Boolean,
    isSelected: Boolean
) {
    val reminder = reminderWithContact.reminder
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(reminder.date)
    val isOverdue = reminder.date < System.currentTimeMillis()

    val cardColors = when {
        isSelected -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
        isOverdue -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
        else -> CardDefaults.cardColors()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = cardColors
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall
                )
                reminder.text?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListTopBar(
    isSelectionMode: Boolean,
    selectedCount: Int,
    onCloseSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    onSortClick: () -> Unit,
    onDeleteModeClick: () -> Unit
) {
    if (isSelectionMode) {
        TopAppBar(
            title = { Text("$selectedCount ${stringResource(R.string.selected)}") },
            navigationIcon = {
                IconButton(onClick = onCloseSelection) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                }
            },
            actions = {
                IconButton(onClick = onDeleteSelected) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
        )
    } else {
        TopAppBar(
            title = { Text(stringResource(R.string.reminders_title), style = MaterialTheme.typography.headlineLarge) },
            actions = {
                IconButton(onClick = onSortClick) {
                    Icon(Icons.Default.AccountBox, contentDescription = stringResource(R.string.sort_by_contact_then_date))
                }
                IconButton(onClick = onDeleteModeClick) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
        )
    }
}