package cut.the.crap.qreverywhere.compose

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import org.koin.androidx.compose.koinViewModel
import org.jetbrains.compose.resources.stringResource as kmpStringResource
import qreverywhere.shared.generated.resources.Res
import qreverywhere.shared.generated.resources.*
import cut.the.crap.qreverywhere.shared.domain.usecase.UserPreferences
import cut.the.crap.qreverywhere.shared.presentation.App
import cut.the.crap.qreverywhere.shared.presentation.viewmodel.MainViewModel
import cut.the.crap.qreverywhere.compose.theme.QrEveryWhereTheme
import org.koin.compose.koinInject

/**
 * Main Activity using shared KMP App composable
 */
class ComposeMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QrEveryWhereTheme {
                val viewModel: MainViewModel = koinViewModel()
                val userPreferences: UserPreferences = koinInject()
                val context = LocalContext.current
                val copiedMessage = kmpStringResource(Res.string.feedback_copied)

                App(
                    viewModel = viewModel,
                    userPreferences = userPreferences,
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
}
