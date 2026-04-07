package com.example.netarchive.ui.screens.settings_screen.pages

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.netarchive.ui.theme.CardBackground

@Composable
fun ImportContactsPage(
    viewModel: ImportContactsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startImport()
        } else {
            viewModel.goBackToSettings()
        }
    }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeMessage()
        }
        state.error?.let { err ->
            snackbarHostState.showSnackbar(err)
            viewModel.consumeMessage()
        }
    }

    if (state.isMainPage) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PersonAddAlt,
                        contentDescription = "",
                        modifier = Modifier.size(50.dp)
                    )
                    Text(
                        "Вы можете импортировать контакты из сторонних приложений",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Из приложения Контакты", fontWeight = FontWeight.Medium)
            }
            Text(
                "Список приложений будет расширяться",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    if (state.isImportFromContacts) {
        if (state.isContactsListLoading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text(
                    "Читаем контакты",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else if (state.isContactsListSaving) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text(
                    "Сохранение в бд",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Box() {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(245.dp))
                    }
                    items(
                        items = state.previewContacts,
                        key = { it.deviceContactId }
                    ) { item ->
                        PreviewContactCard(
                            contactPreviewItem = item,
                            onToggleClick = { viewModel.toggleSelection(item.deviceContactId) }
                        )
                    }
                    if (state.previewContacts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Контакты не найдены",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(85.dp))
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 85.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.95f)),
                        elevation = CardDefaults.cardElevation(1.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(all = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.goBackToSettings() }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                        contentDescription = null,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                Text(
                                    "Выберите контакты, которые нужно импортировать",
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                            Button(
                                onClick = { viewModel.saveSelectedContacts() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                ),
                            ) {
                                Text("Импортировать выбранные", fontWeight = FontWeight.Medium)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { viewModel.toggleChangeSelectedAll(true) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colorScheme.onPrimaryContainer.copy(alpha = 0.65f)
                                    ),
                                ) {
                                    Text("Выбрать все", fontWeight = FontWeight.Medium)
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                Button(
                                    onClick = { viewModel.toggleChangeSelectedAll(false) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colorScheme.onPrimaryContainer.copy(alpha = 0.65f)
                                    ),
                                ) {
                                    Text("Снять все", fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun PreviewContactCard(
    contactPreviewItem: ContactPreviewItem,
    onToggleClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleClick() },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (contactPreviewItem.contact.avatar != null) {
                    AsyncImage(
                        model = contactPreviewItem.contact.avatar,
                        contentDescription = "Avatar of ${contactPreviewItem.contact.username}",
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
                            text = contactPreviewItem.contact.username.firstOrNull()
                                ?.uppercaseChar()?.toString() ?: "?",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contactPreviewItem.contact.username,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = contactPreviewItem.contact.phone
                            ?: contactPreviewItem.contact.email
                            ?: "Нет телефона",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onToggleClick
                ) {
                    if (contactPreviewItem.isSelected) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Circle,
                            contentDescription = null
                        )
                    }
                }
            }

        }
    }
}