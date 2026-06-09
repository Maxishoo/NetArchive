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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.netarchive.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.netarchive.ui.components.QrDialog
import com.example.netarchive.ui.theme.AppTheme
import com.example.netarchive.ui.theme.appOutlinedTextFieldColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.runtime.setValue
import androidx.compose.material3.rememberDatePickerState
import java.util.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue

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
                title = { Text(stringResource(R.string.profile_title), style = MaterialTheme.typography.headlineLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.topBarBackground,
                ),
                actions = {
                    if (!viewState.showQrDialog) {
                        IconButton(onClick = viewModel::openQr) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = stringResource(R.string.profile_show_qr)
                            )
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                modifier = Modifier.size(28.dp),
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = stringResource(R.string.profile_settings)
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.profile_create_hint),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.profile_create_continue),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onCreateClick) {
            Text(stringResource(R.string.create))
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
            label = { Text(stringResource(R.string.profile_name_label)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewState.isEditMode,
            singleLine = true,
            colors = appOutlinedTextFieldColors()
        )

        ProfileBirthdayField(
            timestamp = viewState.birthday,
            onDateSelected = viewModel::onBirthdayChange,
            isEditMode = viewState.isEditMode
        )

        OutlinedTextField(
            value = viewState.phone,
            onValueChange = viewModel::onPhoneChange,
            label = { Text(stringResource(R.string.profile_phone_label)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewState.isEditMode,
            singleLine = true,
            colors = appOutlinedTextFieldColors()
        )
        OutlinedTextField(
            value = viewState.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewState.isEditMode,
            singleLine = true,
            colors = appOutlinedTextFieldColors()
        )
        OutlinedTextField(
            value = viewState.telegram,
            onValueChange = viewModel::onTelegramChange,
            label = { Text("Telegram") },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewState.isEditMode,
            singleLine = true,
            colors = appOutlinedTextFieldColors()
        )
        OutlinedTextField(
            value = viewState.max,
            onValueChange = viewModel::onMaxChange,
            label = { Text("MAX") },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewState.isEditMode,
            singleLine = true,
            colors = appOutlinedTextFieldColors()
        )
        OutlinedTextField(
            value = viewState.job,
            onValueChange = viewModel::onJobChange,
            label = { Text(stringResource(R.string.profile_job_label)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewState.isEditMode,
            singleLine = true,
            colors = appOutlinedTextFieldColors()
        )

        if (viewState.isEditMode) {
            Button(
                onClick = viewModel::saveProfile,
                enabled = viewState.username.isNotBlank() && viewState.hasChanges && !viewState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = if (viewState.hasChanges && viewState.username.isNotBlank())
                        AppTheme.colors.primaryAction
                    else
                        AppTheme.disabledContainerColor(),
                    disabledContainerColor = AppTheme.disabledContainerColor()
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.save),
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
                    text = stringResource(R.string.edit),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileBirthdayField(
    timestamp: Long?,
    onDateSelected: (Long?) -> Unit,
    isEditMode: Boolean,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Форматируем дату для отображения
    val displayText = timestamp?.let {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it))
    } ?: ""

    LaunchedEffect(isFocused, isEditMode) {
        if (isFocused && isEditMode) {
            showDatePicker = true
        }
    }

    OutlinedTextField(
        value = displayText,
        onValueChange = { }, // ReadOnly
        label = { Text(stringResource(R.string.profile_birthday_label)) },
        placeholder = { Text("") },
        modifier = modifier.fillMaxWidth(),
        enabled = isEditMode,
        readOnly = true,
        singleLine = true,
        interactionSource = interactionSource,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = stringResource(R.string.pick_date),
                tint = MaterialTheme.colorScheme.primary,
                modifier = modifier.clickable(enabled = isEditMode) {
                    showDatePicker = true  // ✅ Клик по иконке тоже работает
                }
            )
        },
        colors = appOutlinedTextFieldColors()
    )


    // ✅ Используем нативный диалог как в заметках
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

    val dialog = android.app.DatePickerDialog(
        LocalContext.current,
        { _, selectedYear, selectedMonth, selectedDay ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDay)
                // ✅ Обнуляем время, чтобы была только дата
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

