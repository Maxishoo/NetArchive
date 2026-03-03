package com.example.netarchive.ui.screens.contact_view_screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.netarchive.domain.model.Note
import com.example.netarchive.ui.components.cards.NoteCard
import com.example.netarchive.ui.theme.NetArchiveTheme
import kotlin.text.get

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactViewScreen(
    viewModel: ContactViewViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onAddNoteClick: (Int, String) -> Unit = { _, _ -> }
) {
    val viewState by viewModel.viewState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Информация", "Заметки")
    val contactId = viewState.contactId
    val contactName = viewState.username


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Контакт") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("✕", style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = {
                    if (viewState.isEditMode) {
                        IconButton(
                            onClick = { viewModel.saveContact() },
                            enabled = viewState.hasChanges && !viewState.isLoading && viewState.username.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Save,
                                contentDescription = "Сохранить",
                                tint = if (viewState.hasChanges)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        IconButton(onClick = { viewModel.enableEditMode() }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Редактировать"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (viewState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Табы
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Контент табов
                when (selectedTab) {
                    0 -> ContactInfoTab(
                        viewState = viewState,
                        viewModel = viewModel
                    )
                    1 -> NotesTab(
                        notes = viewState.notes,
                        onAddNoteClick = {
                            onAddNoteClick(contactId, contactName)
                        }
                    )
                }
            }
        }
    }

    LaunchedEffect(viewState.isSuccess) {
        if (viewState.isSuccess && !viewState.isEditMode) {
            viewModel.clearError()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactInfoTab(
    viewState: ContactViewState,
    viewModel: ContactViewViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Аватар
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (viewState.username.isNotEmpty())
                    viewState.username.first().uppercaseChar().toString()
                else "?",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Категория (dropdown)
        var categoryExpanded by remember { mutableStateOf(false) }
        var selectedCategory by remember { mutableStateOf("Друзья") }

        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it && viewState.isEditMode },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Категории") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                enabled = viewState.isEditMode,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF64B5F6),  // Голубой при фокусе
                    unfocusedBorderColor = Color(0xFF90CAF9), // Голубой без фокуса
                    disabledBorderColor = Color(0xFF90CAF9),
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedLabelColor = Color(0xFF64B5F6),
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                listOf("Друзья", "Коллеги", "Семья", "Знакомые").forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        // Поля контакта с голубой обводкой
        OutlinedTextField(
            value = viewState.username,
            onValueChange = { viewModel.onUsernameChange(it) },
            label = { Text("Имя *") },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewState.isEditMode,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF64B5F6),  // Голубой при фокусе
                unfocusedBorderColor = Color(0xFF90CAF9), // Голубой без фокуса
                disabledBorderColor = Color(0xFF90CAF9),
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLabelColor = Color(0xFF64B5F6),
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        OutlinedTextField(
            value = viewState.phone,
            onValueChange = { viewModel.onPhoneChange(it) },
            label = { Text("Телефон") },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewState.isEditMode,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF64B5F6),
                unfocusedBorderColor = Color(0xFF90CAF9),
                disabledBorderColor = Color(0xFF90CAF9),
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLabelColor = Color(0xFF64B5F6),
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        OutlinedTextField(
            value = viewState.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewState.isEditMode,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF64B5F6),
                unfocusedBorderColor = Color(0xFF90CAF9),
                disabledBorderColor = Color(0xFF90CAF9),
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLabelColor = Color(0xFF64B5F6),
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        OutlinedTextField(
            value = viewState.telegram,
            onValueChange = { viewModel.onTelegramChange(it) },
            label = { Text("Telegram") },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewState.isEditMode,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF64B5F6),
                unfocusedBorderColor = Color(0xFF90CAF9),
                disabledBorderColor = Color(0xFF90CAF9),
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLabelColor = Color(0xFF64B5F6),
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        OutlinedTextField(
            value = viewState.max,
            onValueChange = { viewModel.onMaxChange(it) },
            label = { Text("Адрес") },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewState.isEditMode,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF64B5F6),
                unfocusedBorderColor = Color(0xFF90CAF9),
                disabledBorderColor = Color(0xFF90CAF9),
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLabelColor = Color(0xFF64B5F6),
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        OutlinedTextField(
            value = viewState.job,
            onValueChange = { viewModel.onJobChange(it) },
            label = { Text("Работа") },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewState.isEditMode,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF64B5F6),
                unfocusedBorderColor = Color(0xFF90CAF9),
                disabledBorderColor = Color(0xFF90CAF9),
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLabelColor = Color(0xFF64B5F6),
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        // Ошибка
        viewState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun NotesTab(
    notes: List<Note>,
    onAddNoteClick: () -> Unit
) {
    if (notes.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Нет заметок",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAddNoteClick) {
                Text("Добавить заметку")
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 80.dp),  // <-- Добавь эту строку!
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(count = notes.size) { index ->
                    NoteCard(note = notes[index])
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddNoteClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Добавить заметку")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContactViewScreenPreview() {
    NetArchiveTheme {
        ContactViewScreen(onBackClick = {})
    }
}