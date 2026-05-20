package com.example.netarchive.ui.screens.add_reminder_screen

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import android.widget.NumberPicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.netarchive.R
import com.example.netarchive.ui.theme.AppTheme
import com.example.netarchive.ui.components.cards.ActionButtonsSaveCancel
import com.example.netarchive.ui.components.cards.DateTimeSelector_with_valid
import com.example.netarchive.ui.components.cards.SimpleContactCard
import com.example.netarchive.ui.components.cards.TimeSelectorCard
import com.example.netarchive.utils.rememberDefaultVibrator
import com.example.netarchive.utils.vibrateOneShotShort
import java.util.*

const val MAX_TEXT_LENGTH = 500

@Composable
private fun NumberPickerCard(
    curvalue: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        AndroidView(
            factory = { context ->
                NumberPicker(context).apply {
                    minValue = range.first
                    maxValue = range.last
                    wrapSelectorWheel = true
                    value = curvalue
                    setOnValueChangedListener { _, _, newVal ->
                        onValueChange(newVal)
                    }
                }
            },
            modifier = Modifier.width(80.dp).height(120.dp),
            update = { picker ->
                if (picker.value != curvalue) {
                    picker.value = curvalue
                }
            }
        )
    }
}

@Composable
private fun TimePickerDialogContent(
    initialTime: Long,
    selectedDate: Long,
    onTimeSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var hour by remember {
        mutableIntStateOf(
            if (initialTime > 0) {
                Calendar.getInstance().apply { timeInMillis = initialTime }.get(Calendar.HOUR_OF_DAY)
            } else {
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            }
        )
    }
    var minute by remember {
        mutableIntStateOf(
            if (initialTime > 0) {
                Calendar.getInstance().apply { timeInMillis = initialTime }.get(Calendar.MINUTE)
            } else {
                Calendar.getInstance().get(Calendar.MINUTE)
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_time)) },
        text = {
            Column(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(280.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NumberPickerCard(
                        curvalue = hour,
                        onValueChange = { hour = it },
                        range = 0..23,
                        label = stringResource(R.string.time_hour_label)
                    )
                    Text(":", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 24.dp))
                    NumberPickerCard(
                        curvalue = minute,
                        onValueChange = { minute = it },
                        range = 0..59,
                        label = stringResource(R.string.time_minute_label)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val selectedCalendar = Calendar.getInstance().apply {
                        if (selectedDate > 0) {
                            timeInMillis = selectedDate
                        }
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    onTimeSelected(selectedCalendar.timeInMillis)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReminderScreen(
    viewModel: CreateReminderViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onReminderCreated: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isSaveEnabled by viewModel.isSaveEnabled.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val onDateClick: () -> Unit = remember { { showDatePicker = true } }
    val onTimeClick: () -> Unit = remember { { showTimePicker = true } }

    val onDateSelected: (Long) -> Unit = remember { { selectedMillis ->
        viewModel.onDateSelected(selectedMillis)
        showDatePicker = false
    } }

    val onTimeSelected: (Long) -> Unit = remember { { selectedMillis ->
        viewModel.onTimeSelected(selectedMillis)
        showTimePicker = false
    } }

    val onDismissPickers: () -> Unit = remember { {
        showDatePicker = false
        showTimePicker = false
        viewModel.clearDateTimeErrors()
    } }

    val context = LocalContext.current
    val vibrator = rememberDefaultVibrator()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onReminderCreated()
            viewModel.resetSuccess()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.dateTimeErrorMessage) {
        state.dateTimeErrorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = if (state.isEditMode) stringResource(R.string.modify_reminder) else stringResource(
                        R.string.add_reminder,
                    ),
                        style = MaterialTheme.typography.headlineLarge,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.topBarBackground()
                ),
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
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SimpleContactCard(
                contactName = state.contactName,
                contactAvatar = state.contactAvatar
            )
            DateTimeSelector_with_valid(
                selectedDate = state.timestamp,
                onDateClick = onDateClick,
                isError = state.hasDateError,

                )
            TimeSelectorCard(
                selectedTime = state.timestamp,
                onTimeClick = onTimeClick,
                isError = state.hasTimeError,
                errorMessage = state.dateTimeErrorMessage
            )


            if (showDatePicker) {
                DatePickerDialogWrapper(
                    initialDate = state.timestamp,
                    onDateSelected = onDateSelected,
                    onDismiss = onDismissPickers
                )
            }

            if (showTimePicker) {
                TimePickerDialogContent(
                    initialTime = state.timestamp,
                    selectedDate = state.timestamp,
                    onTimeSelected = onTimeSelected,
                    onDismiss = onDismissPickers
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
                onCancelClick = {
                    vibrator.vibrateOneShotShort()
                    onBackClick()
                },
                onSaveClick = {
                    vibrator.vibrateOneShotShort()
                    viewModel.saveReminder()
                },
                saveButtonText = stringResource(R.string.save),
                isEnabled = isSaveEnabled,
                isLoading = state.isLoading
            )
        }
    }
}

@Composable
private fun DatePickerDialogWrapper(
    initialDate: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply {
        timeInMillis = if (initialDate > 0) initialDate else System.currentTimeMillis()
    }

    DisposableEffect(Unit) {
        val dialog = DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val selectedCal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                    if (initialDate > 0) {
                        val initCal = Calendar.getInstance().apply { timeInMillis = initialDate }
                        set(Calendar.HOUR_OF_DAY, initCal.get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, initCal.get(Calendar.MINUTE))
                    }
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onDateSelected(selectedCal.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        dialog.datePicker.minDate = startOfToday - 1000L

        dialog.setOnDismissListener { onDismiss() }
        dialog.show()

        onDispose { dialog.dismiss() }
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