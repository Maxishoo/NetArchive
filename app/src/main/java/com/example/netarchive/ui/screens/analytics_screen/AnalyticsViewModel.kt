package com.example.netarchive.ui.screens.analytics_screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netarchive.R
import com.example.netarchive.data.local.db.dao.CategoryWithCount
import com.example.netarchive.data.local.db.dao.MonthActivity
import com.example.netarchive.data.repository.AnalyticsRepository
import com.example.netarchive.domain.model.Contact
import com.example.netarchive.domain.model.OverallStats
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: AnalyticsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val sharingTimeoutMs = context.resources.getInteger(R.integer.analytics_sharing_timeout_ms).toLong()
    private val forgottenMonths = context.resources.getInteger(R.integer.analytics_forgotten_months)
    private val mostActiveLimit = context.resources.getInteger(R.integer.analytics_most_active_limit)

    val overallStats: StateFlow<OverallStats?> = repository.getOverallStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(sharingTimeoutMs), null)

    val forgottenContacts: StateFlow<List<Contact>> = repository.getForgottenContacts(forgottenMonths)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(sharingTimeoutMs), emptyList())

    val categoriesWithCount: StateFlow<List<CategoryWithCount>> =
        repository.getCategoriesWithCount()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(sharingTimeoutMs), emptyList())

    val monthlyActivity: StateFlow<List<MonthActivity>> = repository.getMonthlyActivity()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(sharingTimeoutMs), emptyList())

    val topContacts: StateFlow<List<Contact>> = repository.getMostActiveContacts(mostActiveLimit)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(sharingTimeoutMs), emptyList())

    val contactsToWrite: StateFlow<List<Contact>> =
        repository.getContactsToWrite()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(sharingTimeoutMs), emptyList())
}