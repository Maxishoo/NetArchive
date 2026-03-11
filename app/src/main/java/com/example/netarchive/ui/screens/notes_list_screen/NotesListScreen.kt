package com.example.netarchive.ui.screens.notes_list_screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.netarchive.domain.model.Note
import com.example.netarchive.ui.components.cards.NoteCard
import kotlinx.coroutines.launch


import androidx.compose.ui.tooling.preview.Preview


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    viewModel: NotesViewModel = hiltViewModel(),
    contactId: Int? = null,
    onNoteClick: (Int) -> Unit = {},
    onAddNoteClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Состояния из ViewModel
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isEditMode by viewModel.isEditMode.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val selectedContactId by viewModel.selectedContactId.collectAsStateWithLifecycle()

    // Загрузка заметок при изменении contactId
    LaunchedEffect(contactId) {
        if (contactId != null) {
            viewModel.loadNotesByContact(contactId)
        } else {
            viewModel.loadAllNotes()
        }
    }

    // Показ ошибок
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                actionLabel = "OK",
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    /*Обработка назад
    BackHandler(enabled = selectedContactId != null) {
        viewModel.clearContactFilter()
        onBackClick?.invoke()
    }*/

    Scaffold(
        topBar = {
            NotesListTopBar(
                isEditMode = isEditMode,
                selectedContactId = selectedContactId,
                onEditModeToggle = { viewModel.toggleEditMode() },
                onBackClick = if (selectedContactId != null) {
                    {
                        viewModel.clearContactFilter()
                        onBackClick?.invoke()
                    }
                } else null
            )
        },
        floatingActionButton = {
            if (!isEditMode && onAddNoteClick != null) {
                FloatingActionButton(
                    onClick = onAddNoteClick,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Добавить заметку"
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> LoadingIndicator()

                notes.isEmpty() -> EmptyNotesContent(
                    contactId = selectedContactId,
                    onAddClick = onAddNoteClick
                )

                else -> NotesList(
                    notes = notes,
                    isEditMode = isEditMode,
                    onNoteClick = { noteId ->
                        if (!isEditMode) {
                            onNoteClick(noteId)
                        }
                    },
                    onDeleteClick = { note ->
                        viewModel.deleteNote(note)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListTopBar(
    isEditMode: Boolean,
    selectedContactId: Int?,
    onEditModeToggle: () -> Unit,
    onBackClick: (() -> Unit)?
) {
    TopAppBar(
        title = {
            Text(
                when {
                    selectedContactId != null -> "Заметки контакта"
                    else -> "Все заметки"
                }
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
        }
    )
}

@Composable
fun NotesList(
    notes: List<Note>,
    isEditMode: Boolean,
    onNoteClick: (Int) -> Unit,
    onDeleteClick: (Note) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = notes,
            key = { it.id }
        ) { note ->
            NoteCard(
                note = note,
                isEditMode = isEditMode,
                onNoteClick = { onNoteClick(note.id) },
                onDeleteClick = { onDeleteClick(note) }
            )
        }
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyNotesContent(
    contactId: Int?,
    onAddClick: (() -> Unit)?
) {
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
            text = when {
                contactId != null -> "У контакта пока нет заметок"
                else -> "У вас пока нет заметок"
            },
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when {
                contactId != null -> "Добавьте первую заметку для этого контакта"
                else -> "Нажмите кнопку + чтобы создать первую заметку"
            },
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


@Preview(
    name = "Notes List - With Notes",
    showBackground = true
)
@Composable
fun NotesListWithNotesPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.padding(8.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            NotesList(
                notes = listOf(
                    Note(
                        id = 1,
                        contactId = 101,
                        text = "Купить продукты: молоко, хлеб, яйца",
                        date = System.currentTimeMillis() - 86400000
                    ),
                    Note(
                        id = 2,
                        contactId = 101,
                        text = "Встреча с командой в 15:00",
                        date = System.currentTimeMillis()
                    ),
                    Note(
                        id = 3,
                        contactId = 102,
                        text = "Позвонить родителям",
                        date = System.currentTimeMillis() - 172800000
                    )
                ),
                isEditMode = false,
                onNoteClick = {},
                onDeleteClick = {}
            )
        }
    }
}