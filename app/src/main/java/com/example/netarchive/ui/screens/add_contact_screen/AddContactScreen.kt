package com.example.netarchive.ui.screens.add_contact_screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.netarchive.ui.theme.NetArchiveTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    viewModel: AddContactViewModel = hiltViewModel(),
    onContactCreated: () -> Unit,
    onBackClick: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавить контакт") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("✕", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = formState.username,
                onValueChange = viewModel::onUsernameChange,
                label = { Text("Имя *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = formState.error != null && formState.username.isBlank()
            )

            OutlinedTextField(
                value = formState.phone,
                onValueChange = viewModel::onPhoneChange,
                label = { Text("Телефон") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.telegram,
                onValueChange = viewModel::onTelegramChange,
                label = { Text("Telegram") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.max,
                onValueChange = viewModel::onMaxChange,
                label = { Text("Max") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formState.job,
                onValueChange = viewModel::onJobChange,
                label = { Text("Место работы") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )


            formState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .padding(bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f),
                    enabled = !formState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text("Отмена")
                }

                Button(
                    onClick = { viewModel.saveContact() },
                    modifier = Modifier.weight(1f),
                    enabled = !formState.isLoading && formState.username.isNotBlank()
                ) {
                    if (formState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Сохранить")
                    }
                }
            }
        }
    }

    LaunchedEffect(formState.isSuccess) {
        if (formState.isSuccess) {
            onContactCreated()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddContactScreenPreview() {
    NetArchiveTheme {
        AddContactScreen(
            onContactCreated = {},
            onBackClick = {}
        )
    }
}