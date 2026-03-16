package com.example.netarchive.ui.screens.add_reminder_screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.app.DatePickerDialog
import com.example.netarchive.ui.components.*
import java.util.Calendar

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
fun CreateReminderScreen(
    contactId: Int,
    contactName: String,
    contactAvatar: String?,
    reminderId: Int = 0,
    reminderText: String = "",
    reminderDate: Long = 0L,
    viewModel: CreateReminderViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onReminderCreated: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onReminderCreated()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Добавьте напоминание")
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Карточка контакта
            ContactCard(
                contactName = contactName,
                contactAvatar = contactAvatar
            )

            // Дата
            DateTimeSelector(
                selectedDate = state.date,
                onDateClick = { showDatePicker = true }
            )

            // DatePicker Dialog
            if (showDatePicker) {
                ShowDatePickerDialog(
                    initialDate = state.date,
                    onDateSelected = { selectedDate ->
                        viewModel.onDateChange(selectedDate)
                        showDatePicker = false
                    },
                    onDismiss = {
                        showDatePicker = false
                    }
                )
            }

            // Текст напоминания
            OutlinedTextField(
                value = state.reminderText,
                onValueChange = viewModel::onReminderTextChange,
                label = { Text("Текст напоминания") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                maxLines = Int.MAX_VALUE
            )

            Spacer(modifier = Modifier.weight(1f))

            // Кнопки
            ActionButtons(
                onCancelClick = onBackClick,
                onSaveClick = {
                    viewModel.saveReminder()
                },
                saveButtonText = "Сохранить",
                isEnabled = state.reminderText.isNotBlank(),
                isLoading = state.isLoading
            )
        }
    }
}