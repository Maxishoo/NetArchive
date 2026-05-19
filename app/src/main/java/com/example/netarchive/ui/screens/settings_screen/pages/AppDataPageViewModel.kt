package com.example.netarchive.ui.screens.settings_screen.pages

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netarchive.R
import com.example.netarchive.data.repository.ContactRepository
import com.example.netarchive.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

sealed class DataSettingsError {
    object UnknownTable : DataSettingsError()
    data class ClearError(val tableName: String, val cause: String?) : DataSettingsError()
    data class SizeLoadError(val cause: String?) : DataSettingsError()
}

data class DataSettingsState(
    val dbSize: String = "0 B",
    val isLoading: Boolean = false,
    val successMessage: Int? = null,
    val error: DataSettingsError? = null
)

@HiltViewModel
class DataSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contactRepository: ContactRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DataSettingsState())
    val state: StateFlow<DataSettingsState> = _state.asStateFlow()

    init { loadDatabaseSize() }

    fun loadDatabaseSize() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sizeBytes = getDatabaseSize(context)
                _state.update { it.copy(dbSize = formatBytes(sizeBytes)) }
            } catch (e: Exception) {
                _state.update { it.copy(error = DataSettingsError.SizeLoadError(e.message)) }
            }
        }
    }

    fun clearTable(tableName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                when (tableName) {
                    "contacts" -> contactRepository.deleteAllContacts()
                    "profile" -> profileRepository.deleteProfile()
                    else -> throw IllegalArgumentException("Unknown table")
                }
                loadDatabaseSize()
                val successRes = when (tableName) {
                    "contacts" -> R.string.data_settings_contacts_cleared
                    "profile" -> R.string.data_settings_profile_cleared
                    else -> R.string.data_settings_table_cleared
                }
                _state.update { it.copy(isLoading = false, successMessage = successRes) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = DataSettingsError.ClearError(tableName, e.message)
                    )
                }
            }
        }
    }

    fun resetMessages() {
        _state.update { it.copy(successMessage = null, error = null) }
    }

    private fun getDatabaseSize(context: Context): Long {
        val dbFile = context.getDatabasePath("archive.db")
        return if (dbFile.exists()) dbFile.length() else 0L
    }

    private fun formatBytes(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1048576 -> String.format(Locale.US, "%.2f KB", bytes / 1024.0)
        else -> String.format(Locale.US, "%.2f MB", bytes / 1048576.0)
    }
}