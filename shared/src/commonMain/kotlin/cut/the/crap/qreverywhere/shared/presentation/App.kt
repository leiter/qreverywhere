package cut.the.crap.qreverywhere.shared.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cut.the.crap.qreverywhere.feature.create.CreateViewModel
import cut.the.crap.qreverywhere.feature.detail.DetailViewModel
import cut.the.crap.qreverywhere.feature.history.HistoryViewModel
import cut.the.crap.qreverywhere.shared.domain.usecase.ThemePreference
import cut.the.crap.qreverywhere.shared.domain.usecase.UserPreferences
import cut.the.crap.qreverywhere.shared.presentation.navigation.AppNavHost
import cut.the.crap.qreverywhere.shared.presentation.navigation.Screen
import cut.the.crap.qreverywhere.shared.utils.DeviceOrientation
import cut.the.crap.qreverywhere.shared.utils.getDeviceOrientation
import cut.the.crap.qreverywhere.shared.utils.toReadableString
import org.jetbrains.compose.resources.stringResource
import qreverywhere.shared.generated.resources.Res
import qreverywhere.shared.generated.resources.*

/**
 * Main App Composable for Compose Multiplatform
 * This is the root composable that will be used by iOS, Android, and Desktop
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    historyViewModel: HistoryViewModel,
    createViewModel: CreateViewModel,
    detailViewModel: DetailViewModel,
    userPreferences: UserPreferences,
    initialRoute: String? = null,
    initialDetailId: Int? = null,
    onShareText: (String) -> Unit = {},
    onCopyToClipboard: (String) -> Unit = {},
    onThemeChanged: (ThemePreference) -> Unit = {}
) {
    val navController = rememberNavController()

    // Handle initial navigation from shortcuts, widgets, or deep links
    LaunchedEffect(initialRoute, initialDetailId) {
        when (initialRoute) {
            "scan" -> {
                navController.navigate(Screen.Scan.route) {
                    popUpTo(Screen.Scan.route) { inclusive = true }
                }
            }
            "create" -> {
                navController.navigate(Screen.Create.route) {
                    popUpTo(Screen.Scan.route)
                }
            }
            "history" -> {
                navController.navigate(Screen.History.route) {
                    popUpTo(Screen.Scan.route)
                }
            }
            "detail" -> {
                initialDetailId?.let { id ->
                    navController.navigate(Screen.Detail.createRoute(id)) {
                        popUpTo(Screen.Scan.route)
                    }
                }
            }
        }
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route ?: ""

    // Get device orientation
    val orientation = getDeviceOrientation()
    val isLandscape = orientation == DeviceOrientation.LANDSCAPE
    val isScanScreen = currentRoute == Screen.Scan.route

    // Hide top bar when in landscape mode on scan screen for full camera preview
    val shouldHideTopBar = isLandscape && isScanScreen

    // Get detail item for fullscreen title
    val detailItem by detailViewModel.detailViewItem.collectAsState()

    // Determine if we're on a child screen that needs a back button
    val isChildScreen = currentRoute.startsWith("create/text/") ||
        currentRoute == Screen.CreateEmail.route ||
        currentRoute == Screen.CreateVcard.route ||
        currentRoute == Screen.CreateWiFi.route ||
        currentRoute == Screen.CreateCalendar.route ||
        currentRoute == Screen.CreateLocation.route ||
        currentRoute == Screen.CreateMeCard.route ||
        currentRoute == Screen.CreateAppStoreLink.route ||
        currentRoute == Screen.CreatePayment.route ||
        currentRoute == Screen.CreateCrypto.route ||
        currentRoute.startsWith("detail/") ||
        currentRoute.startsWith("fullscreen/") ||
        currentRoute == Screen.Settings.route

    // Get the title based on current route
    val topBarTitle: @Composable () -> String = {
        when {
            currentRoute == Screen.Scan.route -> stringResource(Res.string.title_scan)
            currentRoute == Screen.History.route -> stringResource(Res.string.title_history)
            currentRoute == Screen.Create.route -> stringResource(Res.string.title_create)
            currentRoute.startsWith("detail/") -> stringResource(Res.string.title_detail)
            currentRoute.startsWith("fullscreen/") -> detailItem?.let { "${it.acquireType.name} • ${it.timestamp.toReadableString()}" } ?: stringResource(Res.string.title_detail)
            currentRoute == Screen.CreateEmail.route -> stringResource(Res.string.title_email_qr)
            currentRoute == Screen.CreateVcard.route -> stringResource(Res.string.title_contact_qr)
            currentRoute == Screen.CreateWiFi.route -> stringResource(Res.string.title_wifi_qr)
            currentRoute == Screen.CreateCalendar.route -> stringResource(Res.string.title_calendar_qr)
            currentRoute == Screen.CreateLocation.route -> stringResource(Res.string.title_location_qr)
            currentRoute == Screen.CreateMeCard.route -> stringResource(Res.string.title_mecard_qr)
            currentRoute == Screen.CreateAppStoreLink.route -> stringResource(Res.string.title_appstore_qr)
            currentRoute == Screen.CreatePayment.route -> stringResource(Res.string.title_payment_qr)
            currentRoute == Screen.CreateCrypto.route -> stringResource(Res.string.title_crypto_qr)
            currentRoute == Screen.Settings.route -> stringResource(Res.string.settings_title)
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
            // Only show top bar if not in landscape scan mode
            if (!shouldHideTopBar) {
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
                    actions = {
                        // Show settings icon only on main screens (not child screens)
                        if (!isChildScreen) {
                            IconButton(
                                onClick = {
                                    navController.navigate(Screen.Settings.route)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = stringResource(Res.string.cd_settings)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
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
        val layoutDirection = LocalLayoutDirection.current
        val adjustedPadding = if (shouldHideTopBar) {
            PaddingValues(
                top = 0.dp,
                bottom = innerPadding.calculateBottomPadding(),
                start = innerPadding.calculateLeftPadding(layoutDirection),
                end = innerPadding.calculateRightPadding(layoutDirection)
            )
        } else {
            innerPadding
        }

        AppNavHost(
            navController = navController,
            historyViewModel = historyViewModel,
            createViewModel = createViewModel,
            detailViewModel = detailViewModel,
            userPreferences = userPreferences,
            modifier = Modifier
                .fillMaxSize()
                .padding(adjustedPadding),
            onShareText = onShareText,
            onCopyToClipboard = onCopyToClipboard,
            onThemeChanged = onThemeChanged
        )
    }
}
