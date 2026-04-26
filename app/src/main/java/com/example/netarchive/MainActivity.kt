package com.example.netarchive

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.netarchive.data.repository.DatabaseInitializer
import com.example.netarchive.data.repository.ReminderRepository
import com.example.netarchive.ui.navigation.*
import com.example.netarchive.ui.theme.NetArchiveTheme
import com.example.netarchive.utils.NotificationHelper
import com.example.netarchive.utils.ReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import androidx.activity.result.contract.ActivityResultContracts


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var databaseInitializer: DatabaseInitializer

    @Inject
    lateinit var reminderRepository: ReminderRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        databaseInitializer.initializeIfNeeded()
        databaseInitializer.cleanupUnusedCategories()


        NotificationHelper.createNotificationChannel(this)

        requestNotificationPermission()

        rescheduleAllReminders()

        setContent {
            NetArchiveTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Main()
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {

        } else {

        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED -> {
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun rescheduleAllReminders() {
        lifecycleScope.launch {
            val todayMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val futureReminders = reminderRepository.getAllFutureReminders(todayMillis).first()
            futureReminders.forEach { reminder ->
                ReminderScheduler.scheduleReminder(
                    context = this@MainActivity,
                    reminderId = reminder.id,
                    title = "Напоминание",
                    text = reminder.text,
                    timestamp = reminder.date
                )
            }
        }
    }


}



@Composable
fun Main() {
    val navController = rememberNavController()
    var showAddMenu by remember { mutableStateOf(false) }
    var previousRoute by remember { mutableStateOf<String?>(null) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val selectedTab = when {
        currentDestination?.hierarchy?.any {
            it.route == Routes.Contacts::class.qualifiedName
        } == true -> BottomNavItem.Contacts

        currentDestination?.hierarchy?.any {
            it.route == Routes.Profile::class.qualifiedName || it.route == Routes.Settings::class.qualifiedName
        } == true -> BottomNavItem.Profile

        currentDestination?.hierarchy?.any { destination ->
            val route = destination.route ?: ""
            route.startsWith(Routes.CreateContact::class.qualifiedName ?: "") ||
                    route.startsWith(Routes.CreateConnection::class.qualifiedName ?: "") ||
                    route.startsWith(Routes.CreateNoteRoute::class.qualifiedName ?: "") ||
                    route.startsWith(Routes.CreateReminderRoute::class.qualifiedName ?: "")
        } == true -> {
            BottomNavItem.Add
        }

        else -> BottomNavItem.Contacts
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavBar(
                modifier = Modifier,
                navController = navController,
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    when (tab) {
                        BottomNavItem.Add -> {
                            showAddMenu = !showAddMenu
                        }
                        BottomNavItem.Contacts -> {
                            showAddMenu = false
                            navController.navigate(Routes.Contacts) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        BottomNavItem.Profile -> {
                            showAddMenu = false
                            navController.navigate(Routes.Profile) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box {
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                previousRoute = previousRoute,
                onRouteChange = { previousRoute = it }
            )

            AddMenuOverlay(
                visible = showAddMenu,
                onDismiss = { showAddMenu = false },
                onActionSelected = { action ->
                    showAddMenu = false
                    previousRoute = currentDestination?.route

                    when (action) {
                        AddMenuItem.CreateContact -> navController.navigate(Routes.CreateContact) {
                            launchSingleTop = true
                        }
                        AddMenuItem.CreateReminder -> {
                            navController.navigate(
                                Routes.CreateConnection(type = Routes.CreateConnection.EntryType.REMINDER)
                            ) {
                                launchSingleTop = true
                            }
                        }
                        AddMenuItem.CreateNote -> {
                            navController.navigate(
                                Routes.CreateConnection(type = Routes.CreateConnection.EntryType.NOTE)
                            ) {
                                launchSingleTop = true
                            }
                        }
                    }
                },
                modifier = Modifier
            )
        }
    }
}