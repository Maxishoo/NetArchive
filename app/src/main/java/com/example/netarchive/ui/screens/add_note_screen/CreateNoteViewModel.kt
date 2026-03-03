package com.example.netarchive.ui.screens.add_note_screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netarchive.data.repository.NoteRepository
import com.example.netarchive.domain.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateNoteState(
    val contactId: Int = 0,
    val contactName: String = "",
    val noteText: String = "",
    val date: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateNoteViewModel @Inject constructor(
    private val repository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val contactId: Int = checkNotNull(savedStateHandle["contactId"])
    private val contactName: String = checkNotNull(savedStateHandle["contactName"])

    private val _state = MutableStateFlow(CreateNoteState(contactId = contactId, contactName = contactName))
    val state: StateFlow<CreateNoteState> = _state.asStateFlow()

    fun onNoteTextChange(value: String) {
        _state.value = _state.value.copy(noteText = value)
    }

    fun saveNote() {
        val currentState = _state.value

        if (currentState.noteText.isBlank()) {
            _state.value = currentState.copy(error = "Заметка не может быть пустой")
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, error = null)

            try {
                val note = Note(
                    contactId = currentState.contactId,
                    text = currentState.noteText.trim(),
                    date = System.currentTimeMillis()
                )

                repository.addNote(note)
                _state.value = currentState.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    isLoading = false,
                    error = "Ошибка при сохранении: ${e.message}"
                )
            }
        }
    }
    fun setContactData(contactId: Int, contactName: String) {
        _state.value = _state.value.copy(
            contactId = contactId,
            contactName = contactName
        )
    }
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
