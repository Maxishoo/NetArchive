package com.example.netarchive.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        route = AddButt,
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

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (showAddMenu) {
            Column(
                modifier = Modifier.padding(bottom = 25.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                AddMenuItem.entries.forEachIndexed{index, item ->
                    AddButtonItem(
                        item.label
                    )
                    {
                        showAddMenu = false
                        navController.navigate(item.route)
                    }
                }
            }
        }

        NavigationBar {
            BottomNavItem.entries.forEachIndexed { index, item ->
                if (item.isPlusButton) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { showAddMenu = !showAddMenu },
                        icon = {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = item.label,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    )
                } else {
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = item.label,
                                modifier = Modifier.size(32.dp)
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

@Composable
fun AddButtonItem(
    label: String,
    onClick:()->Unit
){
    val colorScheme = MaterialTheme.colorScheme

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.primaryContainer.copy(alpha = 0.85f),
            contentColor = colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .widthIn(min = 180.dp)
            .height(48.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 1.dp
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 20.sp
            )
        )
    }
}