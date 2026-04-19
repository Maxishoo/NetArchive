package com.example.netarchive.ui.screens.add_note_screen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
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
import java.util.Locale
import javax.inject.Inject

data class CreateNoteState(
    val contactId: Int = 0,
    val contactName: String = "",
    val contactAvatar: String? = null,
    val noteId: Int = 0,
    val noteText: String = "",
    val date: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false,

    val isVoicePageOpen: Boolean = true,
    val isVoiceRecording: Boolean = false,
    val isVoiceProcessing: Boolean = false,
    val recognizedText: String = "",
    val isVoiceRecordDone: Boolean = false
)

@HiltViewModel
class CreateNoteViewModel @Inject constructor(
    private val repository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val contactId: Int = checkNotNull(savedStateHandle["contactId"])
    private val contactName: String = checkNotNull(savedStateHandle["contactName"])
    private val contactAvatar: String? = savedStateHandle["contactAvatar"]

    private val noteId: Int? = savedStateHandle["noteId"]
    private val noteText: String? = savedStateHandle["noteText"]
    private val noteDate: Long? = savedStateHandle["noteDate"]
    private val _state = MutableStateFlow(
        CreateNoteState(
            contactId = contactId,
            contactName = contactName,
            contactAvatar = contactAvatar,
            noteId = noteId ?: 0,
            noteText = noteText ?: "",
            date = if (noteDate == null || noteDate == 0L) {
                System.currentTimeMillis()
            } else {
                noteDate
            },
            isEditMode = noteId != null && noteId > 0,
        )
    )
    val state: StateFlow<CreateNoteState> = _state.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

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
            _state.value = currentState.copy(isLoading = true)

            try {
                val note = Note(
                    id = currentState.noteId,
                    contactId = currentState.contactId,
                    text = currentState.noteText.trim(),
                    date = currentState.date
                )

                if (currentState.isEditMode && currentState.noteId > 0) {
                    repository.updateNote(note)
                } else {
                    repository.addNote(note)
                }
                _state.value = currentState.copy(isSuccess = true)
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    error = "Ошибка при сохранении: ${e.message}",
                )
            }
        }
    }

    fun setContactData(contactId: Int, contactName: String, contactAvatar: String? = null) {
        _state.value = _state.value.copy(
            contactId = contactId,
            contactName = contactName,
            contactAvatar = contactAvatar,
        )
    }

    fun setNoteData(noteId: Int, noteText: String, noteDate: Long) {
        _state.value = _state.value.copy(
            noteId = noteId,
            noteText = noteText,
            date = noteDate,
            isEditMode = true,
        )
    }

    fun deleteNote() {
        val currentState = _state.value

        if (currentState.noteId <= 0) return

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true)

            try {
                val note = Note(
                    id = currentState.noteId,
                    contactId = currentState.contactId,
                    text = currentState.noteText,
                    date = currentState.date
                )

                repository.deleteNote(note)
                _state.value = currentState.copy(isSuccess = true)
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    error = "Ошибка при удалении: ${e.message}",
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

    fun openVoicePage(){
        _state.value = _state.value.copy(isVoicePageOpen = true)
    }

    fun initialize(context: Context) {
        try {
            val isAvailable = SpeechRecognizer.isRecognitionAvailable(context)

            if (!isAvailable) {
                _state.value = _state.value.copy(
                    error = "Распознавание речи не поддерживается на этом устройстве",
                    isVoiceRecordDone = true
                )
                return
            }
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

            if (speechRecognizer == null) {
                _state.value = _state.value.copy(
                    error = "Не удалось создать SpeechRecognizer",
                    isVoiceRecordDone = true
                )
                return
            }

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                }

                override fun onBeginningOfSpeech() {
                }

                override fun onRmsChanged(rmsdB: Float) {
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                }

                override fun onEndOfSpeech() {
                }

                override fun onError(error: Int) {
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "Речь не распознана. Попробуйте еще раз."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Речь не обнаружена. Попробуйте еще раз."
                        SpeechRecognizer.ERROR_NETWORK -> "Ошибка сети. Проверьте подключение."
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Таймаут сети. Проверьте подключение."
                        SpeechRecognizer.ERROR_AUDIO -> "Ошибка записи аудио"
                        SpeechRecognizer.ERROR_CLIENT -> "Ошибка клиента"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Распознаватель занят"
                        SpeechRecognizer.ERROR_SERVER -> "Ошибка сервера"
                        else -> "Ошибка распознавания: $error"
                    }

                    _state.value = _state.value.copy(
                        isVoiceRecording = false,
                        isVoiceProcessing = false,
                        recognizedText = errorMessage,
                        isVoiceRecordDone = true
                    )
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    _state.value = _state.value.copy(
                        isVoiceRecording = false,
                        isVoiceProcessing = false,
                        isVoiceRecordDone = true,
                        recognizedText = matches?.firstOrNull() ?: "Речь не распознана"
                    )
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    _state.value = _state.value.copy(
                        recognizedText = matches?.firstOrNull() ?: ""
                    )
                }

                override fun onEvent(eventType: Int, params: Bundle?) {
                }
            })

        } catch (e: Exception) {
            _state.value = _state.value.copy(
                error = "Ошибка инициализации: ${e.message}",
                isVoiceRecordDone = true
            )
        }
    }

    fun startListening() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Говорите...")
            }
            speechRecognizer?.startListening(intent)
            _state.value = _state.value.copy(
                isVoiceRecording = true,
                recognizedText = "",
                isVoiceRecordDone = false,
                isVoiceProcessing = false
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isVoiceRecording = false,
                recognizedText = "Ошибка: ${e.message}",
                isVoiceRecordDone = true
            )
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            _state.value = _state.value.copy(
                isVoiceRecording = false,
                isVoiceProcessing = true
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isVoiceRecording = false,
                isVoiceProcessing = false,
                recognizedText = "Ошибка: ${e.message}",
                isVoiceRecordDone = true
            )
        }
    }

    fun closeRecordingPage() {
        try {
            if (_state.value.isVoiceRecording) {
                speechRecognizer?.stopListening()
            }
            _state.value = _state.value.copy(
                isVoiceRecording = false,
                recognizedText = "",
                isVoiceRecordDone = false,
                isVoiceProcessing = false,
                isVoicePageOpen = false
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isVoiceRecording = false,
                isVoiceProcessing = false,
                isVoiceRecordDone = false,
                isVoicePageOpen = false
            )
        }
    }

    fun applyRecognizedText() {
        val currentState = _state.value
        if (currentState.recognizedText.isNotBlank() && currentState.recognizedText != "Речь не распознана") {
            val newText = if (currentState.noteText.isNotBlank()) {
                "${currentState.noteText}\n${currentState.recognizedText}"
            } else {
                currentState.recognizedText
            }
            _state.value = currentState.copy(
                noteText = newText,
                recognizedText = "",
                isVoiceRecordDone = false
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {
        }
        speechRecognizer = null
    }
}