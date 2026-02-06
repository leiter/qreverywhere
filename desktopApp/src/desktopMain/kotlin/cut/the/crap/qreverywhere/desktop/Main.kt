package cut.the.crap.qreverywhere.desktop

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import cut.the.crap.qreverywhere.shared.domain.repository.QrRepository
import cut.the.crap.qreverywhere.shared.di.commonModule
import cut.the.crap.qreverywhere.shared.di.platformModule
import cut.the.crap.qreverywhere.shared.domain.usecase.UserPreferences
import cut.the.crap.qreverywhere.shared.presentation.App
import cut.the.crap.qreverywhere.shared.presentation.viewmodel.MainViewModel
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

fun main() = application {
    // Initialize Napier logging
    Napier.base(DebugAntilog())

    // Initialize Koin
    startKoin {
        modules(commonModule, platformModule())
    }

    // Get dependencies from Koin
    val viewModel: MainViewModel = getKoin().get()
    val userPreferences: UserPreferences = getKoin().get()
    val repository: QrRepository = getKoin().get()

    // Window visibility state
    var isWindowVisible by remember { mutableStateOf(true) }

    // Initial route for deep linking from tray
    var initialRoute by remember { mutableStateOf<String?>(null) }
    var initialDetailId by remember { mutableStateOf<Int?>(null) }

    // System tray integration (only on supported platforms)
    if (SystemTray.isSupported()) {
        QrSystemTray(
            repository = repository,
            isWindowVisible = isWindowVisible,
            onShowWindow = { isWindowVisible = true },
            onHideWindow = { isWindowVisible = false },
            onCreateFromClipboard = {
                // Get text from clipboard and navigate to create screen
                val clipboardText = getClipboardText()
                if (clipboardText != null) {
                    // Set the clipboard text in ViewModel and navigate to create
                    viewModel.setClipboardContent(clipboardText)
                }
                initialRoute = "create"
                isWindowVisible = true
            },
            onOpenQrCode = { id ->
                initialRoute = "detail"
                initialDetailId = id
            }
        )
    }

    // Main window
    Window(
        onCloseRequest = {
            if (SystemTray.isSupported()) {
                // Minimize to tray instead of exiting
                isWindowVisible = false
            } else {
                exitApplication()
            }
        },
        visible = isWindowVisible,
        title = "QR Everywhere",
        state = WindowState()
    ) {
        MaterialTheme {
            App(
                viewModel = viewModel,
                userPreferences = userPreferences,
                initialRoute = initialRoute,
                initialDetailId = initialDetailId,
                onShareText = { text ->
                    // On desktop, share is implemented as copy to clipboard
                    copyToClipboard(text)
                },
                onCopyToClipboard = { text ->
                    copyToClipboard(text)
                }
            )
        }

        // Clear initial route after first navigation
        if (initialRoute != null) {
            initialRoute = null
            initialDetailId = null
        }
    }
}

private fun copyToClipboard(text: String) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val selection = StringSelection(text)
    clipboard.setContents(selection, selection)
}

private fun getClipboardText(): String? {
    return try {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            clipboard.getData(DataFlavor.stringFlavor) as? String
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}
