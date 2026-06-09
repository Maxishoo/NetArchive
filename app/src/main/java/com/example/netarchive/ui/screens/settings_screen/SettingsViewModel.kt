package com.example.netarchive.ui.screens.settings_screen

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.example.netarchive.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SettingsViewState(
    val selectedPage: Int = 0,
    @StringRes val topBarTextRes: Int = R.string.settings_title,
)

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _viewState = MutableStateFlow(SettingsViewState())
    val viewState: StateFlow<SettingsViewState> = _viewState.asStateFlow()

    fun changeSelectedPage(newPage: Int, @StringRes topBarTextRes: Int = R.string.settings_title) {
        _viewState.value = _viewState.value.copy(selectedPage = newPage, topBarTextRes = topBarTextRes)
    }
}
