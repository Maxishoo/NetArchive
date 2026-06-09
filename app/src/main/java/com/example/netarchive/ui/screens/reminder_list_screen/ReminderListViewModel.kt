package com.example.netarchive.ui.screens.reminder_list_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.app.Application
import com.example.netarchive.data.remote.calendar.GoogleCalendarSyncService
import com.example.netarchive.data.repository.ReminderRepository
import com.example.netarchive.domain.model.ReminderContact
import com.example.netarchive.utils.ReminderScheduler
import com.example.netarchive.widget.WidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.example.netarchive.utils.RefreshBus

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
    private val application: Application,
    private val reminderRepository: ReminderRepository,
    private val calendarSync: GoogleCalendarSyncService,
) : ViewModel() {

    private val _sortingMode = MutableStateFlow(SortingMode.BY_DATE)
    val sortingMode: StateFlow<SortingMode> = _sortingMode.asStateFlow()

    private val _refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val refreshSignal = merge(
        RefreshBus.refreshFlow,
        _refreshTrigger
    ).onStart { emit(Unit) }

    val state: StateFlow<LoadState<List<ReminderContact>>> = refreshSignal
        .flatMapLatest { _ ->
            reminderRepository.getRemindersWithContact()
                .map { reminders ->
                    val mode = _sortingMode.value
                    if (reminders.isEmpty()) LoadState.Empty
                    else {
                        val sorted = when (mode) {
                            SortingMode.BY_DATE -> reminders.sortedBy { it.reminder.date }
                            SortingMode.BY_CONTACT_THEN_DATE -> reminders.sortedWith(
                                compareBy(
                                    { it.contact?.username?.lowercase() ?: "" },
                                    { it.reminder.date }
                                )
                            )
                        }
                        LoadState.Success(sorted)
                    }
                }
                .onStart { emit(LoadState.Loading) }
                .catch { e -> emit(LoadState.Error(e.message ?: "Unknown error")) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadState.Loading
        )

    fun setSortingMode(mode: SortingMode) {
        _sortingMode.value = mode
        _refreshTrigger.tryEmit(Unit)
    }

    fun deleteReminders(reminderIds: List<Int>) {
        viewModelScope.launch {
            val reminders = reminderRepository.getRemindersByIds(reminderIds)
            calendarSync.syncAfterDelete(reminders)
            reminderIds.forEach { id ->
                ReminderScheduler.cancelReminder(application, id)
            }
            reminderRepository.deleteRemindersByIds(reminderIds)
            WidgetUpdater.refresh(application)
            _refreshTrigger.tryEmit(Unit)
        }
    }
}