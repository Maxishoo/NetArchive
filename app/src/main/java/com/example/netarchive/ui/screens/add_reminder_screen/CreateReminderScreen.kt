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
import androidx.compose.ui.res.stringResource
import com.example.netarchive.R
import com.example.netarchive.ui.components.*
import com.example.netarchive.ui.components.cards.ActionButtonsSaveCancel
import com.example.netarchive.ui.components.cards.DateTimeSelector
import com.example.netarchive.ui.components.cards.DateTimeSelector_with_valid
import com.example.netarchive.ui.components.cards.SimpleContactCard
import java.util.Calendar

const val MAX_TEXT_LENGTH = 500


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
    ).apply {
        datePicker.minDate = System.currentTimeMillis() - 1000
    }

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

    val stableContactName = remember { contactName }
    val stableContactAvatar = remember { contactAvatar }

    val onDateClick = remember { { showDatePicker = true } }
    val onDismissDatePicker = remember { { showDatePicker = false } }
    val onSaveClick = remember(viewModel) { { viewModel.saveReminder() } }

    val isSaveEnabled by remember(
        state.reminderText,
        state.isLoading,
        state.hasTextError,
        state.hasDateError
    ) {
        derivedStateOf {
            state.reminderText.isNotBlank() &&
                    !state.isLoading &&
                    !state.hasTextError &&
                    !state.hasDateError
        }
    }

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
                    Text(stringResource(R.string.add_reminder))
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close)
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
            SimpleContactCard(
                contactName = stableContactName,
                contactAvatar = stableContactAvatar
            )

            DateTimeSelector_with_valid(
                selectedDate = state.date,
                onDateClick = onDateClick
            )

            if (showDatePicker) {
                ShowDatePickerDialog(
                    initialDate = state.date,
                    onDateSelected = { selectedDate ->
                        viewModel.onDateChange(selectedDate)
                        showDatePicker = false
                    },
                    onDismiss = onDismissDatePicker
                )
            }

            ReminderTextField(
                value = state.reminderText,
                onValueChange = viewModel::onReminderTextChange,
                textLength = state.textLength,
                maxLength = MAX_TEXT_LENGTH,
                isError = state.hasTextError
            )

            Spacer(modifier = Modifier.weight(1f))

            ActionButtonsSaveCancel(
                onCancelClick = onBackClick,
                onSaveClick = onSaveClick,
                saveButtonText = stringResource(R.string.save),
                isEnabled = isSaveEnabled,
                isLoading = state.isLoading
            )
        }
    }
}

@Composable
private fun ReminderTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textLength: Int = 0,
    maxLength: Int = MAX_TEXT_LENGTH,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.reminder_text)) },
        placeholder = { Text(stringResource(R.string.reminder_text)) },
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        maxLines = Int.MAX_VALUE,
        isError = isError,
        supportingText = {
            Text(
                text = "$textLength / $maxLength",
                color = if (isError || textLength > maxLength) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                }
            )
        }
    )
}