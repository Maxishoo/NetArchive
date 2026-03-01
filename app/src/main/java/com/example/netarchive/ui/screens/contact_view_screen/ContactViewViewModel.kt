package com.example.netarchive.ui.screens.contact_view_screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netarchive.data.repository.ContactRepository
import com.example.netarchive.domain.model.Contact
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactViewState(
    val contactId: Int = 0,
    val username: String = "",
    val phone: String = "",
    val telegram: String = "",
    val max: String = "",
    val email: String = "",
    val job: String = "",
    val avatar: String = "",
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val hasChanges: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ContactViewViewModel @Inject constructor(
    private val repository: ContactRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val contactId: Int = checkNotNull(savedStateHandle["contactId"])

    private val _viewState = MutableStateFlow(ContactViewState(contactId = contactId))
    val viewState: StateFlow<ContactViewState> = _viewState.asStateFlow()

    private val originalState = MutableStateFlow<ContactViewState?>(null)

    init {
        loadContact()
    }

    private fun loadContact() {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isLoading = true)
            try {
                repository.getContactById(contactId).collect { contact ->
                    contact?.let {
                        val state = ContactViewState(
                            contactId = it.id,
                            username = it.username,
                            phone = it.phone ?: "",
                            telegram = it.telegram ?: "",
                            max = it.max ?: "",
                            email = it.email ?: "",
                            job = it.job ?: "",
                            avatar = it.avatar ?: ""
                        )
                        _viewState.value = state
                        originalState.value = state
                    } ?: run {
                        _viewState.value = _viewState.value.copy(
                            error = "Контакт не найден"
                        )
                    }
                    _viewState.value = _viewState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _viewState.value = _viewState.value.copy(
                    isLoading = false,
                    error = "Ошибка при загрузке: ${e.message}"
                )
            }
        }
    }

    fun enableEditMode() {
        _viewState.value = _viewState.value.copy(isEditMode = true)
    }

    fun disableEditMode() {
        originalState.value?.let { original ->
            _viewState.value = original.copy(isEditMode = false)
        } ?: run {
            _viewState.value = _viewState.value.copy(isEditMode = false)
        }
    }

    fun onUsernameChange(value: String) {
        _viewState.value = _viewState.value.copy(username = value, hasChanges = true)
    }

    fun onPhoneChange(value: String) {
        _viewState.value = _viewState.value.copy(phone = value, hasChanges = true)
    }

    fun onTelegramChange(value: String) {
        _viewState.value = _viewState.value.copy(telegram = value, hasChanges = true)
    }

    fun onMaxChange(value: String) {
        _viewState.value = _viewState.value.copy(max = value, hasChanges = true)
    }

    fun onEmailChange(value: String) {
        _viewState.value = _viewState.value.copy(email = value, hasChanges = true)
    }

    fun onJobChange(value: String) {
        _viewState.value = _viewState.value.copy(job = value, hasChanges = true)
    }

    fun onAvatarChange(value: String) {
        _viewState.value = _viewState.value.copy(avatar = value, hasChanges = true)
    }

    fun saveContact() {
        val state = _viewState.value

        if (state.username.isBlank()) {
            _viewState.value = state.copy(error = "Имя обязательно для заполнения")
            return
        }

        viewModelScope.launch {
            _viewState.value = state.copy(isLoading = true, error = null)

            try {
                val contact = Contact(
                    id = state.contactId,
                    username = state.username.trim(),
                    phone = state.phone.trim().takeIf { it.isNotBlank() },
                    telegram = state.telegram.trim().takeIf { it.isNotBlank() },
                    max = state.max.trim().takeIf { it.isNotBlank() },
                    email = state.email.trim().takeIf { it.isNotBlank() },
                    job = state.job.trim().takeIf { it.isNotBlank() },
                    avatar = state.avatar.trim().takeIf { it.isNotBlank() }
                )

                repository.updateContact(contact)

                val newState = state.copy(
                    isLoading = false,
                    isSuccess = true,
                    hasChanges = false,
                    isEditMode = false
                )
                _viewState.value = newState
                originalState.value = newState
            } catch (e: Exception) {
                _viewState.value = state.copy(
                    isLoading = false,
                    error = "Ошибка при сохранении: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _viewState.value = _viewState.value.copy(error = null)
    }

}