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
object AddButt

@Serializable
object Contacts

@Serializable
data class ContactDetail(
    val contactId: Int,
    val selectedTab: Int = 0  // 0 = Информация, 1 = Заметки
)

@Serializable
data class EditContact(val contactId: Int?)

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



@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Contacts
    ){
        composable<Contacts> {
            ContactListScreen(
                onContactClick = { contact ->
                    navController.navigate(ContactDetail(contact.id))
                }
            )
        }
        composable<Profile> {
            Text("Profile", modifier = Modifier.padding(top=100.dp), fontSize = 40.sp)
        }
        composable<CreateContact> {
            AddContactScreen(
                onContactCreated = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable<CreateNoteRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<CreateNoteRoute>()

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

        composable<CreateConnection> {
            ContactListScreen(
                onContactClick = { contact ->
                    navController.navigate(
                        CreateNoteRoute(
                            contactId = contact.id,
                            contactName = contact.username,
                            contactAvatar = contact.avatar,
                            fromScreen = "select_contact")
                    )
                }
            )
        }

        composable<CreateRemind> {
            Text("CreateRemind", modifier = Modifier.padding(top=100.dp), fontSize = 40.sp)
        }

        composable<ContactDetail> { backStackEntry ->
            val contactId = backStackEntry.arguments?.getInt("contactId") ?: 0
            val selectedTab = backStackEntry.arguments?.getInt("selectedTab") ?: 0
            ContactViewScreen(
                initialTab = selectedTab,
                onBackClick = {
                    navController.popBackStack()
                },
                onAddNoteClick = { id, name, contactAvatar, noteId, noteText, noteDate,fromScreen,tab ->
                    navController.navigate(
                        CreateNoteRoute(
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
//            val contactId = backStackEntry.arguments?.getInt("contactId")
//            ContactDetailScreen(
//                contactId = contactId,
//                onBackClick = {
//                    navController.popBackStack()
//                },
//                onEditClick = { id: Int ->
//                    navController.navigate(EditContact(id))
//                }
//            )
        }

    }
}