package com.example.netarchive.ui.screens.add_contact_screen

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netarchive.data.local.db.entity.CategoryEntity
import com.example.netarchive.data.repository.CategoryRepository
import com.example.netarchive.data.repository.ContactRepository
import com.example.netarchive.domain.model.Contact
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import com.example.netarchive.R
import com.example.netarchive.data.local.FileManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.URLDecoder

@Stable
data class ContactFormState(
    val username: String = "",
    val phone: String = "",
    val telegram: String = "",
    val max: String = "",
    val email: String = "",
    val job: String = "",
    val avatar: String = "",
    val birthday: Long? = null,
    val description: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isQrImport: Boolean =false,
    val error: String? = null
)


@HiltViewModel
class AddContactViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ContactRepository,
    private val categoryRepository: CategoryRepository,
    private val fileManager: FileManager
) : ViewModel() {

    val allCategories: StateFlow<List<CategoryEntity>> =
        categoryRepository.allCategories
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _selectedCategories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val selectedCategories: StateFlow<List<CategoryEntity>> = _selectedCategories

    private val _formState = MutableStateFlow(ContactFormState())
    val formState: StateFlow<ContactFormState> = _formState.asStateFlow()

    private val _imagePickerResult = MutableStateFlow<android.net.Uri?>(null)
    val imagePickerResult: StateFlow<android.net.Uri?> = _imagePickerResult

    fun onImagePickerResult(uri: android.net.Uri?) {
        _imagePickerResult.value = uri
    }

    fun clearImagePickerResult() {
        _imagePickerResult.value = null
    }

    fun openQrImport(){
        _formState.value = _formState.value.copy(isQrImport = true)
    }

    fun closeQrImport(){
        _formState.value = _formState.value.copy(isQrImport = false)
    }

    fun onQrUrlChange(data: String) {
        try {
            val decodedData = URLDecoder.decode(data, "UTF-8")

            val params: Map<String, String> = decodedData.split(';').associate { param ->
                val parts = param.split('=', limit = 2)
                parts[0] to (parts.getOrNull(1) ?: "")
            }

            _formState.value = _formState.value.copy(
                username = params["u"] ?: "",
                phone = params["p"] ?: "",
                email = params["e"] ?: "",
                telegram = params["t"] ?: "",
                max = params["m"] ?: "",
                job = params["j"] ?: "",
                birthday = params["b"]?.toLongOrNull(),
                error = null
            )
            closeQrImport()

        } catch (e: Exception) {
            e.printStackTrace()
            _formState.value = _formState.value.copy(
                error = context.getString(R.string.error_qr_parse, e.message ?: "")
            )
            closeQrImport()
        }
    }

    fun onUsernameChange(value: String) {
        if (_formState.value.username != value) {
            _formState.value = _formState.value.copy(username = value)
        }
    }

    fun onPhoneChange(value: String) {
        if (_formState.value.phone != value) {
            _formState.value = _formState.value.copy(phone = value)
        }
    }

    fun onTelegramChange(value: String) {
        if (_formState.value.telegram != value) {
            _formState.value = _formState.value.copy(telegram = value)
        }
    }

    fun onMaxChange(value: String) {
        if (_formState.value.max != value) {
            _formState.value = _formState.value.copy(max = value)
        }
    }

    fun onEmailChange(value: String) {
        if (_formState.value.email != value) {
            _formState.value = _formState.value.copy(email = value)
        }
    }

    fun onJobChange(value: String) {
        if (_formState.value.job != value) {
            _formState.value = _formState.value.copy(job = value)
        }
    }

    fun onAvatarChange(uri: android.net.Uri) {
        val oldAvatarUri = _formState.value.avatar

        viewModelScope.launch {
            try {
                val localUri = fileManager.copyImageToInternalStorage(uri)

                if (oldAvatarUri.isNotBlank()) {
                    fileManager.deleteFile(oldAvatarUri)
                }

                _formState.value = _formState.value.copy(avatar = localUri)
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    error = context.getString(R.string.error_photo_load, e.message ?: "")
                )
            }
        }
    }
    fun onBirthdayChange(timestamp: Long?) {
        _formState.value = _formState.value.copy(birthday = timestamp)
    }

    fun onDescriptionChange(value: String) {
        _formState.value = _formState.value.copy(description = value)
    }

    fun saveContact() {
        val state = _formState.value

        if (state.username.isBlank()) {
            _formState.value = state.copy(error = context.getString(R.string.error_name_required_full))
            return
        }

        viewModelScope.launch {
            _formState.value = state.copy(isLoading = true, error = null)

            try {
                val contact = Contact(
                    username = state.username.trim(),
                    phone = state.phone.trim().takeIf { it.isNotBlank() },
                    telegram = state.telegram.trim().takeIf { it.isNotBlank() },
                    max = state.max.trim().takeIf { it.isNotBlank() },
                    email = state.email.trim().takeIf { it.isNotBlank() },
                    job = state.job.trim().takeIf { it.isNotBlank() },
                    avatar = state.avatar.trim().takeIf { it.isNotBlank() },
                    birthday = state.birthday,
                    description = state.description.takeIf { it.isNotBlank() }
                )

                val contactId = repository.addContact(contact)

                _selectedCategories.value.forEach { category ->
                    repository.addCategoryToContact(contactId, category.id)
                }

                _formState.value = ContactFormState(isSuccess = true)
            } catch (e: Exception) {
                _formState.value = state.copy(
                    isLoading = false,
                    error = context.getString(R.string.error_save, e.message ?: "")
                )
            }
        }
    }

    fun resetForm() {
        _formState.value = ContactFormState()
    }

    fun clearError() {
        _formState.value = _formState.value.copy(error = null)
    }

    fun createCategory(name: String) {
        viewModelScope.launch {
            val categoryId = categoryRepository.createCategoryIfNotExists(name)
            if (categoryId > 0) {
                val newCategory = categoryRepository.getCategoryById(categoryId)
                newCategory?.let {
                    _selectedCategories.value = _selectedCategories.value + it
                }
            }
        }
    }

    fun addCategory(category: CategoryEntity) {
        if (category !in _selectedCategories.value) {
            _selectedCategories.value = _selectedCategories.value + category
        }
    }

    fun removeCategory(category: CategoryEntity) {
        _selectedCategories.value = _selectedCategories.value - category
    }

    fun setSelectedCategories(categories: List<CategoryEntity>) {
        _selectedCategories.value = categories
    }
}

