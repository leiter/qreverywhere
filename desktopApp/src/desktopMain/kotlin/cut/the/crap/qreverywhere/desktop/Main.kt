package cut.the.crap.qreverywhere.desktop

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cut.the.crap.qreverywhere.shared.di.commonModule
import cut.the.crap.qreverywhere.shared.di.platformModule
import cut.the.crap.qreverywhere.shared.domain.usecase.UserPreferences
import cut.the.crap.qreverywhere.shared.presentation.App
import cut.the.crap.qreverywhere.shared.presentation.viewmodel.MainViewModel
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

fun main() = application {
    // Initialize Napier logging
    Napier.base(DebugAntilog())

    // Initialize Koin
    startKoin {
        modules(commonModule, platformModule())
    }

    // Get ViewModel and UserPreferences from Koin
    val viewModel: MainViewModel = getKoin().get()
    val userPreferences: UserPreferences = getKoin().get()

    Window(
        onCloseRequest = ::exitApplication,
        title = "QR Everywhere"
    ) {
        MaterialTheme {
            App(
                viewModel = viewModel,
                userPreferences = userPreferences,
                onShareText = { text ->
                    // On desktop, share is implemented as copy to clipboard
                    copyToClipboard(text)
                },
                onCopyToClipboard = { text ->
                    copyToClipboard(text)
                }
            )
        }
    }
}

private fun copyToClipboard(text: String) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val selection = StringSelection(text)
    clipboard.setContents(selection, selection)
}
