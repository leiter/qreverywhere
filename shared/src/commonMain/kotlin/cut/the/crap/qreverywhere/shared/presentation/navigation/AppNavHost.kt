package cut.the.crap.qreverywhere.shared.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cut.the.crap.qreverywhere.feature.create.CreateAppStoreLinkScreen
import cut.the.crap.qreverywhere.feature.create.CreateCalendarScreen
import cut.the.crap.qreverywhere.feature.create.CreateCryptoScreen
import cut.the.crap.qreverywhere.feature.create.CreateEmailScreen
import cut.the.crap.qreverywhere.feature.create.CreateLocationScreen
import cut.the.crap.qreverywhere.feature.create.CreateMeCardScreen
import cut.the.crap.qreverywhere.feature.create.CreatePaymentScreen
import cut.the.crap.qreverywhere.feature.create.CreateScreen
import cut.the.crap.qreverywhere.feature.create.CreateTextScreen
import cut.the.crap.qreverywhere.feature.create.CreateVcardScreen
import cut.the.crap.qreverywhere.feature.create.CreateViewModel
import cut.the.crap.qreverywhere.feature.create.CreateWiFiScreen
import cut.the.crap.qreverywhere.feature.detail.DetailScreen
import cut.the.crap.qreverywhere.feature.detail.DetailViewModel
import cut.the.crap.qreverywhere.feature.detail.FullscreenScreen
import cut.the.crap.qreverywhere.feature.history.HistoryScreen
import cut.the.crap.qreverywhere.feature.history.HistoryViewModel
import cut.the.crap.qreverywhere.feature.scan.ScanScreen
import cut.the.crap.qreverywhere.feature.settings.SettingsScreen
import cut.the.crap.qreverywhere.shared.domain.model.AcquireType
import cut.the.crap.qreverywhere.shared.domain.usecase.ThemePreference
import cut.the.crap.qreverywhere.shared.domain.usecase.UserPreferences

@Composable
fun AppNavHost(
    navController: NavHostController,
    historyViewModel: HistoryViewModel,
    createViewModel: CreateViewModel,
    detailViewModel: DetailViewModel,
    userPreferences: UserPreferences,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Scan.route,
    onShareText: (String) -> Unit = {},
    onCopyToClipboard: (String) -> Unit = {},
    onThemeChanged: (ThemePreference) -> Unit = {}
) {
    val navigateToHistoryAfterCreate: () -> Unit = {
        navController.navigate(Screen.History.route) {
            popUpTo(Screen.Create.route) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = historyViewModel,
                onQrItemClick = { qrItem ->
                    detailViewModel.setDetailViewItem(qrItem)
                    navController.navigate(Screen.Detail.createRoute(qrItem.id))
                }
            )
        }

        composable(Screen.Detail.route) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")?.toIntOrNull()

            DetailScreen(
                viewModel = detailViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onShare = {
                    detailViewModel.detailViewItem.value?.let { item ->
                        onShareText(item.textContent)
                    }
                },
                onCopyToClipboard = {
                    detailViewModel.detailViewItem.value?.let { item ->
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

        composable(Screen.Fullscreen.route) {
            FullscreenScreen(
                viewModel = detailViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Scan.route) {
            ScanScreen(
                onQrCodeScanned = { scannedText ->
                    detailViewModel.saveScannedQrItem(
                        textContent = scannedText,
                        acquireType = AcquireType.SCANNED
                    ) { /* result handled internally */ }

                    navController.navigate(Screen.History.route) {
                        popUpTo(Screen.Scan.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Create.route) {
            CreateScreen(
                onTextQrClick = { navController.navigate(Screen.CreateText.createRoute("text")) },
                onUrlQrClick = { navController.navigate(Screen.CreateText.createRoute("url")) },
                onPhoneQrClick = { navController.navigate(Screen.CreateText.createRoute("phone")) },
                onSmsQrClick = { navController.navigate(Screen.CreateText.createRoute("sms")) },
                onEmailQrClick = { navController.navigate(Screen.CreateEmail.route) },
                onContactQrClick = { navController.navigate(Screen.CreateVcard.route) },
                onWiFiQrClick = { navController.navigate(Screen.CreateWiFi.route) },
                onCalendarQrClick = { navController.navigate(Screen.CreateCalendar.route) },
                onLocationQrClick = { navController.navigate(Screen.CreateLocation.route) },
                onMeCardQrClick = { navController.navigate(Screen.CreateMeCard.route) },
                onAppStoreQrClick = { navController.navigate(Screen.CreateAppStoreLink.route) },
                onPaymentQrClick = { navController.navigate(Screen.CreatePayment.route) },
                onCryptoQrClick = { navController.navigate(Screen.CreateCrypto.route) }
            )
        }

        composable(Screen.CreateText.route) { backStackEntry ->
            val qrType = backStackEntry.arguments?.getString("qrType") ?: "text"
            CreateTextScreen(
                qrType = qrType,
                viewModel = createViewModel,
                onQrCreated = navigateToHistoryAfterCreate
            )
        }

        composable(Screen.CreateEmail.route) {
            CreateEmailScreen(
                viewModel = createViewModel,
                onQrCreated = navigateToHistoryAfterCreate
            )
        }

        composable(Screen.CreateVcard.route) {
            CreateVcardScreen(
                viewModel = createViewModel,
                onQrCreated = navigateToHistoryAfterCreate
            )
        }

        composable(Screen.CreateWiFi.route) {
            CreateWiFiScreen(
                viewModel = createViewModel,
                onQrCreated = navigateToHistoryAfterCreate
            )
        }

        composable(Screen.CreateCalendar.route) {
            CreateCalendarScreen(
                viewModel = createViewModel,
                onQrCreated = navigateToHistoryAfterCreate
            )
        }

        composable(Screen.CreateLocation.route) {
            CreateLocationScreen(
                viewModel = createViewModel,
                onQrCreated = navigateToHistoryAfterCreate
            )
        }

        composable(Screen.CreateMeCard.route) {
            CreateMeCardScreen(
                viewModel = createViewModel,
                onQrCreated = navigateToHistoryAfterCreate
            )
        }

        composable(Screen.CreateAppStoreLink.route) {
            CreateAppStoreLinkScreen(
                viewModel = createViewModel,
                onQrCreated = navigateToHistoryAfterCreate
            )
        }

        composable(Screen.CreatePayment.route) {
            CreatePaymentScreen(
                viewModel = createViewModel,
                onQrCreated = navigateToHistoryAfterCreate
            )
        }

        composable(Screen.CreateCrypto.route) {
            CreateCryptoScreen(
                viewModel = createViewModel,
                onQrCreated = navigateToHistoryAfterCreate
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                userPreferences = userPreferences,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onThemeChanged = onThemeChanged
            )
        }
    }
}
