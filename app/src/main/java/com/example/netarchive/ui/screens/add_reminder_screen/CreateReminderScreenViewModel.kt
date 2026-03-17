package com.example.netarchive.ui.screens.add_reminder_screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netarchive.data.repository.ReminderRepository
import com.example.netarchive.domain.model.Reminder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    val date: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false,
    val textLength: Int = 0,
    val hasDateError: Boolean = false,
    val hasTextError: Boolean = false
)

@HiltViewModel
class CreateReminderViewModel @Inject constructor(
    private val repository: ReminderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val contactId: Int = checkNotNull(savedStateHandle["contactId"]) { "contactId required" }
    private val contactName: String = checkNotNull(savedStateHandle["contactName"]) { "contactName required" }
    private val contactAvatar: String? = savedStateHandle["contactAvatar"]

    private val reminderId: Int? = savedStateHandle["reminderId"]
    private val reminderText: String? = savedStateHandle["reminderText"]
    private val reminderDate: Long? = savedStateHandle["reminderDate"]

    private val _state = MutableStateFlow(
        CreateReminderState(
            contactId = contactId,
            contactName = contactName,
            contactAvatar = contactAvatar,
            reminderId = reminderId ?: 0,
            reminderText = reminderText ?: "",
            textLength = (reminderText ?: "").length,
            date = if (reminderDate == null || reminderDate == 0L) {
                System.currentTimeMillis()
            } else {
                reminderDate
            },
            isEditMode = reminderId != null && reminderId > 0
        )
    )
    val state: StateFlow<CreateReminderState> = _state.asStateFlow()
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

    fun onDateChange(selectedDate: Long) {
        val now = System.currentTimeMillis()
        val maxFutureDate = now + (ReminderValidation.MAX_FUTURE_DAYS * ReminderValidation.MILLIS_PER_DAY)

        val hasError = selectedDate < now || selectedDate > maxFutureDate

        _state.value = _state.value.copy(
            date = when {
                selectedDate < now -> now
                else -> selectedDate
            },
            hasDateError = hasError,
            error = when {
                selectedDate < now -> "Напоминание не может быть в прошлом"
                else -> null
            }
        )
    }

    fun saveReminder() {
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
        if (currentState.date < now) {
            _state.value = currentState.copy(
                error = "Дата не может быть в прошлом",
                hasDateError = true
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
                    date = currentState.date
                )

                if (currentState.isEditMode && currentState.reminderId > 0) {
                    repository.updateReminder(reminder)
                } else {
                    repository.addReminder(reminder)
                }
                _state.value = currentState.copy(
                    isLoading = false,
                    isSuccess = true,
                    error = null
                )

            } catch (e: Exception) {
                _state.value = currentState.copy(
                    isLoading = false,
                    isSuccess = false,
                    error = "Ошибка при сохранении: ${e.message ?: "Неизвестная ошибка"}"
                )
            }
        }
    }

    fun setReminderData(reminderId: Int, reminderText: String, reminderDate: Long) {
        _state.value = _state.value.copy(
            reminderId = reminderId,
            reminderText = reminderText,
            textLength = reminderText.length,
            date = reminderDate,
            isEditMode = true,
            hasTextError = false,
            hasDateError = false
        )
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
                    date = currentState.date
                )

                repository.deleteReminder(reminder)
                _state.value = currentState.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    isLoading = false,
                    error = "Ошибка при удалении: ${e.message}"
                )
            }
        }
    }
    fun clearError() {
        _state.value = _state.value.copy(
            error = null,
            hasTextError = false,
            hasDateError = false
        )
    }
    fun resetSuccess() {
        _state.value = _state.value.copy(isSuccess = false)
    }
}