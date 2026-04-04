package com.example.netarchive.ui.screens.profile_screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.netarchive.ui.components.QrDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileViewScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onSettingsClick: ()->Unit
) {
    val viewState by viewModel.viewState.collectAsState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let(viewModel::onAvatarChange)
    }

    LaunchedEffect(viewState.isSuccess) {
        if (viewState.isSuccess) {
            viewModel.resetSuccessFlag()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль", style = MaterialTheme.typography.headlineLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFECEBF4),
                ),
                actions = {
                    if (!viewState.showQrDialog) {
                        IconButton(onClick = viewModel::openQr) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Показать qr код"
                            )
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                modifier = Modifier.size(28.dp),
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Настройки"
                            )
                        }
                    }

                }
            )
        }
    ) { paddingValues ->
        if (viewState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (!viewState.isProfileCreated && !viewState.isEditMode) {
            NoProfile(onCreateClick = viewModel::createProfile)
        } else {
            ProfileInfo(
                viewState = viewState,
                viewModel = viewModel,
                onAvatarClick = {
                    if (viewState.isEditMode) {
                        imagePickerLauncher.launch("image/*")
                    }
                },
                context = context
            )
        }

        viewState.error?.let { error ->
            Box(modifier = Modifier.padding(16.dp)) {
                Snackbar(
                    action = {
                        TextButton(onClick = viewModel::clearError) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
    if (viewState.showQrDialog) {
        QrDialog(viewState.qrBitmap, viewModel::closeQr)
    }
}

@Composable
fun NoProfile(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PersonAdd,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Создайте профиль, чтобы делиться им с другими",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Чтобы продолжить, нажмите кнопку создать",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onCreateClick) {
            Text("Создать")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileInfo(
    viewState: ProfileViewState,
    viewModel: ProfileViewModel,
    onAvatarClick: () -> Unit = {},
    context: android.content.Context
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 80.dp, top = 100.dp),
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
                        model = ImageRequest.Builder(context)
                            .data(viewState.avatar)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar",
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
        OutlinedTextField(
            value = viewState.username,
            onValueChange = viewModel::onUsernameChange,
            label = { Text("Имя *") },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewState.isEditMode,
            singleLine = true,
            colors = profileTextFieldColors()
        )
        OutlinedTextField(
            value = viewState.phone,
            onValueChange = viewModel::onPhoneChange,
            label = { Text("Телефон") },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewState.isEditMode,
            singleLine = true,
            colors = profileTextFieldColors()
        )
        OutlinedTextField(
            value = viewState.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewState.isEditMode,
            singleLine = true,
            colors = profileTextFieldColors()
        )
        OutlinedTextField(
            value = viewState.telegram,
            onValueChange = viewModel::onTelegramChange,
            label = { Text("Telegram") },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewState.isEditMode,
            singleLine = true,
            colors = profileTextFieldColors()
        )
        OutlinedTextField(
            value = viewState.max,
            onValueChange = viewModel::onMaxChange,
            label = { Text("MAX") },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewState.isEditMode,
            singleLine = true,
            colors = profileTextFieldColors()
        )
        OutlinedTextField(
            value = viewState.job,
            onValueChange = viewModel::onJobChange,
            label = { Text("Работа") },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewState.isEditMode,
            singleLine = true,
            colors = profileTextFieldColors()
        )

        if (viewState.isEditMode) {
            Button(
                onClick = viewModel::saveProfile,
                enabled = viewState.username.isNotBlank() && viewState.hasChanges && !viewState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = if (viewState.hasChanges && viewState.username.isNotBlank())
                        Color(0xFF4D5D8A)  // Синий при активности
                    else
                        Color.Gray.copy(alpha = 0.3f), // Серый при неактивности
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
        } else if (viewState.isProfileCreated) {
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
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun profileTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF64B5F6),
    unfocusedBorderColor = Color(0xFF90CAF9),
    disabledBorderColor = Color(0xFF90CAF9),
    disabledTextColor = MaterialTheme.colorScheme.onSurface,
    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedLabelColor = Color(0xFF64B5F6),
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
)
