package com.example.netarchive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.netarchive.data.repository.DatabaseInitializer
import com.example.netarchive.ui.navigation.AddMenuItem
import com.example.netarchive.ui.navigation.AddMenuOverlay
import com.example.netarchive.ui.navigation.AppNavHost
import com.example.netarchive.ui.navigation.BottomNavBar
import com.example.netarchive.ui.navigation.BottomNavItem
import com.example.netarchive.ui.navigation.Routes
import com.example.netarchive.ui.theme.NetArchiveTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var databaseInitializer: DatabaseInitializer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        databaseInitializer.initializeIfNeeded()

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
}

@Composable
fun Main(){
    val navController = rememberNavController()
    var showAddMenu by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val selectedTab = when {
        currentDestination?.hierarchy?.any {
            it.route == Routes.Contacts::class.qualifiedName
        } == true -> BottomNavItem.Contacts

        currentDestination?.hierarchy?.any {
            it.route == Routes.Profile::class.qualifiedName
        } == true -> BottomNavItem.Profile

        currentDestination?.hierarchy?.any {
            it.route == Routes.CreateContact::class.qualifiedName || it.route == Routes.CreateRemind::class.qualifiedName || it.route == Routes.CreateConnection::class.qualifiedName
        } == true -> BottomNavItem.Add

        else -> BottomNavItem.Contacts
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(), bottomBar = {
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
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }

                        BottomNavItem.Profile -> {
                            showAddMenu = false
                            navController.navigate(Routes.Profile) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                })
        }) { innerPadding ->
            Box {
                AppNavHost(
                    navController = navController, modifier = Modifier.padding(innerPadding)
                )
                AddMenuOverlay(
                    visible = showAddMenu,
                    onDismiss = { showAddMenu = false },
                    onActionSelected = { action ->
                        showAddMenu = false
                        when (action) {
                            AddMenuItem.CreateContact -> navController.navigate(Routes.CreateContact)
                            AddMenuItem.CreateRemind -> navController.navigate(Routes.CreateRemind)
                            AddMenuItem.CreateConnection -> navController.navigate(Routes.CreateConnection)
                        }
                    },
                    modifier = Modifier
                )
            }
    }
}