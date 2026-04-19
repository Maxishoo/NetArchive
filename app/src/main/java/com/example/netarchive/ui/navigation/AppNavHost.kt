package com.example.netarchive.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.netarchive.ui.screens.add_contact_screen.AddContactScreen
import com.example.netarchive.ui.screens.contacts_list_screen.ContactListScreen
import com.example.netarchive.ui.screens.contact_view_screen.ContactViewScreen
import com.example.netarchive.ui.screens.add_note_screen.CreateNoteScreen
import com.example.netarchive.ui.screens.analytics_screen.AnalyticsScreen
import com.example.netarchive.ui.screens.add_reminder_screen.CreateReminderScreen
import com.example.netarchive.ui.screens.profile_screen.ProfileViewScreen
import com.example.netarchive.ui.screens.settings_screen.SettingsScreen

@Serializable
sealed class Routes{
    @Serializable
    object AddButt

    @Serializable
    object Contacts

    @Serializable
    data class ContactDetail(
        val contactId: Int,
        val selectedTab: Int = 0  // 0 = Информация, 1 = Заметки
    )

    @Serializable
    object Profile

    @Serializable
    object CreateContact


    @Serializable
    data class CreateConnection(
        val type: EntryType = EntryType.NOTE
    ) {
        enum class EntryType { NOTE, REMINDER }
    }


    @Serializable
    data class CreateReminderRoute(
        val contactId: Int,
        val contactName: String,
        val contactAvatar: String?,
        val reminderId: Int = 0,
        val reminderText: String = "",
        val reminderDate: Long = 0L,
        val fromScreen: String = "contact_view",
        val returnTab: Int = 0
    )

    @Serializable
    data class CreateNoteRoute(
        val contactId: Int,
        val contactName: String,
        val contactAvatar: String?,
        val noteId: Int = 0,
        val noteText: String = "",
        val noteDate: Long = 0L,
        val fromScreen: String = "contact_view",
        val returnTab: Int = 0
    )
    @Serializable
    object Analytics

    @Serializable
    object Settings
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier,
    previousRoute: String? = null,
    onRouteChange: (String?) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Contacts
    ) {
        composable<Routes.Contacts> {
            ContactListScreen(
                onContactClick = { contact ->
                    navController.navigate(Routes.ContactDetail(contact.id))
                }
            )
        }

        composable<Routes.Profile> {
            ProfileViewScreen(
                onSettingsClick = {
                    navController.navigate(Routes.Settings)
                }
            )
        }
        composable<Routes.Settings> {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable<Routes.CreateContact> {
            AddContactScreen(
                onContactCreated = {
                    navController.popBackStack()
                    previousRoute?.let {
                        if (!navController.popBackStack(it, false)) {
                            navController.navigate(Routes.Contacts) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    } ?: navController.navigate(Routes.Contacts)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable<Routes.CreateNoteRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.CreateNoteRoute>()
            CreateNoteScreen(
                contactId = route.contactId,
                contactName = route.contactName,
                contactAvatar = route.contactAvatar,
                noteId = route.noteId,
                noteText = route.noteText,
                noteDate = route.noteDate,
                onBackClick = {
                    navController.popBackStack()
                    navController.popBackStack()
                              },
                onNoteCreated = {
                    navController.popBackStack()
                    previousRoute?.let {
                        if (!navController.popBackStack(it, false)) {
                            navController.navigate(Routes.Contacts) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    } ?: navController.navigate(Routes.Contacts)
                }
            )
        }

        composable<Routes.CreateConnection> { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.CreateConnection>()
            ContactListScreen(
                onContactClick = { contact ->
                    when (route.type) {
                        Routes.CreateConnection.EntryType.NOTE -> {
                            navController.navigate(
                                Routes.CreateNoteRoute(
                                    contactId = contact.id,
                                    contactName = contact.username,
                                    contactAvatar = contact.avatar,
                                    fromScreen = "select_contact"
                                )
                            )
                        }
                        Routes.CreateConnection.EntryType.REMINDER -> {
                            navController.navigate(
                                Routes.CreateReminderRoute(
                                    contactId = contact.id,
                                    contactName = contact.username,
                                    contactAvatar = contact.avatar,
                                    fromScreen = "select_contact"
                                )
                            )
                        }
                    }
                },
                isSelectionMode = true
            )
        }

        composable<Routes.CreateReminderRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.CreateReminderRoute>()
            CreateReminderScreen(
                contactId = route.contactId,
                contactName = route.contactName,
                contactAvatar = route.contactAvatar,
                reminderId = route.reminderId,
                reminderText = route.reminderText,
                reminderDate = route.reminderDate,
                onBackClick = { navController.popBackStack() },
                onReminderCreated = {
                    navController.popBackStack()
                    previousRoute?.let {
                        if (!navController.popBackStack(it, false)) {
                            navController.navigate(Routes.Contacts) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    } ?: navController.navigate(Routes.Contacts)
                }
            )
        }

        composable<Routes.ContactDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.ContactDetail>()
            ContactViewScreen(
                initialTab = route.selectedTab,
                onBackClick = {
                    navController.popBackStack()
                },
                onAddNoteClick = { navigationData ->
                    navController.navigate(
                        Routes.CreateNoteRoute(
                            contactId = navigationData.contactId,
                            contactName = navigationData.contactName,
                            contactAvatar = navigationData.contactAvatar,
                            noteId = navigationData.noteId,
                            noteText = navigationData.noteText,
                            noteDate = navigationData.noteDate,
                            fromScreen = navigationData.source,
                            returnTab = navigationData.selectedTab
                        )
                    )
                }
            )
        }

        composable<Routes.Analytics> {
            AnalyticsScreen(
                onContactClick = { contactId ->
                    navController.navigate(Routes.ContactDetail(contactId))
                }
            )
        }
    }
}