package com.example.netarchive.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import com.example.netarchive.R
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

sealed class BottomNavItem(
    val icon: Int,
    val route: Any,
    val label: String,
    val isPlusButton: Boolean = false
) {
    object Contacts : BottomNavItem(
        icon = R.drawable.contacts,
        route = com.example.netarchive.ui.navigation.Contacts,
        label = "Контакты"
    )

    object Add : BottomNavItem(
        icon = R.drawable.add_circle,
        route = com.example.netarchive.ui.navigation.AddButt,
        label = "Добавить",
        isPlusButton = true
    )

    object Profile : BottomNavItem(
        icon = R.drawable.account_circle,
        route = com.example.netarchive.ui.navigation.Profile,
        label = "Профиль",
    )

    companion object {
        val entries = listOf(Contacts, Add, Profile)
    }
}

sealed class AddMenuItem(
    val label: String,
    val route: Any
) {
    object CreateContact : AddMenuItem(
        label = "Контакт",
        route = com.example.netarchive.ui.navigation.CreateContact
    )

    object CreateConnection : AddMenuItem(
        label = "Связь",
        route = com.example.netarchive.ui.navigation.CreateConnection
    )

    object CreateRemind : AddMenuItem(
        label = "Напоминание",
        route = com.example.netarchive.ui.navigation.CreateRemind
    )

    companion object {
        val entries = listOf(CreateContact, CreateConnection, CreateRemind)
    }
}

@Composable
fun BottomNavBar(
    navController: NavHostController
) {
    var showAddMenu by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Column {
        if (showAddMenu) {
            Column(modifier = Modifier.padding(bottom = 100.dp)) {
                Button(onClick = { navController.navigate(CreateContact) }) {
                    Text("Контакт")
                }
                Button(onClick = { navController.navigate(CreateConnection) }) {
                    Text("Связь")
                }
                Button(onClick = { navController.navigate(CreateRemind) }) {
                    Text("Напоминание")
                }
            }
        }

        NavigationBar {
            BottomNavItem.entries.forEachIndexed { index, item ->
                if (item.isPlusButton) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { showAddMenu = true },
                        icon = {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = item.label
                            )
                        }
                    )
                } else {
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = item.label
                            )
                        },
                        selected = selectedTab == index,
                        onClick = {
                            if (selectedTab != index) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                                selectedTab = index
                            }
                        }
                    )
                }
            }
        }
    }
}