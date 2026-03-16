package com.example.netarchive.ui.screens.reminder_list_screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.netarchive.domain.model.Note
import com.example.netarchive.ui.components.cards.NoteCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    viewModel: NotesViewModel = hiltViewModel(),
    contactId: Int? = null,
    onNoteClick: (Int) -> Unit = {},
    onAddNoteClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    LaunchedEffect(contactId) {
        viewModel.loadNotes(contactId)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            snackbarHostState.showSnackbar(
                message = errorMessage,
                actionLabel = "OK",
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            NotesListTopBar(
                isEditMode = uiState.isEditMode,
                hasActiveFilter = uiState.selectedContactId != null,
                onEditModeToggle = viewModel::toggleEditMode,
                onBackClick = if (uiState.selectedContactId != null) {
                    {
                        viewModel.clearContactFilter()
                        onBackClick?.invoke()
                    }
                } else null,
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> LoadingIndicator()

                uiState.notes.isEmpty() -> EmptyNotesContent(
                    contactId = uiState.selectedContactId,
                    onAddClick = onAddNoteClick
                )

                else -> NotesList(
                    notes = uiState.notes,
                    isEditMode = uiState.isEditMode,
                    onNoteClick = onNoteClick,
                    onDeleteClick = viewModel::deleteNote,
                    scrollBehavior = scrollBehavior
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesListTopBar(
    isEditMode: Boolean,
    hasActiveFilter: Boolean,
    onEditModeToggle: () -> Unit,
    onBackClick: (() -> Unit)?,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = {
            Text(
                text = if (hasActiveFilter) "Заметки контакта" else "Все заметки"
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад"
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onEditModeToggle) {
                Icon(
                    imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = if (isEditMode) "Готово" else "Редактировать"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
            scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
        ),
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesList(
    notes: List<Note>,
    isEditMode: Boolean,
    onNoteClick: (Int) -> Unit,
    onDeleteClick: (Note) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        notes.forEach { note ->
            NoteCard(
                note = note,
                isEditMode = isEditMode,
                onNoteClick = {
                    if (!isEditMode) onNoteClick(note.id)
                },onDeleteClick = { onDeleteClick(note) }
            )
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyNotesContent(
    contactId: Int?,
    onAddClick: (() -> Unit)?
) {
    val titleRes = if (contactId != null) "У контакта пока нет заметок" else "У вас пока нет заметок"
    val descRes = if (contactId != null) "Добавьте первую заметку для этого контакта" else "Нажмите кнопку + чтобы создать первую заметку"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Note,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = titleRes,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = descRes,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        if (onAddClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onAddClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Создать заметку")
            }
        }
    }
}

/*
private fun getFakeNotes(): List<Note> {
    return listOf(
        Note(id = 1, contactId = 101, text = "Купить продукты: молоко, хлеб, яйца", date = System.currentTimeMillis() - 86400000),
        Note(id = 2, contactId = 101, text = "Встреча с командой в 15:00", date = System.currentTimeMillis()),
        Note(id = 3, contactId = 102, text = "Позвонить родителям", date = System.currentTimeMillis() - 172800000),
        Note(id = 4, contactId = 101, text = "Заказать доставку воды", date = System.currentTimeMillis() - 259200000),
        Note(id = 5, contactId = 103, text = "Подготовить отчет за квартал", date = System.currentTimeMillis() - 345600000),
        Note(id = 6, contactId = 101, text = "Напомнить о встрече завтра", date = System.currentTimeMillis() - 432000000),
        Note(id = 7, contactId = 102, text = "Купить подарок на день рождения", date = System.currentTimeMillis() - 518400000),
        Note(id = 8, contactId = 101, text = "Записаться к врачу", date = System.currentTimeMillis() - 604800000),
        Note(id = 9, contactId = 103, text = "Проверить счета за коммунальные услуги", date = System.currentTimeMillis() - 691200000),
        Note(id = 10, contactId = 101, text = "Отправить документы по почте", date = System.currentTimeMillis() - 777600000)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesListScreenPreviewWrapper(
    notes: List<Note> = emptyList(),
    isLoading: Boolean = false,
    isEditMode: Boolean = false,
    hasFilter: Boolean = false,
    forceScrollHeight: Int? = null
) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val snackbarHostState = remember { SnackbarHostState() }
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

            Scaffold(
                topBar = {
                    NotesListTopBar(
                        isEditMode = isEditMode,
                        hasActiveFilter = hasFilter,
                        onEditModeToggle = { },
                        onBackClick = if (hasFilter) { { } } else null,
                        scrollBehavior = scrollBehavior
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .then(if (forceScrollHeight != null) Modifier.height(forceScrollHeight.dp) else Modifier)
                ) {
                    when {
                        isLoading -> LoadingIndicator()
                        notes.isEmpty() -> EmptyNotesContent(
                            contactId = if (hasFilter) 1 else null,
                            onAddClick = null
                        )
                        else -> NotesList(
                            notes = notes,
                            isEditMode = isEditMode,
                            onNoteClick = { },
                            onDeleteClick = { },
                            scrollBehavior = scrollBehavior
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Screen - Список заметок", showBackground = true, showSystemUi = true)
@Composable
fun NotesListScreenFullPreview() {
    NotesListScreenPreviewWrapper(
        notes = getFakeNotes(),
        isLoading = false,
        isEditMode = false,
        hasFilter = false
    )
}

@Preview(name = "Screen - Скролл тест (300dp)", showBackground = true, showSystemUi = true)
@Composable
fun NotesListScreenScrollTestPreview() {
    NotesListScreenPreviewWrapper(
        notes = getFakeNotes(),
        isLoading = false,
        isEditMode = false,
        hasFilter = false,
        forceScrollHeight = 300
    )
}

@Preview(name = "Screen - Пустой список", showBackground = true, showSystemUi = true)
@Composable
fun NotesListScreenEmptyPreview() {
    NotesListScreenPreviewWrapper(
        notes = emptyList(),
        isLoading = false,
        isEditMode = false,
        hasFilter = false
    )
}

@Preview(name = "Screen - Загрузка", showBackground = true, showSystemUi = true)
@Composable
fun NotesListScreenLoadingPreview() {
    NotesListScreenPreviewWrapper(
        notes = emptyList(),
        isLoading = true,
        isEditMode = false,
        hasFilter = false
    )
}

@Preview(name = "Screen - Режим редактирования", showBackground = true, showSystemUi = true)
@Composable
fun NotesListScreenEditPreview() {
    NotesListScreenPreviewWrapper(
        notes = getFakeNotes(),
        isLoading = false,
        isEditMode = true,
        hasFilter = false
    )
}*/