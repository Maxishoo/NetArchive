package com.example.netarchive.ui.screens.add_reminder_screen

import android.app.Application
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netarchive.data.local.db.AppDatabase
import com.example.netarchive.data.repository.ReminderRepository
import com.example.netarchive.domain.model.Reminder
import com.example.netarchive.utils.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private object ReminderValidation {
    const val MAX_TEXT_LENGTH = 500
    const val MAX_FUTURE_DAYS = 365L
    const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L
}

data class CreateReminderState(
    val contactId: Int = 0,
    val contactName: String = "",
    val contactAvatar: String? = null,
    val reminderId: Int = 0,
    val reminderText: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false,
    val textLength: Int = 0,
    val hasTextError: Boolean = false,
    val hasDateError: Boolean = false,
    val hasTimeError: Boolean = false,
    val dateTimeErrorMessage: String? = null
)

@HiltViewModel
class CreateReminderViewModel @Inject constructor(
    private val application: Application,
    private val repository: ReminderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val contactId: Int = checkNotNull(savedStateHandle["contactId"]) { "contactId required" }
    private val contactName: String = checkNotNull(savedStateHandle["contactName"]) { "contactName required" }
    private val contactAvatar: String? = savedStateHandle["contactAvatar"]

    private val reminderId: Int? = savedStateHandle["reminderId"]
    private val reminderText: String? = savedStateHandle["reminderText"]
    private val reminderTimestamp: Long? = savedStateHandle["reminderDate"]

    private val _state = MutableStateFlow(
        CreateReminderState(
            contactId = contactId,
            contactName = contactName,
            contactAvatar = contactAvatar,
            reminderId = reminderId ?: 0,
            reminderText = reminderText ?: "",
            textLength = (reminderText ?: "").length,
            timestamp = if (reminderTimestamp == null || reminderTimestamp == 0L) {
                System.currentTimeMillis()
            } else {
                reminderTimestamp
            },
            isEditMode = reminderId != null && reminderId > 0
        )
    )
    val state: StateFlow<CreateReminderState> = _state.asStateFlow()

    val isSaveEnabled: StateFlow<Boolean> = _state.map { state ->
        state.reminderText.isNotBlank() &&
                !state.isLoading &&
                !state.hasTextError &&
                !state.hasDateError &&
                !state.hasTimeError
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun onReminderTextChange(value: String) {
        val trimmedValue = if (value.length > ReminderValidation.MAX_TEXT_LENGTH) {
            value.take(ReminderValidation.MAX_TEXT_LENGTH)
        } else {
            value
        }

        val hasError = trimmedValue.isBlank() || trimmedValue.length > ReminderValidation.MAX_TEXT_LENGTH

        _state.value = _state.value.copy(
            reminderText = trimmedValue,
            textLength = trimmedValue.length,
            hasTextError = hasError,
            error = when {
                trimmedValue.isBlank() -> "Текст не может быть пустым"
                trimmedValue.length > ReminderValidation.MAX_TEXT_LENGTH -> "Превышен лимит в ${ReminderValidation.MAX_TEXT_LENGTH} символов"
                else -> null
            }
        )
    }

    fun onDateSelected(selectedDateMillis: Long) {
        val currentCal = Calendar.getInstance().apply { timeInMillis = _state.value.timestamp }
        val newCal = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
            set(Calendar.HOUR_OF_DAY, currentCal.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, currentCal.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        _state.value = _state.value.copy(timestamp = newCal.timeInMillis)
        validateDateTime()
    }

    fun onTimeSelected(selectedTimeMillis: Long) {
        _state.value = _state.value.copy(timestamp = selectedTimeMillis)
        validateDateTime()
    }

    private fun validateDateTime() {
        val now = System.currentTimeMillis()
        val timestamp = _state.value.timestamp
        val isPast = timestamp < now
        val maxFuture = now + (ReminderValidation.MAX_FUTURE_DAYS * ReminderValidation.MILLIS_PER_DAY)
        val isTooFar = timestamp > maxFuture

        _state.value = _state.value.copy(
            hasDateError = isPast || isTooFar,
            hasTimeError = isPast,
            dateTimeErrorMessage = when {
                isPast -> "Указана прошедшая дата и время"
                isTooFar -> "Дата слишком далеко (макс. ${ReminderValidation.MAX_FUTURE_DAYS} дней)"
                else -> null
            }
        )
    }

    fun onTimestampChange(selectedTimestamp: Long) {
        _state.value = _state.value.copy(timestamp = selectedTimestamp)
        validateDateTime()
    }

    fun clearDateTimeErrors() {
        _state.value = _state.value.copy(
            hasDateError = false,
            hasTimeError = false,
            dateTimeErrorMessage = null
        )
    }

    fun clearError() {
        _state.value = _state.value.copy(
            error = null,
            hasTextError = false,
            hasDateError = false,
            hasTimeError = false,
            dateTimeErrorMessage = null
        )
    }

    fun resetSuccess() {
        _state.value = _state.value.copy(isSuccess = false)
    }

    fun saveReminder() {
        if (!isSaveEnabled.value) {
            return
        }

        val currentState = _state.value

        if (currentState.reminderText.isBlank()) {
            _state.value = currentState.copy(
                error = "Введите текст напоминания",
                hasTextError = true
            )
            return
        }

        if (currentState.reminderText.length > ReminderValidation.MAX_TEXT_LENGTH) {
            _state.value = currentState.copy(
                error = "Текст слишком длинный (макс. ${ReminderValidation.MAX_TEXT_LENGTH} символов)",
                hasTextError = true
            )
            return
        }

        val now = System.currentTimeMillis()
        if (currentState.timestamp < now) {
            _state.value = currentState.copy(
                error = "Дата не может быть в прошлом",
                hasDateError = true,
                hasTimeError = true,
                dateTimeErrorMessage = "Указана прошедшая дата и время"
            )
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, error = null)

            try {
                val reminder = Reminder(
                    id = currentState.reminderId,
                    contactId = currentState.contactId,
                    text = currentState.reminderText.trim(),
                    date = currentState.timestamp
                )

                val savedReminderId = if (currentState.isEditMode && currentState.reminderId > 0) {
                    repository.updateReminder(reminder)
                    currentState.reminderId
                } else {
                    repository.addReminder(reminder).toInt()
                }
                val justSaved = repository.getReminderById(savedReminderId)
                Log.d("CreateReminderVM", "🔍 Read back after save: ${justSaved?.text ?: "NULL"}")
                ReminderScheduler.scheduleReminder(
                    context = application,
                    reminderId = savedReminderId,
                    title = "Напоминание",
                    text = reminder.text,
                    timestamp = reminder.date
                )
                _state.value = currentState.copy(
                    isLoading = false,
                    isSuccess = true,
                    error = null
                )
            } catch (e: Exception) {
                Log.e("CreateReminderVM", "💥 Error saving reminder", e)
                _state.value = currentState.copy(
                    isLoading = false,
                    isSuccess = false,
                    error = "Ошибка при сохранении: ${e.message ?: "Неизвестная ошибка"}"
                )
            }
        }
    }

    fun setReminderData(reminderId: Int, reminderText: String, reminderTimestamp: Long) {
        _state.value = _state.value.copy(
            reminderId = reminderId,
            reminderText = reminderText,
            textLength = reminderText.length,
            timestamp = reminderTimestamp,
            isEditMode = true,
            hasTextError = false,
            hasDateError = false,
            hasTimeError = false,
            dateTimeErrorMessage = null
        )
        validateDateTime()
    }

    fun deleteReminder() {
        val currentState = _state.value

        if (currentState.reminderId <= 0) {
            _state.value = currentState.copy(error = "Нельзя удалить несуществующее напоминание")
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, error = null)

            try {
                val reminder = Reminder(
                    id = currentState.reminderId,
                    contactId = currentState.contactId,
                    text = currentState.reminderText,
                    date = currentState.timestamp
                )

                repository.deleteReminder(reminder)
                ReminderScheduler.cancelReminder(application, currentState.reminderId)
                _state.value = currentState.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    isLoading = false,
                    error = "Ошибка при удалении: ${e.message}"
                )
            }
        }
    }
}