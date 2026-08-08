package cut.the.crap.qreverywhere.shared

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import cut.the.crap.qreverywhere.feature.create.CreateViewModel
import cut.the.crap.qreverywhere.feature.detail.DetailViewModel
import cut.the.crap.qreverywhere.feature.history.HistoryViewModel
import cut.the.crap.qreverywhere.shared.di.initKoinIos
import cut.the.crap.qreverywhere.shared.domain.repository.QrRepository
import cut.the.crap.qreverywhere.shared.domain.usecase.QrCodeGenerator
import cut.the.crap.qreverywhere.shared.domain.usecase.ThemePreference
import cut.the.crap.qreverywhere.shared.domain.usecase.UserPreferences
import cut.the.crap.qreverywhere.shared.presentation.App
import cut.the.crap.qreverywhere.shared.presentation.theme.QrEveryWhereTheme
import cut.the.crap.qreverywhere.shared.screenshot.ScreenshotMode
import cut.the.crap.qreverywhere.shared.screenshot.ScreenshotSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

/**
 * iOS Main entry point for Compose Multiplatform
 * This function creates the main UIViewController that hosts the Compose UI
 */
fun MainViewController(): UIViewController {
    // Initialize Koin for iOS
    initKoinIos()

    val screenshotRoute = configureScreenshotMode()

    return ComposeUIViewController {
        val provider = remember { IosViewModelProvider() }

        // Track theme preference state to trigger recomposition when changed
        var themePreference by remember {
            mutableStateOf(provider.userPreferences.getThemePreference())
        }

        // Determine if dark theme based on preference
        val isSystemDark = isSystemInDarkTheme()
        val isDarkTheme = when (themePreference) {
            ThemePreference.LIGHT -> false
            ThemePreference.DARK -> true
            ThemePreference.SYSTEM -> isSystemDark
        }

        // Use the shared theme from commonMain with dynamic dark theme
        QrEveryWhereTheme(darkTheme = isDarkTheme) {
            App(
                historyViewModel = provider.historyViewModel,
                createViewModel = provider.createViewModel,
                detailViewModel = provider.detailViewModel,
                userPreferences = provider.userPreferences,
                initialRoute = screenshotRoute,
                onShareText = { text ->
                    shareText(text)
                },
                onCopyToClipboard = { text ->
                    copyToClipboard(text)
                },
                onThemeChanged = { newTheme ->
                    themePreference = newTheme
                }
            )
        }
    }
}

/**
 * Turns screenshot launch arguments into a start destination.
 *
 * Arguments prefixed with `-` land in the NSUserDefaults argument domain, so
 * `simctl launch <app> -screenshotMode YES -screenshotScreen wifi` is readable
 * here with no URL scheme and no taps. Returns null for a normal launch.
 *
 * Seeding is fire-and-forget: the history is a Room Flow, so the list redraws
 * itself once the demo rows land, and the capture script waits out the delay.
 */
private fun configureScreenshotMode(): String? {
    val defaults = NSUserDefaults.standardUserDefaults
    if (!defaults.boolForKey("screenshotMode")) return null

    ScreenshotMode.enabled = true

    val screen = defaults.stringForKey("screenshotScreen") ?: "scan"
    val seedTarget = ScreenshotSeedTarget()

    CoroutineScope(Dispatchers.Main).launch {
        val newest = ScreenshotSeeder(seedTarget.repository, seedTarget.generator).seed()
        // The detail screen reads the selected item from the view model rather
        // than from the route, so hand it over explicitly.
        newest?.let { seedTarget.detailViewModel.setDetailViewItem(it) }
    }

    return when (screen) {
        "create" -> "create"
        "history" -> "history"
        "wifi" -> "create/wifi"
        "detail" -> "detail/1"
        else -> "scan"
    }
}

/**
 * Koin lookups needed for seeding, kept out of the composable.
 */
private class ScreenshotSeedTarget : KoinComponent {
    val repository: QrRepository by inject()
    val generator: QrCodeGenerator by inject()
    val detailViewModel: DetailViewModel by inject()
}

/**
 * Helper class to get ViewModels from Koin
 */
private class IosViewModelProvider : KoinComponent {
    val historyViewModel: HistoryViewModel by inject()
    val createViewModel: CreateViewModel by inject()
    val detailViewModel: DetailViewModel by inject()
    val userPreferences: UserPreferences by inject()
}

/**
 * Share text using iOS share sheet (UIActivityViewController)
 */
private fun shareText(text: String) {
    val activityVC = UIActivityViewController(
        activityItems = listOf(text),
        applicationActivities = null
    )

    val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
    rootViewController?.presentViewController(activityVC, animated = true, completion = null)
}

/**
 * Copy text to iOS clipboard
 */
private fun copyToClipboard(text: String) {
    platform.UIKit.UIPasteboard.generalPasteboard.string = text
}

// MARK: - Widget Data Sync

/**
 * Data class representing QR code data for widget sync.
 * This matches the structure expected by WidgetDataStore in Swift.
 */
data class WidgetQrData(
    val id: Int,
    val text: String,
    val imageData: ByteArray?,
    val type: String?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as WidgetQrData
        if (id != other.id) return false
        if (text != other.text) return false
        if (imageData != null) {
            if (other.imageData == null) return false
            if (!imageData.contentEquals(other.imageData)) return false
        } else if (other.imageData != null) return false
        if (type != other.type) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + text.hashCode()
        result = 31 * result + (imageData?.contentHashCode() ?: 0)
        result = 31 * result + (type?.hashCode() ?: 0)
        return result
    }
}

/**
 * Callback for widget updates.
 * Set this from Swift to receive QR data updates for the widget.
 *
 * Usage in Swift:
 * ```swift
 * Main_iosKt.onWidgetUpdateCallback = { qrData in
 *     if let data = qrData {
 *         WidgetDataStore.shared.saveLatestQrCode(
 *             id: Int(data.id),
 *             text: data.text,
 *             imageData: data.imageData?.toData(),
 *             type: data.type
 *         )
 *     }
 * }
 * ```
 */
var onWidgetUpdateCallback: ((WidgetQrData?) -> Unit)? = null

/**
 * Call this function to notify the widget about QR code updates.
 * This should be called whenever a QR code is created, scanned, or selected.
 *
 * @param qrData The QR code data to sync to the widget, or null to clear
 */
fun notifyWidgetUpdate(qrData: WidgetQrData?) {
    onWidgetUpdateCallback?.invoke(qrData)
}

/**
 * Convenience function to notify widget update with individual parameters.
 *
 * @param id The QR code database ID
 * @param text The QR code content text
 * @param imageData PNG data of the generated QR code image (as ByteArray)
 * @param type The QR code type (URL, WiFi, vCard, etc.)
 */
fun notifyWidgetUpdate(id: Int, text: String, imageData: ByteArray?, type: String?) {
    notifyWidgetUpdate(WidgetQrData(id, text, imageData, type))
}

// MARK: - Deep Link Handling

/**
 * Callback for deep link navigation.
 * Set this from Swift to handle deep link navigation requests.
 */
var onDeepLinkCallback: ((String, Int?) -> Unit)? = null

/**
 * Call this function to handle deep link navigation from Swift.
 *
 * @param action The action to perform ("detail" or "create")
 * @param id Optional QR code ID for detail action
 */
fun handleDeepLink(action: String, id: Int?) {
    onDeepLinkCallback?.invoke(action, id)
}
