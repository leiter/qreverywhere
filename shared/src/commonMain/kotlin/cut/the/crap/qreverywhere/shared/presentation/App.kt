package cut.the.crap.qreverywhere.shared.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cut.the.crap.qreverywhere.shared.presentation.navigation.AppNavHost
import cut.the.crap.qreverywhere.shared.presentation.navigation.Screen
import cut.the.crap.qreverywhere.shared.presentation.viewmodel.MainViewModel
import org.jetbrains.compose.resources.stringResource
import qreverywhere.shared.generated.resources.Res
import qreverywhere.shared.generated.resources.*

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
    viewModel: MainViewModel,
    onShareText: (String) -> Unit = {},
    onCopyToClipboard: (String) -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route ?: ""

    // Determine if we're on a child screen that needs a back button
    val isChildScreen = currentRoute.startsWith("create/text/") || currentRoute == Screen.CreateEmail.route

    // Get the title based on current route
    val topBarTitle: @Composable () -> String = {
        when {
            currentRoute == Screen.History.route -> stringResource(Res.string.title_history)
            currentRoute == Screen.CreateEmail.route -> stringResource(Res.string.title_email_qr)
            currentRoute.startsWith("create/text/") -> {
                val qrType = navBackStackEntry?.arguments?.getString("qrType") ?: "text"
                when (qrType) {
                    "text" -> stringResource(Res.string.title_text_qr)
                    "url" -> stringResource(Res.string.title_url_qr)
                    "phone" -> stringResource(Res.string.title_phone_qr)
                    "sms" -> stringResource(Res.string.title_sms_qr)
                    else -> stringResource(Res.string.title_text_qr)
                }
            }
            else -> ""
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val title = topBarTitle()
                    if (title.isNotEmpty()) {
                        Text(text = title)
                    }
                },
                navigationIcon = {
                    if (isChildScreen) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.cd_back)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                // Scan tab
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == Screen.Scan.route } == true,
                    onClick = {
                        navController.navigate(Screen.Scan.route) {
                            popUpTo(Screen.Scan.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(Res.string.nav_scan)
                        )
                    },
                    label = { Text(stringResource(Res.string.nav_scan)) }
                )

                // Create tab
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == Screen.Create.route } == true,
                    onClick = {
                        navController.navigate(Screen.Create.route) {
                            popUpTo(Screen.Scan.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(Res.string.nav_create)
                        )
                    },
                    label = { Text(stringResource(Res.string.nav_create)) }
                )

                // History tab
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == Screen.History.route } == true,
                    onClick = {
                        navController.navigate(Screen.History.route) {
                            popUpTo(Screen.Scan.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = stringResource(Res.string.nav_history)
                        )
                    },
                    label = { Text(stringResource(Res.string.nav_history)) }
                )
            }
        }
    ) { innerPadding ->
        // Navigation host with proper padding for safe areas
        AppNavHost(
            navController = navController,
            viewModel = viewModel,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), // This handles iOS safe areas automatically!
            onShareText = onShareText,
            onCopyToClipboard = onCopyToClipboard
        )
    }
}
