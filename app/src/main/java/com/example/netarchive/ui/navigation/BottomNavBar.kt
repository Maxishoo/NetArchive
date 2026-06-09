package com.example.netarchive.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.annotation.StringRes
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.netarchive.R
import com.example.netarchive.ui.theme.AppTheme
import com.example.netarchive.ui.theme.LightBlue


enum class BottomNavItem(
    val icon: ImageVector,
    val route: Any,
    @StringRes val labelRes: Int,
    val isPlusButton: Boolean = false
) {
    Contacts(
        icon = Icons.Outlined.People,
        route = Routes.Contacts,
        labelRes = R.string.nav_contacts
    ),

    Reminds(
        icon = Icons.Outlined.Notifications,
        route = Routes.RemindersList,
        labelRes = R.string.nav_reminders
    ),

    Add(
        icon = Icons.Outlined.AddCircleOutline,
        route = Routes.AddButt,
        labelRes = R.string.nav_add,
        isPlusButton = true
    ),

    Analytics(
        icon = Icons.Outlined.BarChart,
        route = Routes.Analytics,
        labelRes = R.string.nav_analytics
    ),

    Profile(
        icon = Icons.Outlined.Person,
        route = Routes.Profile,
        labelRes = R.string.nav_profile,
    ),
}

enum class AddMenuItem(
    @StringRes val labelRes: Int,
    val route: Any
) {
    CreateContact(
        labelRes = R.string.add_menu_contact,
        route = Routes.CreateContact
    ),
    CreateNote(
        labelRes = R.string.add_menu_note,
        route = Routes.CreateConnection(type = Routes.CreateConnection.EntryType.NOTE)
    ),
    CreateReminder(
        labelRes = R.string.add_menu_reminder,
        route = Routes.CreateConnection(type = Routes.CreateConnection.EntryType.REMINDER)
    )
}

@Composable
fun BottomNavBar(
    modifier: Modifier = Modifier,
    navController: NavController,
    selectedTab: BottomNavItem,
    onTabSelected: (BottomNavItem) -> Unit
) {
    val navIconColor = AppTheme.colors.navIcon
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = navIconColor,
        unselectedIconColor = navIconColor,
        selectedTextColor = navIconColor,
        unselectedTextColor = navIconColor,
        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
    )

    NavigationBar(
        modifier = modifier,
        windowInsets = NavigationBarDefaults.windowInsets,
        containerColor = AppTheme.bottomBarBackground(),
    ) {
        BottomNavItem.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                colors = itemColors,
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = stringResource(tab.labelRes),
                        modifier = Modifier.size(32.dp),
                        tint = navIconColor,
                    )
                },
            )
        }
    }
}

@Composable
fun AddMenuOverlay(
    modifier: Modifier = Modifier,
    visible: Boolean,
    onDismiss: () -> Unit,
    onActionSelected: (AddMenuItem) -> Unit,
) {
    if (!visible) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { onDismiss() },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = 125.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val colorScheme = MaterialTheme.colorScheme

            AddMenuItem.entries.forEach { action ->
                Button(
                    onClick = { onActionSelected(action) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightBlue.copy(alpha = 0.95f),
                        contentColor = colorScheme.onPrimaryContainer,
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
                        text = stringResource(action.labelRes),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 20.sp
                        )
                    )
                }
            }
        }
    }
}