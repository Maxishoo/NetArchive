package com.example.netarchive

import android.Manifest
import android.content.Intent
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.netarchive.data.local.preferences.ThemeRepository
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.netarchive.data.remote.vk.VkAuthLauncherHolder
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll


data class WidgetNavRequest(
    val target: String,
    val contactId: Int? = null,
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_NAV_TARGET = "widget_nav_target"
        const val EXTRA_CONTACT_ID = "widget_contact_id"
        const val NAV_CONTACT = "contact"
        const val NAV_CONTACTS = "contacts"
        const val NAV_REMINDERS = "reminders"
        const val NAV_ANALYTICS = "analytics"
        const val NAV_ADD_NOTE = "add_note"
    }

    private val widgetNavigation = mutableStateOf<WidgetNavRequest?>(null)

    @Inject
    lateinit var databaseInitializer: DatabaseInitializer

    @Inject
    lateinit var reminderRepository: ReminderRepository

    @Inject
    lateinit var themeRepository: ThemeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        VkAuthLauncherHolder.register(this)

        databaseInitializer.initializeIfNeeded()
        databaseInitializer.cleanupUnusedCategories()


        NotificationHelper.createNotificationChannel(this)

        requestNotificationPermission()

        rescheduleAllReminders()

        widgetNavigation.value = parseWidgetNav(intent)

        setContent {
            val isDarkTheme by themeRepository.isDarkTheme.collectAsState()
            NetArchiveTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Main(widgetNavigation = widgetNavigation)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        widgetNavigation.value = parseWidgetNav(intent)
    }

    private fun parseWidgetNav(intent: Intent?): WidgetNavRequest? {
        val target = intent?.getStringExtra(EXTRA_NAV_TARGET) ?: return null
        val contactId = intent.getIntExtra(EXTRA_CONTACT_ID, -1).takeIf { it > 0 }
        return WidgetNavRequest(target = target, contactId = contactId)
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
            val reminders = withContext(Dispatchers.IO) {
                val todayMillis = LocalDate.now()
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                reminderRepository
                    .getAllFutureReminders(todayMillis)
                    .first()
            }
            reminders.take(20).map { reminder ->
                async(Dispatchers.IO) {
                    ReminderScheduler.scheduleReminder(
                        context = this@MainActivity,
                        reminderId = reminder.id,
                        title = getString(R.string.reminder_title),
                        text = reminder.text,
                        timestamp = reminder.date
                    )
                }
            }.awaitAll()
        }
    }
}



@Composable
fun Main(widgetNavigation: MutableState<WidgetNavRequest?>) {
    val navController = rememberNavController()
    var showAddMenu by remember { mutableStateOf(false) }
    var previousRoute by remember { mutableStateOf<String?>(null) }

    val widgetNavRequest by widgetNavigation

    LaunchedEffect(widgetNavRequest) {
        val request = widgetNavRequest ?: return@LaunchedEffect
        when (request.target) {
            MainActivity.NAV_CONTACT -> {
                request.contactId?.let { contactId ->
                    navController.navigate(Routes.ContactDetail(contactId)) {
                        launchSingleTop = true
                    }
                }
            }
            MainActivity.NAV_REMINDERS -> {
                navController.navigate(Routes.RemindersList) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
            MainActivity.NAV_ANALYTICS -> {
                navController.navigate(Routes.Analytics) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
            MainActivity.NAV_CONTACTS -> {
                navController.navigate(Routes.Contacts) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
            MainActivity.NAV_ADD_NOTE -> {
                navController.navigate(
                    Routes.CreateConnection(type = Routes.CreateConnection.EntryType.NOTE)
                ) {
                    launchSingleTop = true
                }
            }
        }
        widgetNavigation.value = null
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val selectedTab = when {
        currentDestination?.hierarchy?.any {
            it.route == Routes.Contacts::class.qualifiedName
        } == true -> BottomNavItem.Contacts

        currentDestination?.hierarchy?.any {
            it.route == Routes.Analytics::class.qualifiedName
        } == true -> BottomNavItem.Analytics

        currentDestination?.hierarchy?.any {
            it.route == Routes.RemindersList::class.qualifiedName
        } == true -> BottomNavItem.Reminds

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

                        BottomNavItem.Analytics -> {
                            showAddMenu = false
                            navController.navigate(Routes.Analytics) {
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
                        BottomNavItem.Reminds -> {
                            showAddMenu = false
                            navController.navigate(Routes.RemindersList) {
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