package cut.the.crap.qreverywhere.compose

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cut.the.crap.qreverywhere.MainActivityViewModel
import org.koin.androidx.compose.koinViewModel
import cut.the.crap.qreverywhere.compose.navigation.ComposeScreen
import org.jetbrains.compose.resources.stringResource as kmpStringResource
import qreverywhere.shared.generated.resources.Res
import qreverywhere.shared.generated.resources.*
import cut.the.crap.qreverywhere.compose.screens.ComposeCreateEmailQrScreen
import cut.the.crap.qreverywhere.compose.screens.ComposeCreateQrScreen
import cut.the.crap.qreverywhere.compose.screens.ComposeCreateTextQrScreen
import cut.the.crap.qreverywhere.compose.screens.ComposeDetailViewScreen
import cut.the.crap.qreverywhere.compose.screens.ComposeFullscreenQrScreen
import cut.the.crap.qreverywhere.compose.screens.ComposeHistoryScreen
import cut.the.crap.qreverywhere.compose.screens.ComposeScanQrScreen
import cut.the.crap.qreverywhere.shared.presentation.App
import cut.the.crap.qreverywhere.shared.presentation.viewmodel.MainViewModel
import cut.the.crap.qreverywhere.compose.theme.QrEveryWhereTheme

/**
 * Main Activity for Compose UI migration
 *
 * TESTING: Currently configured to use the shared KMP App() composable
 * Toggle between TEST_SHARED_UI = true/false to test shared vs Android-specific UI
 */
class ComposeMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QrEveryWhereTheme {  //ComposeMainScreen()
                // TEST: Use shared KMP App composable
                val viewModel: MainViewModel = koinViewModel()
                val context = LocalContext.current
                val copiedMessage = kmpStringResource(Res.string.feedback_copied)

                App(
                    viewModel = viewModel,
                    onShareText = { text ->
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, null))
                    },
                    onCopyToClipboard = { text ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("QR Content", text)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
                    }
                )

                // OLD: Android-specific Compose screens
                // Uncomment below to use old Android screens
                // ComposeMainScreen()
            }
        }
    }
}

@Composable
fun ComposeMainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Get shared activity-scoped ViewModel instance
    val activityViewModel: MainActivityViewModel = koinViewModel()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                val bottomNavItems = listOf(
                    ComposeScreen.Scan,
                    ComposeScreen.Create,
                    ComposeScreen.History
                )

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(id = screen.icon),
                                contentDescription = stringResource(id = screen.title)
                            )
                        },
                        label = { Text(stringResource(id = screen.title)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ComposeScreen.Scan.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ComposeScreen.Scan.route) {
                ComposeScanQrScreen(navController, activityViewModel)
            }
            composable(ComposeScreen.Create.route) {
                ComposeCreateQrScreen(navController)
            }
            composable(ComposeScreen.History.route) {
                ComposeHistoryScreen(navController, activityViewModel)
            }
            composable(
                route = ComposeScreen.DetailView.route,
                arguments = listOf(navArgument("originFlag") { type = NavType.IntType })
            ) { backStackEntry ->
                val originFlag = backStackEntry.arguments?.getInt("originFlag") ?: 0
                ComposeDetailViewScreen(navController, originFlag, viewModel = activityViewModel)
            }
            composable(
                route = ComposeScreen.Fullscreen.route,
                arguments = listOf(navArgument("originFlag") { type = NavType.IntType })
            ) { backStackEntry ->
                val originFlag = backStackEntry.arguments?.getInt("originFlag") ?: 0
                ComposeFullscreenQrScreen(navController, originFlag, viewModel = activityViewModel)
            }
            composable(
                route = ComposeScreen.CreateText.route,
                arguments = listOf(navArgument("qrType") { type = NavType.StringType })
            ) { backStackEntry ->
                val qrType = backStackEntry.arguments?.getString("qrType") ?: "text"
                ComposeCreateTextQrScreen(navController, activityViewModel, qrType)
            }
            composable(ComposeScreen.CreateEmail.route) {
                ComposeCreateEmailQrScreen(navController, activityViewModel)
            }
        }
    }
}
