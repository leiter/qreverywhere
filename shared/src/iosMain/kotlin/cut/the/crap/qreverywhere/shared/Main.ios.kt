package cut.the.crap.qreverywhere.shared

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import cut.the.crap.qreverywhere.shared.di.initKoinIos
import cut.the.crap.qreverywhere.shared.presentation.App
import cut.the.crap.qreverywhere.shared.presentation.theme.QrEveryWhereTheme
import cut.the.crap.qreverywhere.shared.presentation.viewmodel.MainViewModel
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
        val viewModel = remember { IosViewModelProvider().mainViewModel }

        // Use the shared theme from commonMain
        QrEveryWhereTheme {
            App(
                viewModel = viewModel,
                onShareText = { text ->
                    // iOS sharing handled via native code
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
    val mainViewModel: MainViewModel by inject()
}

/**
 * Share text using iOS share sheet (UIActivityViewController)
 */
private fun shareText(text: String) {
    val activityVC = UIActivityViewController(
        activityItems = listOf(text),
        applicationActivities = null
    )

    // Get the root view controller and present the share sheet
    val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
    rootViewController?.presentViewController(activityVC, animated = true, completion = null)
}

/**
 * Copy text to iOS clipboard
 */
private fun copyToClipboard(text: String) {
    platform.UIKit.UIPasteboard.generalPasteboard.string = text
}
