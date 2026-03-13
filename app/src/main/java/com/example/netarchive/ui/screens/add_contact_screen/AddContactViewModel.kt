package com.example.netarchive.ui.screens.add_contact_screen

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netarchive.data.local.db.entity.CategoryEntity
import com.example.netarchive.data.repository.CategoryRepository
import com.example.netarchive.data.repository.ContactRepository
import com.example.netarchive.domain.model.Contact
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import androidx.core.net.toUri

@Stable
data class ContactFormState(
    val username: String = "",
    val phone: String = "",
    val telegram: String = "",
    val max: String = "",
    val email: String = "",
    val job: String = "",
    val avatar: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddContactViewModel @Inject constructor(
    private val repository: ContactRepository,
    private val categoryRepository: CategoryRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val allCategories: StateFlow<List<CategoryEntity>> =
        categoryRepository.allCategories
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _selectedCategories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val selectedCategories: StateFlow<List<CategoryEntity>> = _selectedCategories
    private val _formState = MutableStateFlow(ContactFormState())
    val formState: StateFlow<ContactFormState> = _formState.asStateFlow()

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

    private fun copyImageToInternalStorage(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Cannot  open URI")

        val fileName = "avatar_${System.currentTimeMillis()}.jpg"
        val outputFile = File(context.filesDir, "avatars/$fileName").apply {
            parentFile?.mkdirs()
        }

        inputStream.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return outputFile.toURI().toString() // Возвращаем file:// URI
    }

    /**
     * Обновлённая функция: теперь принимает Uri и копирует файл
     */
    fun onAvatarChange(uri: Uri) {
        _formState.value.avatar.takeIf { it.isNotBlank() }?.let { oldUri ->
            File(oldUri.toUri().path!!).delete()
        }

        viewModelScope.launch {
            try {
                val localUri = copyImageToInternalStorage(uri)
                _formState.value = _formState.value.copy(avatar = localUri)
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    error = "Ошибка загрузки фото: ${e.message}"
                )
            }
        }
    }

    fun saveContact() {
        val state = _formState.value

        if (state.username.isBlank()) {
            _formState.value = state.copy(error = "Имя обязательно для заполнения")
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
                    avatar = state.avatar.trim().takeIf { it.isNotBlank() }
                )

                val contactId = repository.addContact(contact)

                _selectedCategories.value.forEach { category ->
                    repository.addCategoryToContact(contactId, category.id)
                }

                _formState.value = ContactFormState(isSuccess = true)
            } catch (e: Exception) {
                _formState.value = state.copy(
                    isLoading = false,
                    error = "Ошибка при сохранении: ${e.message}"
                )
            }
        }
    }

    fun resetForm() {
        _formState.value = ContactFormState()
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

