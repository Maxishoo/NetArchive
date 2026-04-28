package com.example.netarchive.ui.screens.analytics_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netarchive.data.local.db.dao.CategoryWithCount
import com.example.netarchive.data.local.db.dao.MonthActivity
import com.example.netarchive.data.repository.AnalyticsRepository
import com.example.netarchive.domain.model.Contact
import com.example.netarchive.domain.model.OverallStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: AnalyticsRepository
) : ViewModel() {

    val overallStats: StateFlow<OverallStats?> = repository.getOverallStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    val forgottenContacts: StateFlow<List<Contact>> = repository.getForgottenContacts(6)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoriesWithCount: StateFlow<List<CategoryWithCount>> =
        repository.getCategoriesWithCount()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyActivity: StateFlow<List<MonthActivity>> = repository.getMonthlyActivity()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topContacts: StateFlow<List<Contact>> = repository.getMostActiveContacts(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contactsToWrite: StateFlow<List<Contact>> =
        repository.getContactsToWrite() // <-- Без аргумента
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}