package com.example.netarchive.ui.screens.reminder_list_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netarchive.data.repository.ReminderRepository
import com.example.netarchive.domain.model.ReminderContact
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoadState<out T> {
    object Loading : LoadState<Nothing>()
    object Empty : LoadState<Nothing>()
    data class Error(val message: String) : LoadState<Nothing>()
    data class Success<T>(val data: T) : LoadState<T>()
}

enum class SortingMode {
    BY_DATE,
    BY_CONTACT_THEN_DATE
}

@HiltViewModel
class ReminderListViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _sortingMode = MutableStateFlow(SortingMode.BY_DATE)
    val sortingMode: StateFlow<SortingMode> = _sortingMode.asStateFlow()

    private val remindersWithContactFlow = reminderRepository.getRemindersWithContact()
        .catch { e -> emit(emptyList()) }

    val state: StateFlow<LoadState<List<ReminderContact>>> = combine(
        remindersWithContactFlow,
        _sortingMode
    ) { reminders, mode ->
        if (reminders.isEmpty()) {
            LoadState.Empty
        } else {
            val sorted = when (mode) {
                SortingMode.BY_DATE -> reminders.sortedBy { it.reminder.date }
                SortingMode.BY_CONTACT_THEN_DATE -> reminders.sortedWith(
                    compareBy(
                        { it.contact?.username ?: "" },
                        { it.reminder.date }
                    )
                )
            }
            LoadState.Success(sorted)
        }
    }.onStart { emit(LoadState.Loading) }
        .catch { e -> emit(LoadState.Error(e.message ?: "Unknown error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadState.Loading
        )

    fun setSortingMode(mode: SortingMode) {
        _sortingMode.value = mode

    }

    fun deleteReminders(reminderIds: List<Int>) {
        viewModelScope.launch {
            reminderRepository.deleteRemindersByIds(reminderIds)
        }
    }
}

