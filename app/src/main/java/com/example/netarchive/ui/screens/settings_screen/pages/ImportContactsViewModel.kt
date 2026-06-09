package com.example.netarchive.ui.screens.settings_screen.pages

import android.content.Context
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netarchive.R
import com.example.netarchive.data.remote.vk.VkAuthHelper
import com.example.netarchive.data.remote.vk.VkFriend
import com.example.netarchive.data.remote.vk.VkFriendsRepository
import com.example.netarchive.data.remote.vk.VkSetupInfo
import com.example.netarchive.data.repository.ContactRepository
import com.example.netarchive.domain.model.Contact
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAuthenticationResult
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
    val isImportFromVk: Boolean = false,
    val isContactsListLoading: Boolean = false,
    val isContactsListSaving: Boolean = false,
    val previewContacts: List<ContactPreviewItem> = emptyList(),
    val successMessage: String? = null,
    val error: String? = null,
) {
    val isSelectionPage: Boolean = isImportFromContacts || isImportFromVk
}

@HiltViewModel
class ImportContactsViewModel @Inject constructor(
    private val repository: ContactRepository,
    private val vkFriendsRepository: VkFriendsRepository,
    private val vkAuthHelper: VkAuthHelper,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(ImportContactsState())
    val state: StateFlow<ImportContactsState> = _state.asStateFlow()

    fun startImport() {
        _state.update { it.copy(isMainPage = false, isImportFromContacts = true, isImportFromVk = false) }
        loadDeviceContacts()
    }

    fun isVkAppIdConfigured(): Boolean = vkAuthHelper.isAppIdConfigured()

    fun vkCertificateFingerprint(): String? = VkSetupInfo.certificateFingerprintForVk(context)

    fun showVkAppIdError() {
        _state.update { it.copy(error = context.getString(R.string.vk_app_id_not_configured)) }
    }

    fun onVkAuthResult(result: VKAuthenticationResult) {
        when (result) {
            is VKAuthenticationResult.Success -> startVkImport()
            is VKAuthenticationResult.Failed -> {
                _state.update { it.copy(error = vkAuthHelper.mapAuthError(result)) }
            }
        }
    }

    fun startVkImport() {
        _state.update {
            it.copy(
                isMainPage = false,
                isImportFromContacts = false,
                isImportFromVk = true,
                previewContacts = emptyList(),
                error = null,
            )
        }
        loadVkFriends()
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
                val name = cursor.getString(1)?.takeIf { it.isNotBlank() }
                    ?: context.getString(R.string.import_no_name)
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

    private fun normalizeVkProfileUrl(url: String?): String? =
        url?.trim()?.lowercase()?.removePrefix("https://")?.removePrefix("http://")
            ?.removePrefix("www.")?.removeSuffix("/")

    private fun vkFriendToContact(friend: VkFriend): Contact =
        Contact(
            id = 0,
            username = friend.displayName,
            phone = friend.mobilePhone?.takeIf { it.isNotBlank() },
            telegram = friend.profileUrl,
            avatar = friend.photoUrl,
        )

    private fun loadDeviceContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isContactsListLoading = true, error = null) }
            try {
                val contacts = fetchContacts()
                val existingPhones = repository.getContactsPhones()

                val phonesInDb = existingPhones
                    .mapNotNull { normalizePhone(it).takeIf { phone -> phone.isNotBlank() } }
                    .toSet()

                val previewItems = contacts.map { (deviceId, contact) ->
                    val normPhone = normalizePhone(contact.phone)
                    val isDuplicate = normPhone.isNotBlank() && normPhone in phonesInDb

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

    private fun loadVkFriends() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isContactsListLoading = true, error = null) }
            try {
                if (!VK.isLoggedIn()) {
                    _state.update {
                        it.copy(
                            isContactsListLoading = false,
                            isMainPage = true,
                            isImportFromVk = false,
                            error = context.getString(R.string.vk_auth_required),
                        )
                    }
                    return@launch
                }

                val friends = vkFriendsRepository.fetchFriends()
                val existingVkProfiles = repository.getContactsVkProfileUrls()
                    .mapNotNull { normalizeVkProfileUrl(it) }
                    .toSet()
                val existingPhones = repository.getContactsPhones()
                    .mapNotNull { normalizePhone(it).takeIf { phone -> phone.isNotBlank() } }
                    .toSet()

                val previewItems = friends.map { friend ->
                    val contact = vkFriendToContact(friend)
                    val profileKey = normalizeVkProfileUrl(friend.profileUrl)
                    val normPhone = normalizePhone(contact.phone)
                    val isDuplicate = profileKey in existingVkProfiles ||
                        (normPhone.isNotBlank() && normPhone in existingPhones)

                    ContactPreviewItem(
                        deviceContactId = -friend.id,
                        contact = contact,
                        isSelected = !isDuplicate,
                        isDuplicate = isDuplicate,
                    )
                }

                _state.update {
                    it.copy(
                        isContactsListLoading = false,
                        previewContacts = previewItems.sortedBy { item -> item.contact.username.lowercase() },
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isContactsListLoading = false,
                        error = e.message ?: context.getString(R.string.vk_friends_load_error),
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
            _state.update { it.copy(isContactsListLoading = true) }

            _state.update { currentState ->
                currentState.copy(
                    previewContacts = currentState.previewContacts.map { item ->
                        if (!item.isDuplicate) item.copy(isSelected = selected) else item
                    }
                )
            }

            _state.update { it.copy(isContactsListLoading = false) }
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
                selected.forEach { repository.addContact(it) }

                _state.update {
                    it.copy(
                        isContactsListSaving = false,
                        successMessage = context.getString(R.string.import_success_count, selected.size),
                        isMainPage = true,
                        isImportFromContacts = false,
                        isImportFromVk = false,
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
                isImportFromVk = false,
                isContactsListSaving = false,
                isContactsListLoading = false,
                previewContacts = emptyList(),
                error = null,
            )
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(error = null, successMessage = null) }
    }
}
