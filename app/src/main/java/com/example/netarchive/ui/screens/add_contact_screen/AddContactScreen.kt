package com.example.netarchive.ui.screens.add_contact_screen

import android.util.Log
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

    val isLoading by viewModel.formState
        .map { it.isLoading }
        .collectAsState(initial = false)

    val isSuccess by viewModel.formState
        .map { it.isSuccess }
        .collectAsState(initial = false)

    val error by viewModel.formState
        .map { it.error }
        .collectAsState(initial = null)

    val username by viewModel.formState
        .map { it.username }
        .collectAsState(initial = "")

    val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавить контакт", style = MaterialTheme.typography.headlineLarge) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("X", style = MaterialTheme.typography.headlineLarge)
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
            CategorySelector(
                allCategories = allCategories,
                selectedCategories = selectedCategories,
                onCategoriesChanged = { categories ->
                    viewModel.setSelectedCategories(categories)
                },
                onCreateCategory = { name ->
                    viewModel.createCategory(name)
                },
                modifier = Modifier.padding(vertical = 8.dp)
            )

            UsernameField(viewModel = viewModel)
            PhoneField(viewModel = viewModel)
            EmailField(viewModel = viewModel)
            TelegramField(viewModel = viewModel)
            MaxField(viewModel = viewModel)
            JobField(viewModel = viewModel)

            error?.let { errorMessage ->
                Text(
                    text = errorMessage,
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
                    .padding(bottom = 90.dp),
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
                    onClick = { viewModel.saveContact() },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && username.isNotBlank()
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
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            onContactCreated()
        }
    }
}


@Composable
private fun UsernameField(viewModel: AddContactViewModel) {

    val value by viewModel.formState
        .map { it.username }
        .collectAsState(initial = "")

    val isError by viewModel.formState
        .map { it.error != null && it.username.isBlank() }
        .collectAsState(initial = false)

    val supportingText = remember(isError) {
        if (isError) "Обязательное поле" else null
    }

    val onValueChange = remember { viewModel::onUsernameChange }

    FormTextField(
        value = value,
        onValueChange = onValueChange,
        label = "Имя *",
        isError = isError,
        supportingText = supportingText
    )
}

@Composable
private fun PhoneField(viewModel: AddContactViewModel) {

    val value by viewModel.formState
        .map { it.phone }
        .collectAsState(initial = "")

    FormTextField(
        value = value,
        onValueChange = viewModel::onPhoneChange,
        label = "Телефон"
    )
}

@Composable
private fun EmailField(viewModel: AddContactViewModel) {

    val value by viewModel.formState
        .map { it.email }
        .collectAsState(initial = "")

    FormTextField(
        value = value,
        onValueChange = viewModel::onEmailChange,
        label = "Email"
    )
}

@Composable
private fun TelegramField(viewModel: AddContactViewModel) {


    val value by viewModel.formState
        .map { it.telegram }
        .collectAsState(initial = "")

    FormTextField(
        value = value,
        onValueChange = viewModel::onTelegramChange,
        label = "Telegram"
    )
}

@Composable
private fun MaxField(viewModel: AddContactViewModel) {

    val value by viewModel.formState
        .map { it.max }
        .collectAsState(initial = "")

    FormTextField(
        value = value,
        onValueChange = viewModel::onMaxChange,
        label = "MAX"
    )
}

@Composable
private fun JobField(viewModel: AddContactViewModel) {


    val value by viewModel.formState
        .map { it.job }
        .collectAsState(initial = "")

    FormTextField(
        value = value,
        onValueChange = viewModel::onJobChange,
        label = "Место работы"
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
    val labelContent = @Composable { Text(label) }
    val supportingContent = supportingText?.let { text ->
        @Composable { Text(text) }
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = labelContent,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        isError = isError,
        supportingText = supportingContent,
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