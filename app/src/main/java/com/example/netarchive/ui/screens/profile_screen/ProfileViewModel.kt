package com.example.netarchive.ui.screens.profile_screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netarchive.data.local.FileManager
import com.example.netarchive.data.repository.ProfileRepository
import com.example.netarchive.domain.model.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ✅ FIX: Пустые строки вместо пробелов для консистентности
data class ProfileViewState(
    val username: String = "",
    val phone: String = "",
    val telegram: String = "",
    val max: String = "",
    val email: String = "",
    val job: String = "",
    val avatar: String = "",
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val hasChanges: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val isProfileCreated: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val fileManager: FileManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private var originalProfile: Profile? = null

    private val _viewState = MutableStateFlow(ProfileViewState())
    val viewState: StateFlow<ProfileViewState> = _viewState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            repository.getProfile().collect { profile ->
                if (profile != null) {
                    originalProfile = profile
                    if (!_viewState.value.isEditMode) {
                        _viewState.update {
                            it.copy(
                                isProfileCreated = true,
                                username = profile.username,
                                phone = profile.phone.orEmpty(),
                                telegram = profile.telegram.orEmpty(),
                                max = profile.max.orEmpty(),
                                email = profile.email.orEmpty(),
                                job = profile.job.orEmpty(),
                                avatar = profile.avatar.orEmpty(),
                                isLoading = false
                            )
                        }
                    }
                } else {
                    if (!_viewState.value.isEditMode) {
                        _viewState.update {
                            it.copy(
                                isProfileCreated = false,
                                isEditMode = false,
                                username = "",
                                phone = "",
                                telegram = "",
                                max = "",
                                email = "",
                                job = "",
                                avatar = "",
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun createProfile() {
        _viewState.update {
            it.copy(
                isEditMode = true,
                isProfileCreated = false,
                username = "",
                phone = "",
                telegram = "",
                max = "",
                email = "",
                job = "",
                avatar = "",
                hasChanges = true
            )
        }
    }

    // ✅ FIX: Сравнение с .orEmpty() для консистентности
    private fun hasChanges(state: ProfileViewState): Boolean {
        return originalProfile?.let { original ->
            state.username != original.username ||
                    state.phone != original.phone.orEmpty() ||
                    state.telegram != original.telegram.orEmpty() ||
                    state.max != original.max.orEmpty() ||
                    state.email != original.email.orEmpty() ||
                    state.job != original.job.orEmpty() ||
                    state.avatar != original.avatar.orEmpty()
        } ?: true
    }

    fun onAvatarChange(uri: android.net.Uri) {
        val oldAvatarUri = _viewState.value.avatar

        viewModelScope.launch {
            try {
                // ✅ FIX: Убедитесь, что fileManager возвращает абсолютный путь к файлу
                val localPath = fileManager.copyImageToInternalStorage(uri)
                    .trim() // убираем возможные пробелы

                // Удаляем старое изображение, если есть
                if (oldAvatarUri.isNotBlank() && oldAvatarUri != localPath) {
                    fileManager.deleteFile(oldAvatarUri)
                }

                _viewState.update { it.copy(avatar = localPath) }
            } catch (e: Exception) {
                _viewState.update {
                    it.copy(error = "Ошибка загрузки фото: ${e.message}")
                }
            }
        }
    }

    private fun updateField(update: ProfileViewState.() -> ProfileViewState) {
        _viewState.update { state ->
            val newState = state.update()
            newState.copy(hasChanges = hasChanges(newState))
        }
    }

    fun onUsernameChange(value: String) = updateField { copy(username = value) }
    fun onPhoneChange(value: String) = updateField { copy(phone = value) }
    fun onTelegramChange(value: String) = updateField { copy(telegram = value) }
    fun onMaxChange(value: String) = updateField { copy(max = value) }
    fun onEmailChange(value: String) = updateField { copy(email = value) }
    fun onJobChange(value: String) = updateField { copy(job = value) }

    fun saveProfile() {
        if (_viewState.value.username.isBlank()) {
            _viewState.update { it.copy(error = "Имя обязательно") }
            return
        }

        viewModelScope.launch {
            try {
                val state = _viewState.value
                val profile = Profile(
                    username = state.username.trim(), // ✅ FIX: убран пробел в ключе
                    phone = state.phone.ifEmpty { null },
                    telegram = state.telegram.ifEmpty { null },
                    max = state.max.ifEmpty { null },
                    email = state.email.ifEmpty { null },
                    job = state.job.ifEmpty { null },
                    avatar = state.avatar.ifEmpty { null }
                )

                repository.saveProfile(profile)
                // ✅ originalProfile обновится в loadProfile(), но можно и здесь
                originalProfile = profile

                _viewState.update {
                    it.copy(
                        isProfileCreated = true,
                        isEditMode = false,
                        hasChanges = false,
                        isSuccess = true
                    )
                }
            } catch (e: Exception) {
                _viewState.update { it.copy(error = e.message) }
            }
        }
    }

    fun enableEditMode() {
        _viewState.update { it.copy(isEditMode = true) }
    }

    fun resetSuccessFlag() {
        _viewState.update { it.copy(isSuccess = false) }
    }

    fun clearError() {
        _viewState.update { it.copy(error = null) }
    }
}