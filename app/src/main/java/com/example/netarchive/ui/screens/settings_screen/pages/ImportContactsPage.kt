package com.example.netarchive.ui.screens.settings_screen.pages

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.netarchive.R

@Composable
fun ImportContactsPage(
    viewModel: ImportContactsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
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
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.consumeMessage()
        }
        state.error?.let { err ->
            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
            viewModel.consumeMessage()
        }
    }

    if (state.isMainPage) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = dimensionResource(id = R.dimen.screen_padding_top),
                    start = dimensionResource(id = R.dimen.padding_medium),
                    end = dimensionResource(id = R.dimen.padding_medium)
                ),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_medium)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen.import_contacts_info_card_height)),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_big_small)),
                colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.card_background)),
                elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.card_elevation_default))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(id = R.dimen.card_padding)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_dialog)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PersonAddAlt,
                        contentDescription = null,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.import_contacts_icon_size))
                    )
                    Text(
                        text = stringResource(R.string.import_contacts_info_text),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(id =R.dimen.import_contacts_secondary_button_height))
            ) {
                Text(stringResource(R.string.import_from_contacts_app), fontWeight = FontWeight.Medium)
            }
            Text(
                text = stringResource(R.string.import_contacts_coming_soon),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    if (state.isImportFromContacts) {
        if (state.isContactsListLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(id = R.dimen.padding_medium)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.import_contacts_loading_spacer)))
                Text(
                    text = stringResource(R.string.import_contacts_reading),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else if (state.isContactsListSaving) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(id = R.dimen.padding_medium)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.import_contacts_loading_spacer)))
                Text(
                    text = stringResource(R.string.import_contacts_saving),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Box {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(vertical = dimensionResource(id = R.dimen.spacing_small)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.import_contacts_card_spacing))
                ) {
                    item {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.import_contacts_list_top_spacer)))
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
                                    .padding(dimensionResource(id = R.dimen.import_contacts_empty_padding)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.import_contacts_not_found),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.import_contacts_list_bottom_spacer)))
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = dimensionResource(id = R.dimen.import_contacts_toolbar_top_padding)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_small))
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_zero)),
                        colors = CardDefaults.cardColors(
                            containerColor = colorResource(id = R.color.card_background).copy(alpha = 0.95f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.card_elevation_default)),
                    ) {
                        Column(
                            modifier = Modifier.padding(all = dimensionResource(id = R.dimen.import_contacts_toolbar_padding)),
                            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.import_contacts_loading_spacer))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { viewModel.goBackToSettings() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                        contentDescription = stringResource(R.string.back),
                                        modifier = Modifier.size(dimensionResource(id = R.dimen.import_contacts_back_icon_size))
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.import_contacts_select_prompt),
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Button(
                                onClick = { viewModel.saveSelectedContacts() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(dimensionResource(id = R.dimen.import_contacts_first_button_height)),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(id = R.color.import_action_primary).copy(alpha = 0.85f)
                                ),
                            ) {
                                Text(stringResource(R.string.import_selected), fontWeight = FontWeight.Medium)
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { viewModel.toggleChangeSelectedAll(true) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(dimensionResource(id = R.dimen.import_contacts_secondary_button_height)),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colorResource(id = R.color.import_action_secondary).copy(alpha = 0.65f)
                                    ),
                                ) {
                                    Text(stringResource(R.string.import_select_all), fontWeight = FontWeight.Medium)
                                }

                                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_extra_small)))

                                Button(
                                    onClick = { viewModel.toggleChangeSelectedAll(false) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(dimensionResource(id = R.dimen.import_contacts_secondary_button_height)),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colorResource(id = R.color.import_action_secondary).copy(alpha = 0.65f)
                                    ),
                                ) {
                                    Text(stringResource(R.string.import_deselect_all), fontWeight = FontWeight.Medium)
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
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_zero)),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.card_background)),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.import_contacts_card_elevation))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.import_contacts_card_padding))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.import_contacts_row_spacing))
            ) {
                if (contactPreviewItem.contact.avatar != null) {
                    AsyncImage(
                        model = contactPreviewItem.contact.avatar,
                        contentDescription = stringResource(R.string.contact_avatar_description, contactPreviewItem.contact.username),
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.import_contacts_avatar_size))
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.import_contacts_avatar_size))
                            .clip(CircleShape)
                            .background(color = colorResource(id = R.color.reminder_contact_avatar_placeholder)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contactPreviewItem.contact.username.firstOrNull()
                                ?.uppercaseChar()?.toString() ?: stringResource(R.string.default_avatar),
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
                            ?: stringResource(R.string.import_contacts_no_phone),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (contactPreviewItem.isDuplicate) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(end = dimensionResource(id = R.dimen.import_contacts_duplicate_icon_padding))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Error,
                            contentDescription = stringResource(R.string.import_contacts_duplicate),
                            tint = colorResource(id = R.color.text_error)
                        )
                        Text(
                            text = stringResource(R.string.import_contacts_duplicate),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorResource(id = R.color.text_error)
                        )
                    }
                } else {
                    IconButton(onClick = onToggleClick) {
                        Icon(
                            imageVector = if (contactPreviewItem.isSelected) Icons.Outlined.CheckCircle else Icons.Outlined.Circle,
                            contentDescription = if (contactPreviewItem.isSelected)
                                stringResource(R.string.import_contacts_deselect)
                            else
                                stringResource(R.string.import_contacts_select),
                            tint = if (contactPreviewItem.isSelected)
                                colorResource(id = R.color.import_action_primary)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}