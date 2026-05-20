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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.netarchive.R

@Composable
fun AppDataPage(viewModel: DataSettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showContactsDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { resId ->
            snackbarHostState.showSnackbar(context.getString(resId))
            viewModel.resetMessages()
        }
        state.error?.let { err ->
            snackbarHostState.showSnackbar(context.getString(R.string.data_settings_error, err))
            viewModel.resetMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = dimensionResource(id = R.dimen.padding_top_header),
                start = dimensionResource(id = R.dimen.padding_medium),
                end = dimensionResource(id = R.dimen.padding_medium)
            ),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_medium))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_medium)),
            colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.card_background)),
            elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.card_elevation_default))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.card_padding)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(dimensionResource(id = R.dimen.data_page_icon_size))
                )
                Column {
                    Text(
                        text = stringResource(R.string.data_settings_db_size),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = state.dbSize,
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
                .height(dimensionResource(id = R.dimen.button_height))
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_large)),
                    color = colorResource(id = R.color.white),
                    strokeWidth = dimensionResource(id = R.dimen.data_settings_progress_stroke)
                )
            } else {
                Text(stringResource(R.string.data_settings_clear_contacts), fontWeight = FontWeight.Medium)
            }
        }

        Button(
            onClick = { showProfileDialog = true },
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.button_height))
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_large)),
                    color = colorResource(id = R.color.white),
                    strokeWidth = dimensionResource(id = R.dimen.data_settings_progress_stroke)
                )
            } else {
                Text(stringResource(R.string.data_settings_clear_profile), fontWeight = FontWeight.Medium)
            }
        }
    }

    if (showContactsDialog) {
        AlertDialog(
            onDismissRequest = { showContactsDialog = false },
            title = { Text(stringResource(R.string.data_settings_delete_contacts_title)) },
            text = { Text(stringResource(R.string.data_settings_delete_contacts_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showContactsDialog = false
                    viewModel.clearTable("contacts")
                }) {
                    Text(stringResource(R.string.delete), color = colorResource(id = R.color.data_action_blue))
                }
            },
            dismissButton = {
                TextButton(onClick = { showContactsDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text(stringResource(R.string.data_settings_reset_profile_title)) },
            text = { Text(stringResource(R.string.data_settings_reset_profile_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showProfileDialog = false
                    viewModel.clearTable("profile")
                }) {
                    Text(stringResource(R.string.data_settings_reset), color = colorResource(id = R.color.data_action_red))
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}