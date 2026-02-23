package com.example.netarchive.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

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
            Text("Contacts")
//            ContactsScreen(
//                onContactClick = { id: Int ->
//                    navController.navigate(ContactDetail(id))
//                }
//            )
        }

        composable<ContactDetail> { backStackEntry ->
            val contactId = backStackEntry.arguments?.getInt("contactId")
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