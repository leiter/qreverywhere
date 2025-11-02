package cut.the.crap.qreverywhere.shared.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cut.the.crap.qreverywhere.shared.presentation.navigation.AppNavHost
import cut.the.crap.qreverywhere.shared.presentation.navigation.Screen
import cut.the.crap.qreverywhere.shared.presentation.viewmodel.MainViewModel

/**
 * Main App Composable for Compose Multiplatform
 * This is the root composable that will be used by iOS, Android, and Desktop
 *
 * Handles:
 * - Bottom Navigation Bar (Material 3)
 * - Top App Bar
 * - Navigation between screens
 * - iOS safe areas (handled automatically by Scaffold)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    viewModel: MainViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {},

//                    {
//                    Text(
//                        text = when (currentDestination?.route) {
//                            Screen.History.route -> Strings.titleHistory
//                            Screen.Scan.route -> Strings.titleScan
//                            Screen.Create.route -> Strings.titleCreate
//                            Screen.CreateText.route -> Strings.titleCreateText
//                            Screen.CreateEmail.route -> Strings.titleCreateEmail
//                            else -> Strings.appName
//                        }
//                    )
//                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val bottomNavItems = listOf(
                    BottomNavItem(
                        route = Screen.Scan.route,
                        icon = Icons.Default.Search,
                        label = Strings.navScan
                    ),
                    BottomNavItem(
                        route = Screen.Create.route,
                        icon = Icons.Default.Add,
                        label = Strings.navCreate
                    ),
                    BottomNavItem(
                        route = Screen.History.route,
                        icon = Icons.Default.Home,
                        label = Strings.navHistory
                    )
                )

                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                // Pop up to the start destination to avoid building up a large stack
                                popUpTo(Screen.Scan.route) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = {
                            Text(item.label)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // Navigation host with proper padding for safe areas
        AppNavHost(
            navController = navController,
            viewModel = viewModel,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // This handles iOS safe areas automatically!
        )
    }
}

/**
 * Data class for bottom navigation items
 */
private data class BottomNavItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)
