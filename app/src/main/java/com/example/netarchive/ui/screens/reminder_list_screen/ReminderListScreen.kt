package com.example.netarchive.ui.screens.reminder_list_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.GroupOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.netarchive.R
import com.example.netarchive.domain.model.ReminderContact
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeApi::class)
@Suppress("UNUSED_PARAMETER")
@Composable
fun ReminderListScreen(
    modifier: Modifier = Modifier,
    viewModel: ReminderListViewModel = hiltViewModel(),
    onReminderClick: (ReminderContact) -> Unit = {},
    onBackClick: () -> Boolean
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var selectionMode by remember { mutableStateOf(false) }
    val selectedReminderIds = remember { mutableStateSetOf<Int>() }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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
                onSortClick = { groupByContact = !groupByContact },
                onDeleteModeClick = { selectionMode = true },
                groupByContact
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
        ) {
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
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = paddingValues.calculateTopPadding() + dimensionResource(id = R.dimen.reminder_list_top_bar_offset)
                            ),
                            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.reminder_card_spacing))
                        ) {
                            items(reminders, key = { it.reminder.id }) { reminderWithContact ->
                                Card(
                                    modifier = Modifier.fillMaxSize(),
                                    elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.reminder_card_spacing)),
                                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_small)),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(Modifier.padding(horizontal = dimensionResource(id = R.dimen.reminder_card_horizontal_padding))) {
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
                                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.reminder_card_vertical_padding)))
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_large))) }
                        }
                    }
                }
            }
        }
    }
}

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
        contentPadding = PaddingValues(
            top = dimensionResource(id = R.dimen.padding_small)
        ),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_small))
    ) {
        grouped.forEach { group ->
            item(key = "group_${group.contact?.id ?: "no_contact"}") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.reminder_card_spacing)),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(Modifier.padding(horizontal = dimensionResource(id = R.dimen.reminder_card_horizontal_padding))) {
                        ContactHeader(contact = group.contact)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_small))
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
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.reminder_card_vertical_padding)))
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.reminder_list_bottom_spacer))) }
    }
}

@Composable
private fun ContactHeader(contact: com.example.netarchive.domain.model.Contact?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(id = R.dimen.reminder_contact_avatar_spacing)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (contact?.avatar != null) {
            AsyncImage(
                model = contact.avatar,
                contentDescription = stringResource(R.string.reminder_avatar_description, contact.username),
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.reminder_contact_avatar_size))
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.reminder_contact_avatar_size))
                    .clip(CircleShape)
                    .background(color = colorResource(id = R.color.reminder_contact_avatar_placeholder)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact?.username?.firstOrNull()?.uppercaseChar()?.toString() ?: stringResource(R.string.reminder_unknown),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.reminder_contact_avatar_spacing)))
        Text(
            text = contact?.username ?: stringResource(R.string.reminder_unknown),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = dimensionResource(id = R.dimen.reminder_contact_name_font_size).value.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

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
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = cardColors,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.reminder_card_corner_radius))
    ) {
        Row(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.card_padding)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.size(dimensionResource(id = R.dimen.reminder_checkbox_size))
                )
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_small)))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (isOverdue) {
                        Text(
                            text = stringResource(R.string.reminder_overdue),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                reminder.text.let {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_small)))
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
    onDeleteModeClick: () -> Unit,
    groupByContact: Boolean
) {
    if (isSelectionMode) {
        TopAppBar(
            title = { Text("$selectedCount ${stringResource(R.string.selected)}") },
            navigationIcon = {
                IconButton(onClick = onCloseSelection) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorResource(id = R.color.top_bar_background).copy(alpha = 0.95f)
            ),
            actions = {
                IconButton(onClick = onDeleteSelected) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
        )
    } else {
        TopAppBar(
            title = {
                Text(
                    stringResource(R.string.reminders_title),
                    style = MaterialTheme.typography.headlineLarge
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorResource(id = R.color.top_bar_background).copy(alpha = 0.95f)
            ),
            actions = {
                IconButton(onClick = onSortClick) {
                    Icon(
                        if (groupByContact) Icons.Outlined.GroupOff else Icons.Outlined.Group,
                        contentDescription = stringResource(R.string.sort_by_contact_then_date)
                    )
                }
                IconButton(onClick = onDeleteModeClick) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
        )
    }
}