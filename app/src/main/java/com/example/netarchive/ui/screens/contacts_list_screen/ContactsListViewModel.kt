package com.example.netarchive.ui.screens.contacts_list_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netarchive.data.repository.ContactRepository
import com.example.netarchive.domain.model.Contact
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


sealed class LoadState<out T> {
    object Loading : LoadState<Nothing>()
    data class Success<T>(
        val data: T,
        val searchQuery: String = ""
    ) : LoadState<T>()
    data class Error(val message: String) : LoadState<Nothing>()
    object Empty : LoadState<Nothing>()
}

@HiltViewModel
class ContactListViewModel @Inject constructor(
    private val repository: ContactRepository
) : ViewModel() {
    private val searchQueryFlow = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private val contactsFlow: Flow<LoadState<List<Contact>>> =
        searchQueryFlow
            .debounce(100)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                repository.getContacts(query)
                    .map { contacts ->
                        if (contacts.isEmpty()) LoadState.Empty
                        else LoadState.Success(contacts, searchQuery = query)
                    }
                    .onStart { emit(LoadState.Loading) }
                    .catch { e -> emit(LoadState.Error(e.message ?: "Error")) }
            }
    val state: StateFlow<LoadState<List<Contact>>> = contactsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LoadState.Loading
    )

    fun onSearchQueryChange(query: String) {
        searchQueryFlow.value = query
    }
}