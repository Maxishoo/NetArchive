package com.example.netarchive.ui.screens.settings_screen

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import com.example.netarchive.R

data class SettingsViewState(
    val selectedPage: Int = 0,
    val topBarTextResId: Int = R.string.settings_title,
    val isDarkTheme: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _viewState = MutableStateFlow(SettingsViewState())
    val viewState: StateFlow<SettingsViewState> = _viewState.asStateFlow()

    fun changeSelectedPage(newPage: Int, titleResId: Int = R.string.settings_title) {
        _viewState.update { it.copy(selectedPage = newPage, topBarTextResId = titleResId) }
    }

    fun setDarkTheme(isDark: Boolean) {
        _viewState.update { it.copy(isDarkTheme = isDark) }
    }
}