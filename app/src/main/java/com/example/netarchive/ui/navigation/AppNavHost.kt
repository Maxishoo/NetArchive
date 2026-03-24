package com.example.netarchive.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.netarchive.ui.screens.add_contact_screen.AddContactScreen
import com.example.netarchive.ui.screens.contacts_list_screen.ContactListScreen
import com.example.netarchive.ui.screens.contact_view_screen.ContactViewScreen
import com.example.netarchive.ui.screens.add_note_screen.CreateNoteScreen

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
    object CreateConnection


    @Serializable
    object CreateRemind

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
            Text("Profile", modifier = Modifier.padding(top = 100.dp), fontSize = 40.sp)
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
                onBackClick = { navController.popBackStack() },
                onNoteCreated = {
                    navController.popBackStack()
                    if (route.fromScreen == "select_contact") {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable<Routes.CreateConnection> {
            ContactListScreen(
                onContactClick = { contact ->
                    navController.navigate(
                        Routes.CreateNoteRoute(
                            contactId = contact.id,
                            contactName = contact.username,
                            contactAvatar = contact.avatar,
                            fromScreen = "select_contact"
                        )
                    )
                },
                isSelectionMode = true
            )
        }

        composable<Routes.CreateRemind> {
            Text("CreateRemind", modifier = Modifier.padding(top = 100.dp), fontSize = 40.sp)
        }

        composable<Routes.ContactDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.ContactDetail>()
            ContactViewScreen(
                initialTab = route.selectedTab,
                onBackClick = {
                    navController.popBackStack()
                },
                onAddNoteClick = { id, name, contactAvatar, noteId, noteText, noteDate, fromScreen, tab ->
                    navController.navigate(
                        Routes.CreateNoteRoute(
                            contactId = id,
                            contactName = name,
                            contactAvatar = contactAvatar,
                            noteId = noteId,
                            noteText = noteText,
                            noteDate = noteDate,
                            fromScreen = fromScreen,
                            returnTab = tab
                        )
                    )
                }
            )
        }
    }
}