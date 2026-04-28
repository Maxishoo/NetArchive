package com.example.netarchive.ui.screens.add_note_screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.app.DatePickerDialog
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.ui.res.stringResource
import com.example.netarchive.R
import com.example.netarchive.ui.components.cards.ActionButtonsSaveCancel
import com.example.netarchive.ui.components.cards.SimpleContactCard
import com.example.netarchive.ui.components.cards.DateTimeSelector

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
fun CreateNoteScreen(
    contactId: Int,
    contactName: String,
    contactAvatar: String?,
    noteId: Int = 0,
    noteText: String = "",
    noteDate: Long = 0L,
    viewModel: CreateNoteViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNoteCreated: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onNoteCreated()
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
                    Text(text = if (state.isEditMode) stringResource(R.string.modify_note) else stringResource(
                            R.string.add_note,
                        ),
                        style = MaterialTheme.typography.headlineLarge,
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.openVoicePage() }) {
                        Icon(
                            imageVector = Icons.Rounded.Mic,
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
                contactAvatar = contactAvatar
            )
            DateTimeSelector(
                selectedDate = state.date,
                onDateClick = { showDatePicker = true }
            )

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
            OutlinedTextField(
                value = state.noteText,
                onValueChange = viewModel::onNoteTextChange,
                label = { Text(stringResource(R.string.note_text)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                maxLines = Int.MAX_VALUE
            )

            Spacer(modifier = Modifier.weight(1f))

            ActionButtonsSaveCancel(
                onCancelClick = {
                    if (vibrator.hasVibrator()) {
                        vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                    onBackClick()
                },
                onSaveClick = {
                    if (vibrator.hasVibrator()) {
                        vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                    viewModel.saveNote()
                },
                saveButtonText = if (state.isEditMode) stringResource(R.string.save) else stringResource(
                    R.string.create
                ),
                isEnabled = state.noteText.isNotBlank(),
                isLoading = state.isLoading
            )
        }
    }
    if (state.isVoicePageOpen) {
        VoiceRecordingScreen()
    }
}