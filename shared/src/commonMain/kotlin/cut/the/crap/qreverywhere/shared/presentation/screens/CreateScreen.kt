package cut.the.crap.qreverywhere.shared.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
    onEmailQrClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = "Create QR Code",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Choose what type of QR code to create",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Plain Text QR Code
        QrTypeCard(
            title = "Text QR Code",
            description = "Create a QR code with any text content",
            onClick = onTextQrClick
        )

        // Web URL QR Code
        QrTypeCard(
            title = "Web URL QR Code",
            description = "Create a QR code with website URL",
            onClick = onUrlQrClick
        )

        // Phone QR Code
        QrTypeCard(
            title = "Phone QR Code",
            description = "Create a QR code with phone number",
            onClick = onPhoneQrClick
        )

        // SMS QR Code
        QrTypeCard(
            title = "SMS QR Code",
            description = "Create a QR code with SMS message",
            onClick = onSmsQrClick
        )

        // Email QR Code
        QrTypeCard(
            title = "Email QR Code",
            description = "Create a QR code with email information",
            onClick = onEmailQrClick
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
