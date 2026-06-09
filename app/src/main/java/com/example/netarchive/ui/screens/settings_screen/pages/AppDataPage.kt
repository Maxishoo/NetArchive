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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.netarchive.R
import com.example.netarchive.ui.theme.AppTheme
import com.example.netarchive.ui.theme.CardBackground

@Composable
fun AppDataPage(viewModel: DataSettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showContactsDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.resetMessages()
        }
        state.error?.let { err ->
            snackbarHostState.showSnackbar(context.getString(R.string.app_data_error, err))
            viewModel.resetMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
                    Text(stringResource(R.string.app_data_db_size), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        state.dbSize,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Button(
            onClick = { showContactsDialog = true },
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (state.isLoading) CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = AppTheme.colors.cardBackground,
                strokeWidth = 2.dp
            )
            else Text(stringResource(R.string.app_data_clear_contacts), fontWeight = FontWeight.Medium)
        }

        Button(
            onClick = { showProfileDialog = true },
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (state.isLoading) CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = AppTheme.colors.cardBackground,
                strokeWidth = 2.dp
            )
            else Text(stringResource(R.string.app_data_clear_profile), fontWeight = FontWeight.Medium)
        }
    }
    if (showContactsDialog) {
        AlertDialog(
            onDismissRequest = { showContactsDialog = false },
            title = { Text(stringResource(R.string.app_data_delete_contacts_title)) },
            text = { Text(stringResource(R.string.app_data_delete_contacts_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showContactsDialog = false; viewModel.clearTable("contacts")
                }) {
                    Text(stringResource(R.string.action_delete), color = AppTheme.colors.confirmAction)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showContactsDialog = false
                }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text(stringResource(R.string.app_data_reset_profile_title)) },
            text = { Text(stringResource(R.string.app_data_reset_profile_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showProfileDialog = false; viewModel.clearTable("profile")
                }) {
                    Text(stringResource(R.string.app_data_reset), color = AppTheme.colors.destructiveAction)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showProfileDialog = false
                }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}