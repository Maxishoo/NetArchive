package com.example.netarchive.ui.screens.add_contact_screen

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

data class ContactFormState(
    val username: String = "",
    val phone: String = "",
    val telegram: String = "",
    val max: String = "",
    val email: String = "",
    val job: String = "",
    val avatar: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddContactViewModel @Inject constructor(
    private val repository: ContactRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(ContactFormState())
    val formState: StateFlow<ContactFormState> = _formState.asStateFlow()

    fun onUsernameChange(value: String) {
        _formState.value = _formState.value.copy(username = value)
    }

    fun onPhoneChange(value: String) {
        _formState.value = _formState.value.copy(phone = value)
    }

    fun onTelegramChange(value: String) {
        _formState.value = _formState.value.copy(telegram = value)
    }

    fun onMaxChange(value: String) {
        _formState.value = _formState.value.copy(max = value)
    }

    fun onEmailChange(value: String) {
        _formState.value = _formState.value.copy(email = value)
    }

    fun onJobChange(value: String) {
        _formState.value = _formState.value.copy(job = value)
    }

    fun onAvatarChange(value: String) {
        _formState.value = _formState.value.copy(avatar = value)
    }

    fun saveContact() {
        val state = _formState.value

        if (state.username.isBlank()) {
            _formState.value = state.copy(error = "Имя обязательно для заполнения")
            return
        }

        viewModelScope.launch {
            _formState.value = state.copy(isLoading = true, error = null)

            try {
                val contact = Contact(
                    username = state.username.trim(),
                    phone = state.phone.trim().takeIf { it.isNotBlank() },
                    telegram = state.telegram.trim().takeIf { it.isNotBlank() },
                    max = state.max.trim().takeIf { it.isNotBlank() },
                    email = state.email.trim().takeIf { it.isNotBlank() },
                    job = state.job.trim().takeIf { it.isNotBlank() },
                    avatar = state.avatar.trim().takeIf { it.isNotBlank() }
                )

                repository.addContact(contact)
                _formState.value = ContactFormState(isSuccess = true)
            } catch (e: Exception) {
                _formState.value = state.copy(
                    isLoading = false,
                    error = "Ошибка при сохранении: ${e.message}"
                )
            }
        }
    }

    fun resetForm() {
        _formState.value = ContactFormState()
    }
}
