package com.example.netarchive.ui.screens.add_contact_screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.netarchive.ui.components.CategorySelector
import com.example.netarchive.ui.components.QrScannerDialog
import com.example.netarchive.ui.theme.NetArchiveTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import android.app.DatePickerDialog
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*


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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    viewModel: AddContactViewModel = hiltViewModel(),
    onContactCreated: () -> Unit,
    onBackClick: () -> Unit
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val imagePickerResult by viewModel.imagePickerResult.collectAsStateWithLifecycle()

    val username = formState.username
    val avatar = formState.avatar
    val isLoading = formState.isLoading
    val isSuccess = formState.isSuccess
    val error = formState.error

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onImagePickerResult(uri)
    }

    LaunchedEffect(imagePickerResult) {
        imagePickerResult?.let { uri ->
            viewModel.onAvatarChange(uri)
            viewModel.clearImagePickerResult()
        }
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            onContactCreated()
        }
    }
    if (formState.isQrImport) {
        QrScannerDialog(
            onQrUrlChange = { scannedData ->
                viewModel.onQrUrlChange(scannedData)
            },
            onCloseClick = {
                viewModel.closeQrImport()
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .padding(top = 90.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AvatarSelector(
                avatarUri = avatar,
                onClick = { imagePickerLauncher.launch("image/*") }
            )

            CategorySelector(
                allCategories = allCategories,
                selectedCategories = selectedCategories,
                onCategoriesChanged = viewModel::setSelectedCategories,
                onCreateCategory = viewModel::createCategory,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            UsernameField(
                value = username,
                onValueChange = viewModel::onUsernameChange,
                isError = error != null && username.isBlank()
            )
            BirthdayField(
                timestamp = formState.birthday,
                onDateSelected = viewModel::onBirthdayChange,
                isEditMode = true
            )

            PhoneField(
                value = formState.phone,
                onValueChange = viewModel::onPhoneChange
            )
            EmailField(
                value = formState.email,
                onValueChange = viewModel::onEmailChange
            )
            TelegramField(
                value = formState.telegram,
                onValueChange = viewModel::onTelegramChange
            )
            MaxField(
                value = formState.max,
                onValueChange = viewModel::onMaxChange
            )
            JobField(
                value = formState.job,
                onValueChange = viewModel::onJobChange
            )
            DescriptionField(
                value = formState.description,
                onValueChange = viewModel::onDescriptionChange,
            )


            if (error != null) {
                ErrorSection(errorMessage = error)
            }

            Spacer(modifier = Modifier.weight(1f))

            ContactActions(
                isLoading = isLoading,
                isUsernameFilled = username.isNotBlank(),
                onBackClick = onBackClick,
                onSaveClick = viewModel::saveContact,
                onQrAddClick = viewModel::openQrImport
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFFECEBF4).copy(alpha = 0.95f))
                .padding(top = 30.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
        ) {
            ContactHeader(
                title = "Добавить контакт",
                onBackClick = onBackClick
            )
        }
    }
}


@Composable
private fun ContactHeader(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = "Добавить контакт",
        style = MaterialTheme.typography.headlineLarge,
        color = Color.Black
    )
}

@Composable
private fun ErrorSection(
    errorMessage: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = errorMessage,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun ContactActions(
    isLoading: Boolean,
    isUsernameFilled: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onQrAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onQrAddClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Filled.QrCode,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "По QR коду",
            style = MaterialTheme.typography.labelLarge
        )
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onBackClick,
            modifier = Modifier.weight(1f),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text("Отмена")
        }

        Button(
            onClick = onSaveClick,
            modifier = Modifier.weight(1f),
            enabled = !isLoading && isUsernameFilled
        ) {
            if (isLoading) {
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


@Composable
private fun UsernameField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val supportingText = if (isError) "Обязательное поле" else null

    FormTextField(
        value = value,
        onValueChange = onValueChange,
        label = "Имя *",
        isError = isError,
        supportingText = supportingText,
        modifier = modifier
    )
}

@Composable
private fun PhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FormTextField(
        value = value,
        onValueChange = onValueChange,
        label = "Телефон",
        modifier = modifier
    )
}

@Composable
private fun EmailField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FormTextField(
        value = value,
        onValueChange = onValueChange,
        label = "Email",
        modifier = modifier
    )
}

@Composable
private fun TelegramField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FormTextField(
        value = value,
        onValueChange = onValueChange,
        label = "Telegram",
        modifier = modifier
    )
}

@Composable
private fun MaxField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FormTextField(
        value = value,
        onValueChange = onValueChange,
        label = "MAX",
        modifier = modifier
    )
}

@Composable
private fun JobField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FormTextField(
        value = value,
        onValueChange = onValueChange,
        label = "Место работы",
        modifier = modifier
    )
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = label != "Описание",
        minLines = if (label == "Описание") 3 else 1,
        maxLines = if (label == "Описание") 5 else 1,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isError)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.primary
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthdayField(
    timestamp: Long?,
    onDateSelected: (Long?) -> Unit,
    isEditMode: Boolean,
    isError: Boolean = false,
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
            focusedBorderColor = if (isError)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.primary
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
private fun DescriptionField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FormTextField(
        value = value,
        onValueChange = onValueChange,
        label = "Описание",
        modifier = modifier,
        isError = false
    )
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