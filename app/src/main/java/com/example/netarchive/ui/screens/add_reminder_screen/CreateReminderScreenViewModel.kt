package com.example.netarchive.ui.screens.add_reminder_screen

import androidx.compose.runtime.Stable
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

@Stable
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
    val isEditMode: Boolean = false
)

@HiltViewModel
class CreateReminderViewModel @Inject constructor(
    private val repository: ReminderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val contactId: Int = checkNotNull(savedStateHandle["contactId"])
    private val contactName: String = checkNotNull(savedStateHandle["contactName"])
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
        _state.value = _state.value.copy(reminderText = value)
    }

    fun saveReminder() {
        val currentState = _state.value
        if (currentState.reminderText.isBlank()) {
            _state.value = currentState.copy(error = "Напоминание не может быть пустым")
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
                _state.value = currentState.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    isLoading = false,
                    error = "Ошибка при сохранении: ${e.message}"
                )
            }
        }
    }

    fun setContactData(contactId: Int, contactName: String, contactAvatar: String? = null) {
        _state.value = _state.value.copy(
            contactId = contactId,
            contactName = contactName,
            contactAvatar = contactAvatar
        )
    }

    fun setReminderData(reminderId: Int, reminderText: String, reminderDate: Long) {
        _state.value = _state.value.copy(
            reminderId = reminderId,
            reminderText = reminderText,
            date = reminderDate,
            isEditMode = true
        )
    }

    fun deleteReminder() {
        val currentState = _state.value

        if (currentState.reminderId <= 0) return

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

    fun onDateChange(date: Long) {
        _state.value = _state.value.copy(date = date)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}