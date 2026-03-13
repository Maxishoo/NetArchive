package com.example.netarchive.ui.screens.contact_view_screen

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netarchive.data.local.db.entity.CategoryEntity
import com.example.netarchive.data.repository.CategoryRepository
import com.example.netarchive.data.repository.ContactRepository
import com.example.netarchive.domain.model.Contact
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.netarchive.data.repository.NoteRepository
import com.example.netarchive.domain.model.Note
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File

data class ContactViewState(
    val contactId: Int = 0,
    val username: String = "",
    val phone: String = "",
    val telegram: String = "",
    val max: String = "",
    val email: String = "",
    val job: String = "",
    val avatar: String = "",
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val hasChanges: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ContactViewViewModel @Inject constructor(
    private val repository: ContactRepository,
    private val noteRepository: NoteRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val contactId: Int = checkNotNull(savedStateHandle["contactId"])

    private val _viewState = MutableStateFlow(ContactViewState(contactId = contactId))
    val viewState: StateFlow<ContactViewState> = _viewState.asStateFlow()

    private val originalState = MutableStateFlow<ContactViewState?>(null)

    val allCategories: StateFlow<List<CategoryEntity>> =
        categoryRepository.allCategories
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val selectedCategories: StateFlow<List<CategoryEntity>> = _selectedCategories.asStateFlow()
    init {
        loadContact()
        loadNotes()
    }

    private fun loadContact() {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isLoading = true, notes = emptyList())
            try {

                repository.getContactWithCategories(contactId).collect { contactWithCategories ->
                    contactWithCategories?.let {
                        _viewState.value = _viewState.value.copy(
                            isLoading = false,
                            contactId = it.contact.id,
                            username = it.contact.username,
                            phone = it.contact.phone ?: "",
                            telegram = it.contact.telegram ?: "",
                            max = it.contact.max ?: "",
                            email = it.contact.email ?: "",
                            job = it.contact.job ?: "",
                            avatar = it.contact.avatar ?: ""
                        )

                        _selectedCategories.value = it.categories
                        originalState.value = _viewState.value.copy(isLoading = false)
                    } ?: run {
                        _viewState.value = _viewState.value.copy(
                            isLoading = false,
                            error = "Контакт не найден"
                        )
                    }
                }
            } catch (e: Exception) {
                _viewState.value = _viewState.value.copy(
                    isLoading = false,
                    error = "Ошибка при загрузке: ${e.message}"
                )
            }
        }
    }
    private fun loadNotes() {
        viewModelScope.launch {
            noteRepository.getNotesByContactId(contactId).collect { notes ->
                _viewState.value = _viewState.value.copy(notes = notes)
            }
        }
    }

    fun enableEditMode() {
        _viewState.value = _viewState.value.copy(isEditMode = true)
    }

    fun disableEditMode() {
        originalState.value?.let { original ->
            _viewState.value = original.copy(isEditMode = false)
        } ?: run {
            _viewState.value = _viewState.value.copy(isEditMode = false)
        }
    }

    fun onUsernameChange(value: String) {
        _viewState.value = _viewState.value.copy(username = value, hasChanges = true)
    }

    fun onPhoneChange(value: String) {
        _viewState.value = _viewState.value.copy(phone = value, hasChanges = true)
    }

    fun onTelegramChange(value: String) {
        _viewState.value = _viewState.value.copy(telegram = value, hasChanges = true)
    }

    fun onMaxChange(value: String) {
        _viewState.value = _viewState.value.copy(max = value, hasChanges = true)
    }

    fun onEmailChange(value: String) {
        _viewState.value = _viewState.value.copy(email = value, hasChanges = true)
    }

    fun onJobChange(value: String) {
        _viewState.value = _viewState.value.copy(job = value, hasChanges = true)
    }

    private fun copyAvatarToInternalStorage(uri: android.net.Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Cannot open URI")

        val fileName = "avatar_${System.currentTimeMillis()}.jpg"
        val outputFile = File(context.filesDir, "avatars/$fileName").apply {
            parentFile?.mkdirs()
        }

        inputStream.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return outputFile.toURI().toString()
    }
    fun onAvatarSelected(uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val localUri = copyAvatarToInternalStorage(uri)
                _viewState.value = _viewState.value.copy(
                    avatar = localUri,
                    hasChanges = true
                )
            } catch (e: Exception) {
                _viewState.value = _viewState.value.copy(
                    error = "Ошибка загрузки фото: ${e.message}"
                )
            }
        }
    }
    fun createCategory(name: String) {
        viewModelScope.launch {
            android.util.Log.d("ContactViewVM", "=== createCategory START: $name ===")


            val previouslySelected = _selectedCategories.value.toList()

            try {
                val categoryId = categoryRepository.createCategoryIfNotExists(name)

                if (categoryId > 0) {
                    kotlinx.coroutines.delay(100)

                    val newCategory = categoryRepository.getCategoryById(categoryId)

                    newCategory?.let { category ->

                        val alreadyExists = previouslySelected.any { it.id == category.id }

                        if (!alreadyExists) {
                            val newList = previouslySelected + category
                            _selectedCategories.value = newList
                            _viewState.value = _viewState.value.copy(hasChanges = true)

                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ContactViewVM", "Error creating category", e)
            }

        }
    }

    fun addCategory(category: CategoryEntity) {
        viewModelScope.launch {
            val currentSelected = _selectedCategories.value.toMutableList()


            if (currentSelected.none { it.id == category.id }) {
                currentSelected.add(category)
                _selectedCategories.value = currentSelected
                _viewState.value = _viewState.value.copy(hasChanges = true)

            }
        }
    }

    fun removeCategory(category: CategoryEntity) {
        viewModelScope.launch {
            val currentSelected = _selectedCategories.value.toMutableList()
            currentSelected.removeAll { it.id == category.id }
            _selectedCategories.value = currentSelected
            _viewState.value = _viewState.value.copy(hasChanges = true)

        }
    }

    fun setSelectedCategories(categories: List<CategoryEntity>) {
        viewModelScope.launch {

            if (categories.isEmpty() && _selectedCategories.value.isNotEmpty()) {
                return@launch
            }

            _selectedCategories.value = categories.toList()
            _viewState.value = _viewState.value.copy(hasChanges = true)
        }
    }

    fun saveContact() {
        val state = _viewState.value

        if (state.username.isBlank()) {
            _viewState.value = state.copy(error = "Имя обязательно для заполнения")
            return
        }

        viewModelScope.launch {
            _viewState.value = state.copy(isLoading = true, error = null)

            try {
                val contact = Contact(
                    id = state.contactId,
                    username = state.username.trim(),
                    phone = state.phone.trim().takeIf { it.isNotBlank() },
                    telegram = state.telegram.trim().takeIf { it.isNotBlank() },
                    max = state.max.trim().takeIf { it.isNotBlank() },
                    email = state.email.trim().takeIf { it.isNotBlank() },
                    job = state.job.trim().takeIf { it.isNotBlank() },
                    avatar = state.avatar.trim().takeIf { it.isNotBlank() }
                )

                repository.updateContact(contact)
                repository.updateContactCategories(
                    contactId = contactId,
                    categoryIds = _selectedCategories.value.map { it.id }
                )

                categoryRepository.deleteUnusedCustomCategories()

                val newState = state.copy(
                    isLoading = false,
                    isSuccess = true,
                    hasChanges = false,
                    isEditMode = false
                )
                _viewState.value = newState
                originalState.value = newState
            } catch (e: Exception) {
                _viewState.value = state.copy(
                    isLoading = false,
                    error = "Ошибка при сохранении: ${e.message}"
                )
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.deleteNote(note)
        }
    }
    fun clearError() {
        _viewState.value = _viewState.value.copy(error = null)
    }
    fun deleteContact(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isLoading = true)
            try {
                val currentState = _viewState.value
                val contact = Contact(
                    id = currentState.contactId,
                    username = currentState.username,
                    phone = currentState.phone,
                    telegram = currentState.telegram,
                    max = currentState.max,
                    email = currentState.email,
                    job = currentState.job,
                    avatar = currentState.avatar
                )

                repository.deleteContact(contact)


                _viewState.value = _viewState.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _viewState.value = _viewState.value.copy(
                    isLoading = false,
                    error = "Ошибка при удалении: ${e.message}"
                )
            }
        }
    }

}