package com.example.netarchive.ui.screens.settings_screen.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.netarchive.ui.theme.CardBackground

@Composable
fun AppDataPage(viewModel: DataSettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showContactsDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.resetMessages()
        }
        state.error?.let { err ->
            snackbarHostState.showSnackbar("Ошибка: $err")
            viewModel.resetMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Размер БД
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Folder,
                    contentDescription = "",
                    modifier = Modifier.size(50.dp)
                )
                Column {
                    Text("Размер базы данных", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        state.dbSize,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Кнопка очистки контактов
        Button(
            onClick = { showContactsDialog = true },
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (state.isLoading) CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
            else Text("Очистить контакты", fontWeight = FontWeight.Medium)
        }

        // Кнопка очистки профиля
        Button(
            onClick = { showProfileDialog = true },
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (state.isLoading) CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
            else Text("Очистить профиль", fontWeight = FontWeight.Medium)
        }
    }
    if (showContactsDialog) {
        AlertDialog(
            onDismissRequest = { showContactsDialog = false },
            title = { Text("Удалить все контакты?") },
            text = { Text("Все сохранённые контакты и связанные заметки будут удалены безвозвратно.") },
            confirmButton = {
                TextButton(onClick = {
                    showContactsDialog = false; viewModel.clearTable("contacts")
                }) {
                    Text("Удалить", color = Color(0xFF1976D2))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showContactsDialog = false
                }) { Text("Отмена") }
            }
        )
    }

    // Диалог: Профиль
    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("Сбросить профиль?") },
            text = { Text("Данные профиля будут удалены.") },
            confirmButton = {
                TextButton(onClick = {
                    showProfileDialog = false; viewModel.clearTable("profile")
                }) {
                    Text("Сбросить", color = Color(0xFFE85653))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showProfileDialog = false
                }) { Text("Отмена") }
            }
        )
    }
}