package com.example.netarchive.ui.navigation

import com.example.netarchive.domain.model.Note

/**
 * Данные для навигации к экрану создания/редактирования заметки
 */
data class NoteNavigationData(
    val contactId: Int,
    val contactName: String,
    val contactAvatar: String?,
    val noteId: Int = 0,
    val noteText: String = "",
    val noteDate: Long = 0L,
    val source: String = "",
    val selectedTab: Int = 0
) {
    companion object {
        fun forNewNote(
            contactId: Int,
            contactName: String,
            contactAvatar: String?,
            source: String = "",
            selectedTab: Int = 0
        ): NoteNavigationData {
            return NoteNavigationData(
                contactId = contactId,
                contactName = contactName,
                contactAvatar = contactAvatar,
                noteId = 0,
                noteText = "",
                noteDate = 0L,
                source = source,
                selectedTab = selectedTab
            )
        }

        fun forEditNote(
            contactId: Int,
            contactName: String,
            contactAvatar: String?,
            note: Note,
            source: String = "",
            selectedTab: Int = 0
        ): NoteNavigationData {
            return NoteNavigationData(
                contactId = contactId,
                contactName = contactName,
                contactAvatar = contactAvatar,
                noteId = note.id,
                noteText = note.text,
                noteDate = note.date,
                source = source,
                selectedTab = selectedTab
            )
        }
    }
}