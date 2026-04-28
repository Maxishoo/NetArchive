package com.example.netarchive.ui.screens.contact_view_screen

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.netarchive.data.remote.ai.model.AiFeatureConfig
import com.example.netarchive.domain.model.Note
import com.example.netarchive.ui.components.CategorySelector
import com.example.netarchive.ui.components.QrDialog
import com.example.netarchive.ui.components.cards.NoteCard
import com.example.netarchive.ui.theme.NetArchiveTheme
import com.example.netarchive.ui.navigation.NoteNavigationData
import androidx.core.net.toUri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.app.DatePickerDialog
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.runtime.getValue
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactViewScreen(
    viewModel: ContactViewViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onAddNoteClick: (NoteNavigationData) -> Unit = {},
    initialTab: Int = 0
) {
    val viewState by viewModel.viewState.collectAsState()
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) }
    val tabs = listOf("Информация", "Заметки")
    var isNotesEditMode by remember { mutableStateOf(false) }
    val aiState by viewModel.aiState.collectAsState()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onAvatarSelected(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Контакт") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть"
                        )
                    }
                },
                actions = {
                    if (!viewState.isEditMode) {
                        IconButton(onClick = { viewModel.generateConversationStarter() }) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = "AI подсказка",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }


                    if (!viewState.showQrDialog) {
                        IconButton(onClick = viewModel::openQr) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Показать qr код"
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
                PrimaryTabRow(
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
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        slideInHorizontally { width ->
                            if (targetState > initialState) width else -width
                        } + fadeIn() togetherWith
                                slideOutHorizontally { width ->
                                    if (targetState > initialState) -width else width
                                } + fadeOut()
                    },
                    label = "tabAnimation"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> ContactInfoTab(
                            viewState = viewState,
                            viewModel = viewModel,
                            selectedTab,
                            onAvatarClick = {
                                if (viewState.isEditMode) {
                                    imagePickerLauncher.launch("image/*")
                                }
                            }
                        )

                        1 -> NotesTab(
                            notes = viewState.notes,
                            contactName = viewState.username,
                            isEditMode = isNotesEditMode,
                            onAddNoteClick = {
                                onAddNoteClick(
                                    NoteNavigationData.forNewNote(
                                        contactId = viewState.contactId,
                                        contactName = viewState.username,
                                        contactAvatar = viewState.avatar,
                                        source = "contact_view",
                                        selectedTab = selectedTab
                                    )
                                )
                            },
                            onNoteClick = { note ->
                                onAddNoteClick(
                                    NoteNavigationData.forEditNote(
                                        contactId = viewState.contactId,
                                        contactName = viewState.username,
                                        contactAvatar = viewState.avatar,
                                        note = note,
                                        source = "contact_view",
                                        selectedTab = selectedTab
                                    )
                                )
                            },
                            onDeleteNoteClick = { note ->
                                viewModel.deleteNote(note)
                            },
                            onToggleEditMode = { isNotesEditMode = !isNotesEditMode }
                        )
                    }
                }
            }
        }
    }
    LaunchedEffect(viewState.isContactDeleted) {
        if (viewState.isContactDeleted) {
            onBackClick()
            viewModel.clearDeleteFlag()
        }
    }

    LaunchedEffect(viewState.isSuccess) {
        if (viewState.isSuccess && !viewState.isEditMode) {
            viewModel.clearError()
        }
    }
    if (viewState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteDialog() },
            title = { Text("Удалить контакт?") },
            text = {
                Column {
                    Text("Вы уверены что хотите удалить контакт \"${viewState.username}\"?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Все связанные заметки будут безвозвратно удалены",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteContact() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteDialog() }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (viewState.showQrDialog) {
        QrDialog(
            qrCode = viewState.qrBitmap,
            onCloseClick = viewModel::closeQr
        )
    }
    when (val state = aiState) {
        is AiState.Loading -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetAiState() },
                title = { Text("Генерирую подсказку...") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Анализирую информацию о контакте",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { viewModel.resetAiState() }) {
                        Text("Отмена")
                    }
                }
            )
        }

        is AiState.Success -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetAiState() },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Идеи для сообщения")
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        state.suggestions.forEachIndexed { index, suggestion ->
                            val isCopied = state.copiedIndex == index

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.copySuggestion(index) }
                                    .then(if (isCopied) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small) else Modifier),
                                shape = MaterialTheme.shapes.small,
                                color = if (isCopied)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = suggestion,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isCopied) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Скопировано",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = "Копировать",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = "🔒 Данные обрабатываются анонимно",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                },

                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { viewModel.regenerateSuggestions() }) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ещё варианты")
                        }
                        TextButton(onClick = { viewModel.resetAiState() }) {
                            Text("Закрыть")
                        }
                    }
                },
                dismissButton = {}
            )
        }

        is AiState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetAiState() },
                title = { Text("Ошибка") },
                text = { Text(state.message) },
                confirmButton = {
                    Button(onClick = { viewModel.resetAiState() }) {
                        Text("Понятно")
                    }
                }
            )
        }
        is AiState.Disabled -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetAiState() },
                title = { Text("⚠️ AI отключен") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(AiFeatureConfig.AI_DISABLED_MESSAGE)

                        // пока todoo (кнопка в настройки)
                        // TextButton(onClick = { navigateToSettings() }) {
                        //     Text("Перейти в настройки")
                        // }
                    }
                },
                confirmButton = {
                    Button(onClick = { viewModel.resetAiState() }) {
                        Text("Понятно")
                    }
                }
            )
        }

        is AiState.Idle -> { }
    }
}

