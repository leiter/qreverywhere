package cut.the.crap.qreverywhere.shared.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import qreverywhere.shared.generated.resources.Res
import qreverywhere.shared.generated.resources.create_subtitle
import qreverywhere.shared.generated.resources.title_text_qr
import qreverywhere.shared.generated.resources.desc_text_qr
import qreverywhere.shared.generated.resources.title_url_qr
import qreverywhere.shared.generated.resources.desc_url_qr
import qreverywhere.shared.generated.resources.title_phone_qr
import qreverywhere.shared.generated.resources.desc_phone_qr
import qreverywhere.shared.generated.resources.title_email_qr
import qreverywhere.shared.generated.resources.desc_email_qr
import qreverywhere.shared.generated.resources.title_contact_qr
import qreverywhere.shared.generated.resources.desc_contact_qr
import qreverywhere.shared.generated.resources.title_wifi_qr
import qreverywhere.shared.generated.resources.desc_wifi_qr

/**
 * Create Screen for Compose Multiplatform
 * Main screen for choosing QR code creation type
 *
 * Design adapted from Android ComposeCreateQrScreen with card-based selection
 */
@Composable
fun CreateScreen(
    onTextQrClick: () -> Unit = {},
    onUrlQrClick: () -> Unit = {},
    onPhoneQrClick: () -> Unit = {},
    onSmsQrClick: () -> Unit = {},
    onEmailQrClick: () -> Unit = {},
    onContactQrClick: () -> Unit = {},
    onWiFiQrClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.create_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Plain Text QR Code
        QrTypeCard(
            title = stringResource(Res.string.title_text_qr),
            description = stringResource(Res.string.desc_text_qr),
            onClick = onTextQrClick
        )

        // Web URL QR Code
        QrTypeCard(
            title = stringResource(Res.string.title_url_qr),
            description = stringResource(Res.string.desc_url_qr),
            onClick = onUrlQrClick
        )

        // Phone QR Code
        QrTypeCard(
            title = stringResource(Res.string.title_phone_qr),
            description = stringResource(Res.string.desc_phone_qr),
            onClick = onPhoneQrClick
        )

        // SMS QR Code - temporarily hidden
        // QrTypeCard(
        //     title = stringResource(Res.string.title_sms_qr),
        //     description = stringResource(Res.string.desc_sms_qr),
        //     onClick = onSmsQrClick
        // )

        // Email QR Code
        QrTypeCard(
            title = stringResource(Res.string.title_email_qr),
            description = stringResource(Res.string.desc_email_qr),
            onClick = onEmailQrClick
        )

        // Contact (vCard) QR Code
        QrTypeCard(
            title = stringResource(Res.string.title_contact_qr),
            description = stringResource(Res.string.desc_contact_qr),
            onClick = onContactQrClick
        )

        // WiFi QR Code
        QrTypeCard(
            title = stringResource(Res.string.title_wifi_qr),
            description = stringResource(Res.string.desc_wifi_qr),
            onClick = onWiFiQrClick
        )
    }
}

@Composable
private fun QrTypeCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
