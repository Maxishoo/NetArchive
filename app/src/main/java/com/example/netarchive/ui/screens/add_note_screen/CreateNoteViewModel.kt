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
    val noteId: Int = 0,
    val noteText: String = "",
    val date: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false
)

@HiltViewModel
class CreateNoteViewModel @Inject constructor(
    private val repository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val contactId: Int = checkNotNull(savedStateHandle["contactId"])
    private val contactName: String = checkNotNull(savedStateHandle["contactName"])

    private val noteId: Int? = savedStateHandle["noteId"]
    private val noteText: String? = savedStateHandle["noteText"]
    private val noteDate: Long? = savedStateHandle["noteDate"]
    private val _state = MutableStateFlow(
        CreateNoteState(
            contactId = contactId,
            contactName = contactName,
            noteId = noteId ?: 0,           // <-- Используем из savedStateHandle
            noteText = noteText ?: "",      // <-- Используем из savedStateHandle
            date = if (noteDate == null || noteDate == 0L) {
                System.currentTimeMillis()
            } else {
                noteDate
            },
            isEditMode = noteId != null && noteId > 0  // <-- Авто-режим редактирования
        )
    )
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
                    id = currentState.noteId,
                    contactId = currentState.contactId,
                    text = currentState.noteText.trim(),
                    date = currentState.date
                )

                if (currentState.isEditMode && currentState.noteId > 0) {
                    // Редактирование существующей заметки
                    repository.updateNote(note)
                } else {
                    // Создание новой заметки
                    repository.addNote(note)
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
    fun setContactData(contactId: Int, contactName: String) {
        _state.value = _state.value.copy(
            contactId = contactId,
            contactName = contactName
        )
    }
    fun setNoteData(noteId: Int, noteText: String, noteDate: Long) {
        _state.value = _state.value.copy(
            noteId = noteId,
            noteText = noteText,
            date = noteDate,
            isEditMode = true
        )
    }
    fun deleteNote() {
        val currentState = _state.value

        if (currentState.noteId <= 0) return

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, error = null)

            try {
                val note = Note(
                    id = currentState.noteId,
                    contactId = currentState.contactId,
                    text = currentState.noteText,
                    date = currentState.date
                )

                repository.deleteNote(note)
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
