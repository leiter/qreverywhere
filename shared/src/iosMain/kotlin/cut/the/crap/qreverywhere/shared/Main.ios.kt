package cut.the.crap.qreverywhere.shared

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import cut.the.crap.qreverywhere.feature.create.CreateViewModel
import cut.the.crap.qreverywhere.feature.detail.DetailViewModel
import cut.the.crap.qreverywhere.feature.history.HistoryViewModel
import cut.the.crap.qreverywhere.shared.di.initKoinIos
import cut.the.crap.qreverywhere.shared.domain.usecase.UserPreferences
import cut.the.crap.qreverywhere.shared.presentation.App
import cut.the.crap.qreverywhere.shared.presentation.theme.QrEveryWhereTheme
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
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

    return ComposeUIViewController {
        val provider = remember { IosViewModelProvider() }

        // Use the shared theme from commonMain
        QrEveryWhereTheme {
            App(
                historyViewModel = provider.historyViewModel,
                createViewModel = provider.createViewModel,
                detailViewModel = provider.detailViewModel,
                userPreferences = provider.userPreferences,
                onShareText = { text ->
                    shareText(text)
                },
                onCopyToClipboard = { text ->
                    copyToClipboard(text)
                }
            )
        }
    }
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
