package cut.the.crap.qreverywhere.shared.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cut.the.crap.qreverywhere.shared.presentation.screens.HistoryScreen
import cut.the.crap.qreverywhere.shared.presentation.viewmodel.MainViewModel
import cut.the.crap.qreverywhere.shared.utils.Logger

/**
 * Shared Navigation Host for Compose Multiplatform
 *
 * Note: Platform-specific screens (Scanner, Share, etc.) are not yet implemented.
 * These require expect/actual declarations for platform-specific APIs.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: MainViewModel,
    startDestination: String = Screen.History.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = viewModel,
                onQrItemClick = { qrItem ->
                    viewModel.setDetailViewItem(qrItem)
                    navController.navigate(Screen.Detail.createRoute(qrItem.id))
                }
            )
        }

        composable(Screen.Detail.route) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")?.toIntOrNull()

            // TODO: Implement DetailScreen
            // This requires platform-specific image rendering and sharing capabilities
            Logger.w("NotImplemented") { "DetailScreen not yet implemented for KMP" }

            // Placeholder screen
            HistoryScreen(viewModel = viewModel)
        }

        composable(Screen.Scan.route) {
            // TODO: Implement ScanScreen
            // This requires platform-specific camera APIs (expect/actual)
            Logger.w("NotImplemented") { "ScanScreen not yet implemented for KMP - needs platform-specific camera API" }

            // Placeholder screen
            HistoryScreen(viewModel = viewModel)
        }

        composable(Screen.Create.route) {
            // TODO: Implement CreateScreen
            Logger.w("NotImplemented") { "CreateScreen not yet implemented for KMP" }

            // Placeholder screen
            HistoryScreen(viewModel = viewModel)
        }

        composable(Screen.CreateText.route) {
            // TODO: Implement CreateTextScreen
            Logger.w("NotImplemented") { "CreateTextScreen not yet implemented for KMP" }

            // Placeholder screen
            HistoryScreen(viewModel = viewModel)
        }

        composable(Screen.CreateEmail.route) {
            // TODO: Implement CreateEmailScreen
            Logger.w("NotImplemented") { "CreateEmailScreen not yet implemented for KMP" }

            // Placeholder screen
            HistoryScreen(viewModel = viewModel)
        }
    }
}
