package com.example.netarchive.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.netarchive.R

enum class BottomNavItem(
    val icon: ImageVector,
    val route: Any,
    val labelResId: Int,
    val isPlusButton: Boolean = false
) {
    Contacts(
        icon = Icons.Outlined.People,
        route = Routes.Contacts,
        labelResId = R.string.contacts_title
    ),
    Reminds(
        icon = Icons.Outlined.Notifications,
        route = Routes.RemindersList,
        labelResId = R.string.nav_reminders
    ),
    Add(
        icon = Icons.Outlined.AddCircleOutline,
        route = Routes.AddButt,
        labelResId = R.string.nav_add,
        isPlusButton = true
    ),
    Analytics(
        icon = Icons.Outlined.BarChart,
        route = Routes.Analytics,
        labelResId = R.string.nav_analytics
    ),
    Profile(
        icon = Icons.Outlined.Person,
        route = Routes.Profile,
        labelResId = R.string.nav_profile,
    ),
}

enum class AddMenuItem(
    val labelResId: Int,
    val route: Any
) {
    CreateContact(
        labelResId = R.string.contacts_title,
        route = Routes.CreateContact
    ),
    CreateNote(
        labelResId = R.string.add_item_note,
        route = Routes.CreateConnection(type = Routes.CreateConnection.EntryType.NOTE)
    ),
    CreateReminder(
        labelResId = R.string.add_item_reminder,
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
    NavigationBar(
        modifier = modifier,
        windowInsets = NavigationBarDefaults.windowInsets,
        containerColor = colorResource(id = R.color.top_bar_background).copy(alpha = 0.95f),
    ) {
        BottomNavItem.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = stringResource(tab.labelResId),
                        modifier = Modifier.size(dimensionResource(id = R.dimen.nav_bar_icon_size))
                    )
                }
                // ✅ Убран параметр `label` — теперь только иконки, без текста
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
                .padding(
                    bottom = dimensionResource(id = R.dimen.add_menu_bottom_padding),
                    start = dimensionResource(id = R.dimen.padding_medium),
                    end = dimensionResource(id = R.dimen.padding_medium)
                )
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.add_menu_spacing))
        ) {
            val colorScheme = MaterialTheme.colorScheme

            AddMenuItem.entries.forEach { action ->
                Button(
                    onClick = { onActionSelected(action) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.add_menu_button_bg).copy(alpha = 0.95f),
                        contentColor = colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_pill)),
                    modifier = Modifier
                        .widthIn(min = dimensionResource(id = R.dimen.add_menu_button_min_width))
                        .height(dimensionResource(id = R.dimen.button_height)),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = dimensionResource(id = R.dimen.button_elevation)
                    )
                ) {
                    Text(
                        text = stringResource(action.labelResId),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = dimensionResource(id = R.dimen.add_menu_button_font_size).value.sp
                        )
                    )
                }
            }
        }
    }
}