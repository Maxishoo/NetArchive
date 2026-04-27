package com.example.netarchive.ui.screens.contact_view_screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
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
import kotlinx.coroutines.flow.stateIn
import dagger.hilt.android.qualifiers.ApplicationContext
import qrgenerator.generateQrCode
import java.io.File
import java.net.URLEncoder
import com.example.netarchive.BuildConfig
import com.example.netarchive.data.remote.ai.model.AiFeatureConfig
import com.example.netarchive.data.remote.ai.model.RetrofitClient
import com.example.netarchive.data.remote.ai.model.CompletionOptions
import com.example.netarchive.data.remote.ai.model.Message as AiMessage
import com.example.netarchive.data.remote.ai.model.YandexGptRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

data class ContactViewState(
    val contactId: Int = 0,
    val username: String = "",
    val phone: String = "",
    val telegram: String = "",
    val max: String = "",
    val email: String = "",
    val job: String = "",
    val avatar: String = "",
    val birthday: Long? = null,
    val description: String = "",
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val hasChanges: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val showDeleteDialog: Boolean = false,
    val deleteContactId: Int = 0,
    val isContactDeleted: Boolean = false,

    val showQrDialog: Boolean = false,
    val qrGenerating: Boolean = false,
    val qrBitmap:  ImageBitmap? = null
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

    private val _aiState = MutableStateFlow<AiState>(AiState.Idle)
    val aiState: StateFlow<AiState> = _aiState.asStateFlow()
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    // === AI METHOD ===
    fun generateConversationStarter() {
        viewModelScope.launch {
            if (!AiFeatureConfig.IS_AI_ENABLED) {
                _aiState.value = AiState.Disabled
                return@launch
            }

            _aiState.value = AiState.Loading

            val state = _viewState.value

            // Собираем контекст из контакта и заметок
            val contextText = buildString {
                append("Контакт: ${state.username}\n")
                if (state.job.isNotBlank()) append("Работа: ${state.job}\n")

                if (state.description.isNotBlank()) {
                    append("Описание: $state.description\n")
                }

                if (state.notes.isNotEmpty()) {
                    append("\nПоследние заметки:\n")
                    state.notes.take(5).forEachIndexed { index, note ->
                        val noteText = if (note.text.length > 200) {
                            note.text.take(200) + "..."
                        } else {
                            note.text
                        }
                        append("${index + 1}. $noteText\n")
                    }
                }

                if (_selectedCategories.value.isNotEmpty()) {
                    append("\nКатегории: ${_selectedCategories.value.joinToString { it.name }}")
                }
            }

            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.yandexGptApi.generateCompletion(
                        apiKey = "Api-Key ${BuildConfig.YANDEX_API_KEY}",
                        folderId = BuildConfig.YANDEX_FOLDER_ID,

                        request = YandexGptRequest(
                            //modelUri = "gpt://${BuildConfig.YANDEX_FOLDER_ID}/yandexgpt-lite/latest",
                            // или для полной версии:
                             modelUri = "gpt://${BuildConfig.YANDEX_FOLDER_ID}/yandexgpt/latest",

                            completionOptions = CompletionOptions(
                                stream = false,
                                temperature = 0.7f,
                                maxTokens = "1000"
                            ),
                            messages = listOf(
                                AiMessage(
                                    role = "system",
                                    text = """
                                    Ты — помощник по нетворкингу. Помогаешь пользователю поддержать связь с контактами.
    
                                     ЗАДАЧА:
                                    Предложи 3 варианта сообщения для мессенджера (Telegram/WhatsApp).
                                    Длина: 1-3 предложения, естественно для переписки.
                                    
                                     ТОН ПО КАТЕГОРИЯМ:
                                    - "Друг", "Знакомый" → Тёплый, дружеский, можно смайлики 
                                    - "Коллега" → Дружелюбный, рабочий, без излишней формальности
                                    - "Инвестор", "Партнёр", "Клиент" → Вежливый, уважительный, но не сухой
                                    - Нет категорий → Нейтрально-дружелюбный
                                    
                                     РАБОТА С ЗАМЕТКАМИ:
                                     - Если в заметке описано СОБЫТИЕ (встретились, сходили, обсудили) →
                                      Ссылайся на него уверенно: "Было здорово на рыбалке!",
                                    - НЕ используй "нашу/наше", если из заметки не ясно, что вы были ВМЕСТЕ
                                    - Если заметка про ЕГО событие (рассказал про, съездил, был) →
                                      Спроси с интересом: "Как твоя рыбалка? Удачно?", "Как съездил?"
                                    - Если заметка о БУДУЩЕМ (договорились, планируем) → 
                                      Спроси о статусе: "Как продвижение по тому вопросу?"
                                    - Если заметок много (3+) → Опирайся на последние 1-2, не перечисляй всё
                                    - Если заметок нет → Просто тёплое приветствие без выдумок
                                    - Если есть описание → используй для персонализации (интересы, общие темы)
                                    
                                     ВАЖНО:
                                    - НЕ выдумывай темы, которых нет в данных (проекты, встречи, сроки)
                                    - НЕ пиши канцелярит ("Уважаемый", "Прошу сообщить") — это мессенджер!
                                    - НЕ спрашивай "Как дела?" в лоб — привяжи к контексту
                                    
                                     ФОРМАТ:
                                    - 3 варианта, каждый с новой строки с тире
                                    - Без пояснений, только готовые сообщения
                                """.trimIndent()
                                ),
                                AiMessage(
                                    role = "user",
                                    text = contextText
                                )
                            )
                        )
                    )
                }

                // Парсим ответ: нейронка вернёт текст вида "- Вариант 1\n- Вариант 2..."
                val rawText = response.result?.alternatives?.firstOrNull()?.message?.text ?: ""
                val suggestions = rawText
                    .split("\n")
                    .map { it.trim().removePrefix("-").removePrefix("•").trim() }
                    .filter { it.isNotBlank() }
                    .take(3)

                if (suggestions.isNotEmpty()) {
                    _aiState.value = AiState.Success(suggestions)
                } else {
                    _aiState.value = AiState.Error("Не удалось сгенерировать подсказки")
                }

            } catch (e: Exception) {
                _aiState.value = AiState.Error("Ошибка AI: ${e.message ?: "Неизвестная ошибка"}")
            }
        }
    }
    fun regenerateSuggestions() {
        if (_isGenerating.value) return

        viewModelScope.launch {
            _isGenerating.value = true
            generateConversationStarter()
            _isGenerating.value = false
        }
    }

    fun copySuggestion(index: Int) {
        val suggestions = when(val state = _aiState.value) {
            is AiState.Success -> state.suggestions
            else -> return
        }

        if (index in suggestions.indices) {
            // Обновляем состояние, чтобы подсветить скопированный вариант
            _aiState.value = AiState.Success(suggestions, copiedIndex = index)

            // Копируем в буфер
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("ai_suggestion_${index}", suggestions[index])
            clipboard.setPrimaryClip(clip)

            // Возвращаем состояние в норму через 2 секунды (чтобы подсветка пропала)
            viewModelScope.launch {
                delay(2000)
                _aiState.value = AiState.Success(suggestions, copiedIndex = null)
            }
        }
    }

    fun resetAiState() {
        _aiState.value = AiState.Idle
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
                            avatar = it.contact.avatar ?: "",
                            birthday = it.contact.birthday,
                            description = it.contact.description ?: ""

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
    fun onBirthdayChange(timestamp: Long?) {
        _viewState.value = _viewState.value.copy(birthday = timestamp, hasChanges = true )
    }

    fun onDescriptionChange(value: String) {
        _viewState.value = _viewState.value.copy(description = value, hasChanges = true)
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
                    avatar = state.avatar.trim().takeIf { it.isNotBlank() },
                    birthday = state.birthday,
                    description = state.description.takeIf { it.isNotBlank() }
                )

                repository.updateContact(contact)
                repository.updateContactCategories(
                    contactId = contactId,
                    categoryIds = _selectedCategories.value.map { it.id }
                )

                //categoryRepository.deleteUnusedCustomCategories()

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

    fun showDeleteDialog() {
        _viewState.value = _viewState.value.copy(
            showDeleteDialog = true,
            deleteContactId = contactId
        )
    }

    fun hideDeleteDialog() {
        _viewState.value = _viewState.value.copy(showDeleteDialog = false)
    }

    fun deleteContact() {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(
                isLoading = true,
                isContactDeleted = true
            )
            try {
                val currentState = _viewState.value
                val contact = Contact(
                    id = currentState.deleteContactId,
                    username = currentState.username,
                    phone = currentState.phone,
                    telegram = currentState.telegram,
                    max = currentState.max,
                    email = currentState.email,
                    job = currentState.job,
                    avatar = currentState.avatar,
                    birthday = currentState.birthday,
                    description = currentState.description
                )

                repository.deleteContact(contact)
                hideDeleteDialog()

                _viewState.value = _viewState.value.copy(
                    isLoading = false,
                )
            } catch (e: Exception) {
                _viewState.value = _viewState.value.copy(
                    isLoading = false,
                    error = "Ошибка при удалении: ${e.message}"
                )
            }
        }
    }
    fun clearDeleteFlag() {
        _viewState.value = _viewState.value.copy(isContactDeleted = false)
    }

    fun openQr() {
        _viewState.value = _viewState.value.copy(showQrDialog = true)


        _viewState.value = _viewState.value.copy(qrGenerating = true)
        try {
            val rawData = buildString {
                append("u=${_viewState.value.username.trim()}")
                if (_viewState.value.phone.isNotBlank()) append(";p=${_viewState.value.phone}")
                if (_viewState.value.email.isNotBlank()) append(";e=${_viewState.value.email}")
                if (_viewState.value.telegram.isNotBlank()) append(";t=${_viewState.value.telegram}")
                if (_viewState.value.max.isNotBlank()) append(";m=${_viewState.value.max}")
                if (_viewState.value.job.isNotBlank()) append(";j=${_viewState.value.job}")
                if (_viewState.value.birthday != null) append(";b=${_viewState.value.birthday}")
            }
            val encodedData = URLEncoder.encode(rawData, "UTF-8")
            generateQrCode(
                url = encodedData,
                onSuccess = { info, qrCode ->
                    _viewState.value = _viewState.value.copy(
                        qrGenerating = false,
                        qrBitmap = qrCode
                    )
                },
                onFailure = {
                    _viewState.value = _viewState.value.copy(qrGenerating = false)
                },
            )
        } catch (e: Exception) {
            e.printStackTrace()
            _viewState.value = _viewState.value.copy(qrGenerating = false)
        }
    }

    fun closeQr(){
        _viewState.value = _viewState.value.copy(showQrDialog = false)
    }

}