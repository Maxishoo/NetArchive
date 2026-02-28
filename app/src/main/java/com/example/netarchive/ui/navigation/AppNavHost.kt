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
import com.example.netarchive.ui.screens.add_contact_screen.AddContactScreen
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
            ContactListScreen()
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
        composable<CreateConnection> {
            Text("CreateConnection", modifier = Modifier.padding(top=100.dp), fontSize = 40.sp)
        }
        composable<CreateRemind> {
            Text("CreateRemind", modifier = Modifier.padding(top=100.dp), fontSize = 40.sp)
        }

        composable<ContactDetail> { backStackEntry ->
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