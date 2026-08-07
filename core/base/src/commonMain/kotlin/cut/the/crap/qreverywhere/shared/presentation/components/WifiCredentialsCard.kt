package cut.the.crap.qreverywhere.shared.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cut.the.crap.qreverywhere.core.base.generated.resources.Res
import cut.the.crap.qreverywhere.core.base.generated.resources.wifi_credentials_close
import cut.the.crap.qreverywhere.core.base.generated.resources.wifi_credentials_hide
import cut.the.crap.qreverywhere.core.base.generated.resources.wifi_credentials_password
import cut.the.crap.qreverywhere.core.base.generated.resources.wifi_credentials_show
import cut.the.crap.qreverywhere.core.base.generated.resources.wifi_credentials_ssid
import cut.the.crap.qreverywhere.core.base.generated.resources.wifi_credentials_title
import cut.the.crap.qreverywhere.shared.domain.model.WifiCredentials
import org.jetbrains.compose.resources.stringResource

/**
 * Informational, reveal-only display of parsed WiFi credentials from a scanned/created
 * WIFI: QR code. There is no auto-connect logic in V1 - this purely shows the SSID and
 * a maskable password so the user can verify or manually enter them.
 */
@Composable
fun WifiCredentialsCard(
    credentials: WifiCredentials,
    onDismiss: () -> Unit
) {
    var passwordRevealed by remember { mutableStateOf(false) }

    val titleText = stringResource(Res.string.wifi_credentials_title)
    val ssidLabel = stringResource(Res.string.wifi_credentials_ssid)
    val passwordLabel = stringResource(Res.string.wifi_credentials_password)
    val showText = stringResource(Res.string.wifi_credentials_show)
    val hideText = stringResource(Res.string.wifi_credentials_hide)
    val closeText = stringResource(Res.string.wifi_credentials_close)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = titleText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = ssidLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = credentials.ssid,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = passwordLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (passwordRevealed) credentials.password else credentials.getMaskedPassword(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    TextButton(onClick = { passwordRevealed = !passwordRevealed }) {
                        Text(if (passwordRevealed) hideText else showText)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(closeText)
            }
        }
    )
}
