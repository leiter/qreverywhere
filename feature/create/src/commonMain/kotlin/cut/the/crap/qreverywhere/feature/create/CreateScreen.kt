package cut.the.crap.qreverywhere.feature.create

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
import qreverywhere.shared.generated.resources.*

@Composable
fun CreateScreen(
    onTextQrClick: () -> Unit = {},
    onUrlQrClick: () -> Unit = {},
    onPhoneQrClick: () -> Unit = {},
    onSmsQrClick: () -> Unit = {},
    onEmailQrClick: () -> Unit = {},
    onContactQrClick: () -> Unit = {},
    onWiFiQrClick: () -> Unit = {},
    onCalendarQrClick: () -> Unit = {},
    onLocationQrClick: () -> Unit = {},
    onMeCardQrClick: () -> Unit = {},
    onAppStoreQrClick: () -> Unit = {},
    onPaymentQrClick: () -> Unit = {},
    onCryptoQrClick: () -> Unit = {}
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

        QrTypeCard(
            title = stringResource(Res.string.title_text_qr),
            description = stringResource(Res.string.desc_text_qr),
            onClick = onTextQrClick
        )

        QrTypeCard(
            title = stringResource(Res.string.title_url_qr),
            description = stringResource(Res.string.desc_url_qr),
            onClick = onUrlQrClick
        )

        QrTypeCard(
            title = stringResource(Res.string.title_phone_qr),
            description = stringResource(Res.string.desc_phone_qr),
            onClick = onPhoneQrClick
        )

        QrTypeCard(
            title = stringResource(Res.string.title_email_qr),
            description = stringResource(Res.string.desc_email_qr),
            onClick = onEmailQrClick
        )

        QrTypeCard(
            title = stringResource(Res.string.title_contact_qr),
            description = stringResource(Res.string.desc_contact_qr),
            onClick = onContactQrClick
        )

        QrTypeCard(
            title = stringResource(Res.string.title_wifi_qr),
            description = stringResource(Res.string.desc_wifi_qr),
            onClick = onWiFiQrClick
        )

        QrTypeCard(
            title = stringResource(Res.string.title_calendar_qr),
            description = stringResource(Res.string.desc_calendar_qr),
            onClick = onCalendarQrClick
        )

        QrTypeCard(
            title = stringResource(Res.string.title_location_qr),
            description = stringResource(Res.string.desc_location_qr),
            onClick = onLocationQrClick
        )

        QrTypeCard(
            title = stringResource(Res.string.title_mecard_qr),
            description = stringResource(Res.string.desc_mecard_qr),
            onClick = onMeCardQrClick
        )

        QrTypeCard(
            title = stringResource(Res.string.title_appstore_qr),
            description = stringResource(Res.string.desc_appstore_qr),
            onClick = onAppStoreQrClick
        )

        QrTypeCard(
            title = stringResource(Res.string.title_payment_qr),
            description = stringResource(Res.string.desc_payment_qr),
            onClick = onPaymentQrClick
        )

        QrTypeCard(
            title = stringResource(Res.string.title_crypto_qr),
            description = stringResource(Res.string.desc_crypto_qr),
            onClick = onCryptoQrClick
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
