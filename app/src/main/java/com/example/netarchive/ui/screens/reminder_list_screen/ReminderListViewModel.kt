package com.example.netarchive.ui.screens.reminder_list_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netarchive.domain.model.Note
import com.example.netarchive.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val error: String? = null,
    val selectedContactId: Int? = null
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()



    /**
     * Универсальный метод загрузки.
     * @param contactId Если null - грузим все заметки, иначе - фильтруем по контакту.
     */
    fun loadNotes(contactId: Int?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, selectedContactId = contactId) }

            val notesFlow = if (contactId == null) {
                noteRepository.getAllNotes()
            } else {
                noteRepository.getNotesByContactId(contactId)
            }

            notesFlow
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Ошибка загрузки: ${e.message}",
                            notes = emptyList()
                        )
                    }
                }
                .onCompletion {
                    // Если поток завершился без ошибки через catch, снимаем лоадер
                    // Но так как у нас есть catch выше, сюда попадем в любом случае после emit
                }
                .collect { notesList ->
                    _uiState.update {
                        it.copy(
                            notes = notesList,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                noteRepository.deleteNote(note)
                loadNotes(null)

            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Ошибка удаления: ${e.message}") }
            }
        }
    }

    fun addNote(text: String, contactId: Int) {
        viewModelScope.launch {
            try {
                val newNote = Note(
                    id = 0,
                    contactId = contactId,
                    text = text,
                    date = System.currentTimeMillis()
                )
                noteRepository.addNote(newNote)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Ошибка добавления: ${e.message}") }
            }
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            try {
                noteRepository.updateNote(note)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Ошибка обновления: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearContactFilter() {
        loadNotes(null)
    }
}