package com.example.netarchive.ui.screens.settings_screen.pages

import android.content.Context
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netarchive.R
import com.example.netarchive.data.repository.ContactRepository
import com.example.netarchive.domain.model.Contact
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ContactPreviewItem(
    val deviceContactId: Long,
    val contact: Contact,
    val isSelected: Boolean,
    val isDuplicate: Boolean,
)

data class ImportContactsState(
    val isMainPage: Boolean = true,
    val isImportFromContacts: Boolean = false,
    val isContactsListLoading: Boolean = false,
    val isContactsListSaving: Boolean = false,
    val previewContacts: List<ContactPreviewItem> = emptyList(),
    val successMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class ImportContactsViewModel @Inject constructor(
    private val repository: ContactRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(ImportContactsState())
    val state: StateFlow<ImportContactsState> = _state.asStateFlow()

    fun startImport() {
        _state.update { it.copy(isMainPage = false, isImportFromContacts = true) }
        loadDeviceContacts()
    }

    private suspend fun fetchContacts(): List<Pair<Long, Contact>> = withContext(Dispatchers.IO) {
        val contactsMap = mutableMapOf<Long, Contact>()
        val resolver = context.contentResolver

        resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI
            ), null, null, null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val deviceId = cursor.getLong(0)
                val name = cursor.getString(1)?.takeIf { it.isNotBlank() } ?: context.getString(R.string.import_no_name)
                val phone = cursor.getString(2)
                val avatar = cursor.getString(3)

                contactsMap[deviceId] =
                    Contact(id = 0, username = name, phone = phone, avatar = avatar)
            }
        }

        resolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Email.CONTACT_ID,
                ContactsContract.CommonDataKinds.Email.DATA
            ), null, null, null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val email = cursor.getString(1)
                contactsMap[id]?.let { contactsMap[id] = it.copy(email = email) }
            }
        }

        return@withContext contactsMap.toList()
    }

    private fun normalizePhone(phone: String?): String =
        phone?.replace(Regex("[^0-9]"), "")?.takeLast(10) ?: ""

    private fun loadDeviceContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isContactsListLoading = true, error = null) }
            try {
                val contacts = fetchContacts()
                val existingPhones = repository.getContactsPhones()

                val phonesInDb = existingPhones
                    .mapNotNull { normalizePhone(it).takeIf { it.isNotBlank() } }
                    .toSet()

                val previewItems = contacts.map { (deviceId, contact) ->
                    val normPhone = normalizePhone(contact.phone)
                    val isDuplicate = normPhone in phonesInDb

                    ContactPreviewItem(
                        deviceContactId = deviceId,
                        contact = contact,
                        isSelected = !isDuplicate,
                        isDuplicate = isDuplicate
                    )
                }

                _state.update {
                    it.copy(
                        isContactsListLoading = false,
                        previewContacts = previewItems
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isContactsListLoading = false,
                        error = e.message ?: context.getString(R.string.import_error)
                    )
                }
            }
        }
    }

    fun toggleSelection(deviceContactId: Long) {
        _state.update { currentState ->
            currentState.copy(
                previewContacts = currentState.previewContacts.map { item ->
                    if (item.deviceContactId == deviceContactId) item.copy(isSelected = !item.isSelected)
                    else item
                }
            )
        }
    }

    fun toggleChangeSelectedAll(selected: Boolean) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isContactsListLoading = true
                )
            }

            _state.update { currentState ->
                currentState.copy(
                    previewContacts = currentState.previewContacts.map { item ->
                        if(!item.isDuplicate) item.copy(isSelected = selected)
                        else item
                    }
                )
            }

            _state.update {
                it.copy(
                    isContactsListLoading = false
                )
            }
        }
    }

    fun saveSelectedContacts() {
        val selected = _state.value.previewContacts.filter { it.isSelected && !it.isDuplicate }
            .map { it.contact }
        if (selected.isEmpty()) {
            _state.update { it.copy(error = context.getString(R.string.import_none_selected)) }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isContactsListSaving = true, error = null) }
            try {
                selected.forEach {
                    repository.addContact(it)
                }

                _state.update {
                    it.copy(
                        isContactsListSaving = false,
                        successMessage = context.getString(R.string.import_success_count, selected.size),
                        isMainPage = true,
                        isImportFromContacts = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isContactsListSaving = false,
                        error = e.message ?: context.getString(R.string.import_save_error)
                    )
                }
            }
        }
    }

    fun goBackToSettings() {
        _state.update {
            it.copy(
                isMainPage = true,
                isImportFromContacts = false,
                isContactsListSaving = false,
                isContactsListLoading = false,
                previewContacts = emptyList(),
                error = null
            )
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(error = null) }
    }
}