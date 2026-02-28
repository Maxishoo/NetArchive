package com.example.netarchive.ui.screens.contacts_list_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netarchive.data.repository.ContactRepository
import com.example.netarchive.domain.model.Contact
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


sealed class LoadState<out T> {
    object Loading : LoadState<Nothing>()
    data class Success<T>(val data: T) : LoadState<T>()
    data class Error(val message: String) : LoadState<Nothing>()
    object Empty : LoadState<Nothing>()
}

@HiltViewModel
class ContactListViewModel @Inject constructor(
    private val repository: ContactRepository
) : ViewModel() {

    private val _state = MutableStateFlow<LoadState<List<Contact>>>(LoadState.Loading)
    val state: StateFlow<LoadState<List<Contact>>> = _state.asStateFlow()

    init {
        loadContacts()
    }

    private fun loadContacts() {
        repository.getAllContacts()
            .catch { exception ->
                _state.value = LoadState.Error(exception.message ?: "Unknown error")
            }
            .onEach { contacts ->
                _state.value = if (contacts.isEmpty()) {
                    LoadState.Empty
                } else {
                    LoadState.Success(contacts)
                }
            }
            .launchIn(viewModelScope)
    }
}