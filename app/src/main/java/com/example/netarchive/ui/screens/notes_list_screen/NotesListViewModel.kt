package com.example.netarchive.ui.screens.notes_list_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netarchive.domain.model.Note
import com.example.netarchive.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    // Состояния UI
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedContactId = MutableStateFlow<Int?>(null)
    val selectedContactId: StateFlow<Int?> = _selectedContactId.asStateFlow()

    init {
        loadAllNotes()
    }

    // Загрузка всех заметок
    fun loadAllNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                noteRepository.getAllNotes()
                    .catch { e ->
                        _error.value = "Ошибка загрузки: ${e.message}"
                        emit(emptyList())
                    }
                    .collect { notesList ->
                        _notes.value = notesList
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // Загрузка заметок для конкретного контакта
    fun loadNotesByContact(contactId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _selectedContactId.value = contactId

            try {
                noteRepository.getNotesByContactId(contactId)
                    .catch { e ->
                        _error.value = "Ошибка загрузки: ${e.message}"
                        emit(emptyList())
                    }
                    .collect { notesList ->
                        _notes.value = notesList
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // Переключение режима редактирования
    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value
    }

    // Удаление заметки
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                noteRepository.deleteNote(note)
            } catch (e: Exception) {
                _error.value = "Ошибка удаления: ${e.message}"
            }
        }
    }

    // Удаление заметки по ID
    fun deleteNoteById(noteId: Int) {
        viewModelScope.launch {
            try {
                val note = noteRepository.getNoteById(noteId)
                note?.let { noteRepository.deleteNote(it) }
            } catch (e: Exception) {
                _error.value = "Ошибка удаления: ${e.message}"
            }
        }
    }

    // Добавление заметки
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
                _error.value = "Ошибка добавления: ${e.message}"
            }
        }
    }

    // Обновление заметки
    fun updateNote(note: Note) {
        viewModelScope.launch {
            try {
                noteRepository.updateNote(note)
            } catch (e: Exception) {
                _error.value = "Ошибка обновления: ${e.message}"
            }
        }
    }

    // Получение заметки по ID
    suspend fun getNoteById(noteId: Int): Note? {
        return try {
            noteRepository.getNoteById(noteId)
        } catch (e: Exception) {
            _error.value = "Ошибка загрузки заметки: ${e.message}"
            null
        }
    }

    // Очистка ошибки
    fun clearError() {
        _error.value = null
    }

    // Сброс фильтра по контакту
    fun clearContactFilter() {
        _selectedContactId.value = null
        loadAllNotes()
    }
}