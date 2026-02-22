package cut.the.crap.qreverywhere.compose

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import org.jetbrains.compose.resources.stringResource as kmpStringResource
import qreverywhere.shared.generated.resources.Res
import qreverywhere.shared.generated.resources.*
import cut.the.crap.qreverywhere.shared.domain.usecase.UserPreferences
import cut.the.crap.qreverywhere.feature.create.CreateViewModel
import cut.the.crap.qreverywhere.feature.detail.DetailViewModel
import cut.the.crap.qreverywhere.feature.history.HistoryViewModel
import cut.the.crap.qreverywhere.shared.presentation.App
import cut.the.crap.qreverywhere.compose.theme.QrEveryWhereTheme
import cut.the.crap.qreverywhere.widget.QrWidgetProvider
import org.koin.compose.koinInject

/**
 * Main Activity using shared KMP App composable
 */
class ComposeMainActivity : ComponentActivity() {

    // Initial route based on intent (shortcuts, widgets)
    private val initialRoute = mutableStateOf<String?>(null)
    private val initialDetailId = mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle initial intent (shortcuts, widgets)
        handleIntent(intent)

        setContent {
            QrEveryWhereTheme {
                val historyViewModel: HistoryViewModel = koinInject()
                val createViewModel: CreateViewModel = koinInject()
                val detailViewModel: DetailViewModel = koinInject()
                val userPreferences: UserPreferences = koinInject()
                val context = LocalContext.current
                val copiedMessage = kmpStringResource(Res.string.feedback_copied)

                App(
                    historyViewModel = historyViewModel,
                    createViewModel = createViewModel,
                    detailViewModel = detailViewModel,
                    userPreferences = userPreferences,
                    initialRoute = initialRoute.value,
                    initialDetailId = initialDetailId.value,
                    onShareText = { text ->
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, null))
                    },
                    onCopyToClipboard = { text ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("QR Content", text)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    /**
     * Handle intents from shortcuts, widgets, and deep links
     */
    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            QrWidgetProvider.ACTION_OPEN_SCAN -> {
                initialRoute.value = "scan"
            }
            QrWidgetProvider.ACTION_OPEN_CREATE -> {
                initialRoute.value = "create"
            }
            ACTION_OPEN_HISTORY -> {
                initialRoute.value = "history"
            }
            QrWidgetProvider.ACTION_OPEN_DETAIL -> {
                val qrId = intent.getIntExtra(QrWidgetProvider.EXTRA_QR_ID, -1)
                if (qrId != -1) {
                    initialRoute.value = "detail"
                    initialDetailId.value = qrId
                }
            }
        }
    }

    companion object {
        const val ACTION_OPEN_HISTORY = "cut.the.crap.qreverywhere.OPEN_HISTORY"
    }
}
