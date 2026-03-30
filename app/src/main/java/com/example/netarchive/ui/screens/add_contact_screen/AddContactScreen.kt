package com.example.netarchive.ui.screens.add_contact_screen

import android.util.Log
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.netarchive.ui.theme.NetArchiveTheme
import kotlinx.coroutines.flow.map

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

    // Деструктуризация стейта для удобной передачи в подфункции
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
    Box(
        modifier = Modifier
        .fillMaxWidth()
        .background(color = Color(0xFFECEBF4).copy(alpha = 0.95f))
        .padding(top = 30.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
    ){
        ContactHeader(
            title = "Добавить контакт",
            onBackClick = onBackClick
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .padding(top = 90.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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

        if (error != null) {
            ErrorSection(errorMessage = error)
        }

        Spacer(modifier = Modifier.weight(1f))

        ContactActions(
            isLoading = isLoading,
            isUsernameFilled = username.isNotBlank(),
            onBackClick = onBackClick,
            onSaveClick = viewModel::saveContact
        )
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
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .padding(bottom = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
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
        singleLine = true,
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