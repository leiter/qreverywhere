package cut.the.crap.qreverywhere.shared.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cut.the.crap.qreverywhere.shared.presentation.screens.CreateScreen
import cut.the.crap.qreverywhere.shared.presentation.screens.DetailScreen
import cut.the.crap.qreverywhere.shared.presentation.screens.HistoryScreen
import cut.the.crap.qreverywhere.shared.presentation.screens.ScanScreen
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
    modifier: Modifier = Modifier,
    startDestination: String = Screen.History.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
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

            DetailScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onShare = {
                    Logger.w("DetailScreen") { "Share functionality requires platform-specific implementation" }
                },
                onCopyToClipboard = {
                    Logger.w("DetailScreen") { "Clipboard functionality requires platform-specific implementation" }
                }
            )
        }

        composable(Screen.Scan.route) {
            ScanScreen(
                onQrCodeScanned = { scannedText ->
                    viewModel.saveQrItemFromText(
                        textContent = scannedText,
                        acquireType = cut.the.crap.qreverywhere.shared.domain.model.AcquireType.SCANNED
                    )
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Create.route) {
            CreateScreen(
                viewModel = viewModel,
                onQrCreated = {
                    // Navigate to detail view after creating QR code
                    viewModel.detailViewItem.value?.let { item ->
                        navController.navigate(Screen.Detail.createRoute(item.id))
                    }
                }
            )
        }

        composable(Screen.CreateText.route) {
            CreateScreen(
                viewModel = viewModel,
                onQrCreated = {
                    viewModel.detailViewItem.value?.let { item ->
                        navController.navigate(Screen.Detail.createRoute(item.id))
                    }
                }
            )
        }

        composable(Screen.CreateEmail.route) {
            // TODO: Implement specialized email QR screen
            CreateScreen(viewModel = viewModel)
        }
    }
}
