package com.example.netarchive.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.netarchive.ui.screens.add_contact_screen.AddContactScreen
import com.example.netarchive.ui.screens.contacts_list_screen.ContactListScreen
import com.example.netarchive.ui.screens.contact_view_screen.ContactViewScreen
import com.example.netarchive.ui.screens.add_note_screen.CreateNoteScreen
import com.example.netarchive.ui.screens.add_note_screen.CreateNoteViewModel
import com.example.netarchive.ui.screens.contacts_list_screen.ContactListScreen
@Serializable
object AddButt

@Serializable
object Contacts

@Serializable
data class ContactDetail(val contactId: Int)

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
data class CreateNoteRoute(val contactId: Int, val contactName: String)



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
            // Извлекаем параметры из маршрута
            val contactId = backStackEntry.arguments?.getInt("contactId") ?: 0
            val contactName = backStackEntry.arguments?.getString("contactName") ?: ""

            CreateNoteScreen(
                contactId = contactId,
                contactName = contactName,
                onBackClick = { navController.popBackStack() },
                onNoteCreated = {
                    navController.popBackStack()
                    navController.popBackStack()
                }
            )
        }
        composable<CreateConnection> {
            ContactListScreen(
                isSelectionMode = true,
                onContactClick = { contact ->
                    // Навигируем сразу с параметрами!
                    navController.navigate(
                        CreateNoteRoute(contactId = contact.id, contactName = contact.username)
                    )
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable<CreateRemind> {
            Text("CreateRemind", modifier = Modifier.padding(top=100.dp), fontSize = 40.sp)
        }

        composable<ContactDetail> { backStackEntry ->
            val contactId = backStackEntry.arguments?.getInt("contactId") ?: 0
            ContactViewScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onAddNoteClick = { id, name ->
                    // Сразу переходим на создание заметки с этим контактом!
                    navController.navigate(CreateNoteRoute(contactId = id, contactName = name))
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