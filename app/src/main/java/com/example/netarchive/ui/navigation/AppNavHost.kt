package com.example.netarchive.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Serializable
object Contacts

@Serializable
data class ContactDetail(val contactId: Int)

@Serializable
data class EditContact(val contactId: Int?)

@Composable
fun AppNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Contacts
    ){
        composable<Contacts> {
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