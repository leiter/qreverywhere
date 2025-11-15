package cut.the.crap.qreverywhere.shared.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cut.the.crap.qreverywhere.shared.presentation.screens.CreateEmailScreen
import cut.the.crap.qreverywhere.shared.presentation.screens.CreateScreen
import cut.the.crap.qreverywhere.shared.presentation.screens.CreateTextScreen
import cut.the.crap.qreverywhere.shared.presentation.screens.DetailScreen
import cut.the.crap.qreverywhere.shared.presentation.screens.FullscreenScreen
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
    startDestination: String = Screen.Scan.route,
    onShareText: (String) -> Unit = {},
    onCopyToClipboard: (String) -> Unit = {}
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
                    viewModel.detailViewItem.value?.let { item ->
                        onShareText(item.textContent)
                    }
                },
                onCopyToClipboard = {
                    viewModel.detailViewItem.value?.let { item ->
                        onCopyToClipboard(item.textContent)
                    }
                },
                onFullscreenClick = {
                    itemId?.let {
                        navController.navigate(Screen.Fullscreen.createRoute(it))
                    }
                }
            )
        }

        composable(Screen.Fullscreen.route) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")?.toIntOrNull()

            FullscreenScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
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
                onTextQrClick = {
                    navController.navigate(Screen.CreateText.createRoute("text"))
                },
                onUrlQrClick = {
                    navController.navigate(Screen.CreateText.createRoute("url"))
                },
                onPhoneQrClick = {
                    navController.navigate(Screen.CreateText.createRoute("phone"))
                },
                onSmsQrClick = {
                    navController.navigate(Screen.CreateText.createRoute("sms"))
                },
                onEmailQrClick = {
                    navController.navigate(Screen.CreateEmail.route)
                }
            )
        }

        composable(Screen.CreateText.route) { backStackEntry ->
            val qrType = backStackEntry.arguments?.getString("qrType") ?: "text"
            CreateTextScreen(
                qrType = qrType,
                viewModel = viewModel,
                onQrCreated = {
                    // Navigate to detail view after creating QR code
                    viewModel.detailViewItem.value?.let { item ->
                        navController.navigate(Screen.Detail.createRoute(item.id))
                    }
                }
            )
        }

        composable(Screen.CreateEmail.route) {
            CreateEmailScreen(
                viewModel = viewModel,
                onQrCreated = {
                    viewModel.detailViewItem.value?.let { item ->
                        navController.navigate(Screen.Detail.createRoute(item.id))
                    }
                }
            )
        }
    }
}
