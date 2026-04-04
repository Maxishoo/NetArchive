package com.example.netarchive.ui.screens.settings_screen

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SettingsViewState(
    val selectedPage: Int = 0,
    val topBarText: String = "Настройки",
)

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _viewState = MutableStateFlow(SettingsViewState())
    val viewState: StateFlow<SettingsViewState> = _viewState.asStateFlow()

    fun backClick() {
        _viewState.value = _viewState.value.copy(selectedPage = 0)
    }

    fun changeSelectedPage(newPage: Int) {
        _viewState.value = _viewState.value.copy(selectedPage = newPage)
    }
}