@SuppressLint("UseKtx")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactInfoTab(
    viewState: ContactViewState,
    viewModel: ContactViewViewModel,
    selectedTab: Int,
    onAvatarClick: () -> Unit = {}
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 80.dp, top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
                .then(
                    if (viewState.isEditMode) {
                        Modifier.clickable(onClick = onAvatarClick)
                    } else {
                        Modifier
                    }
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (viewState.avatar.isNotBlank()) {
                    AsyncImage(
                        model = viewState.avatar,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center,
                        error = null
                    )
                } else {
                    Text(
                        text = viewState.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp)
                    )
                }
            }
            if (viewState.isEditMode) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Change Avatar",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .offset(x = 4.dp, y = 4.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(4.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }



        if (viewState.isEditMode) {
            val allCategories by viewModel.allCategories.collectAsState()
            val selectedCategories by viewModel.selectedCategories.collectAsState()

            CategorySelector(
                allCategories = allCategories,
                selectedCategories = selectedCategories,
                onCategoriesChanged = { categories ->
                    viewModel.setSelectedCategories(categories)
                },
                onCreateCategory = { name ->
                    viewModel.createCategory(name)
                },
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            val selectedCategories by viewModel.selectedCategories.collectAsState()
            if (selectedCategories.isNotEmpty()) {
                Text(
                    text = "Категории:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    selectedCategories.forEach { category ->
                        AssistChip(
                            onClick = { },
                            label = { Text(category.name) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (category.isDefault) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.secondaryContainer
                                }
                            ),
                            enabled = true
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = viewState.username,
            onValueChange = { viewModel.onUsernameChange(it) },
            label = { Text("Имя *") },
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
        BirthdayField(
            timestamp = viewState.birthday,
            onDateSelected = viewModel::onBirthdayChange,
            isEditMode = viewState.isEditMode
        )

        OutlinedTextField(
            value = viewState.phone,
            onValueChange = { viewModel.onPhoneChange(it) },
            label = { Text("Телефон") },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewState.isEditMode,
            singleLine = true,
            trailingIcon = {
                if (!viewState.isEditMode && viewState.phone.isNotBlank()) {
                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = "tel:${viewState.phone}".toUri()
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Call,
                            contentDescription = "Позвонить",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
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
            trailingIcon = {
                if (!viewState.isEditMode && viewState.email.isNotBlank()) {
                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = "mailto:${viewState.email}".toUri()
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = "Написать email",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
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
            trailingIcon = {
                if (!viewState.isEditMode && viewState.telegram.isNotBlank()) {
                    IconButton(
                        onClick = {
                            try {
                                val username = viewState.telegram.trimStart('@')
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("tg://resolve?domain=$username")
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                try {
                                    val username = viewState.telegram.trimStart('@')
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("https://t.me/$username")
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Send,
                            contentDescription = "Открыть Telegram",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
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
            label = { Text("MAX") },
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

        DescriptionField(
            value = viewState.description,
            onValueChange = viewModel::onDescriptionChange,
            isEditMode = viewState.isEditMode
        )

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
        if (selectedTab == 0) {
            if (viewState.isEditMode) {
                Button(
                    onClick = viewModel::saveContact,
                    enabled = viewState.username.isNotBlank() && viewState.hasChanges && !viewState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewState.hasChanges && viewState.username.isNotBlank())
                            Color(0xFF4D5D8A)
                        else
                            Color.Gray.copy(alpha = 0.3f),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Сохранить",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            } else {
                Button(
                    onClick = viewModel::enableEditMode,
                    enabled = !viewState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Редактировать",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
        if (!viewState.isEditMode) {
            Button(
                onClick = { viewModel.showDeleteDialog() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                enabled = !viewState.isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Удалить контакт")
            }
        }
    }
}

@Composable
private fun NotesTab(
    notes: List<Note>,
    contactName: String,
    isEditMode: Boolean = false,
    onAddNoteClick: () -> Unit,
    onNoteClick: (Note) -> Unit = {},
    onDeleteNoteClick: (Note) -> Unit = {},
    onToggleEditMode: () -> Unit = {}
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
                .padding(bottom = 80.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Заметки о $contactName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = onToggleEditMode) {
                    Text(if (isEditMode) "Готово" else "Удалить")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(count = notes.size) { index ->
                    NoteCard(
                        note = notes[index],
                        isEditMode = isEditMode,
                        onNoteClick = { onNoteClick(notes[index]) },
                        onDeleteClick = { onDeleteNoteClick(notes[index]) }
                    )
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthdayField(
    timestamp: Long?,
    onDateSelected: (Long?) -> Unit,
    isEditMode: Boolean,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val displayText = timestamp?.let {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it))
    } ?: ""

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused, isEditMode) {
        if (isFocused && isEditMode) {
            showDatePicker = true
        }
    }

    OutlinedTextField(
        value = displayText,
        onValueChange = { },
        label = { Text("Дата рождения") },
        placeholder = { Text("") },
        modifier = modifier.fillMaxWidth(),
        enabled = isEditMode,
        readOnly = true,
        singleLine = true,
        interactionSource = interactionSource,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Выбрать дату",
                tint = MaterialTheme.colorScheme.primary,
                modifier = modifier.clickable(enabled = isEditMode) {
                    showDatePicker = true
                }
            )
        },
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

    if (showDatePicker) {
        ShowDatePickerDialog(
            initialDate = timestamp ?: System.currentTimeMillis(),
            onDateSelected = { selectedDate ->
                onDateSelected(selectedDate)
                showDatePicker = false
            },
            onDismiss = {
                showDatePicker = false
            }
        )
    }
}
@Composable
private fun ShowDatePickerDialog(
    initialDate: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = initialDate
    }

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val dialog = DatePickerDialog(
        LocalContext.current,
        { _, selectedYear, selectedMonth, selectedDay ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDay)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onDateSelected(selectedCalendar.timeInMillis)
        },
        year,
        month,
        day
    )

    dialog.setOnDismissListener { onDismiss() }
    dialog.show()
}

@Composable
private fun DescriptionField(
    value: String,
    onValueChange: (String) -> Unit,
    isEditMode: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Описание") },
        modifier = modifier.fillMaxWidth(),
        enabled = isEditMode,
        singleLine = false,
        minLines = 3,
        maxLines = 6,
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
}

@Preview(showBackground = true)
@Composable
fun ContactViewScreenPreview() {
    NetArchiveTheme {
        ContactViewScreen(onBackClick = {})
    }
}