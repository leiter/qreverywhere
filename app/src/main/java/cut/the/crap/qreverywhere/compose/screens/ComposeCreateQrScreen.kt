package cut.the.crap.qreverywhere.compose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import cut.the.crap.qreverywhere.compose.navigation.ComposeScreen
import cut.the.crap.qreverywhere.ui.theme.QrEveryWhereTheme

/**
 * Compose version of CreateQrCodeFragment
 * Main screen for choosing QR code creation type
 */
@Composable
fun ComposeCreateQrScreen(navController: NavController) {
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

        // Email QR Code
        QrTypeCard(
            title = "Email QR Code",
            description = "Create a QR code with email information",
            onClick = {
                navController.navigate(ComposeScreen.CreateEmail.route)
            }
        )

        // Phone QR Code
        QrTypeCard(
            title = "Phone QR Code",
            description = "Create a QR code with phone number",
            onClick = {
                navController.navigate(ComposeScreen.CreateOneLiner.createRoute(0)) // CREATE_PHONE
            }
        )

        // SMS QR Code
        QrTypeCard(
            title = "SMS QR Code",
            description = "Create a QR code with SMS message",
            onClick = {
                navController.navigate(ComposeScreen.CreateOneLiner.createRoute(1)) // CREATE_SMS
            }
        )

        // Web URL QR Code
        QrTypeCard(
            title = "Web URL QR Code",
            description = "Create a QR code with website URL",
            onClick = {
                navController.navigate(ComposeScreen.CreateOneLiner.createRoute(2)) // CREATE_WEB
            }
        )
    }
}

@Composable
fun QrTypeCard(
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

@Preview(name = "Create Screen Light", showBackground = true)
@Composable
fun ComposeCreateQrScreenPreview() {
    QrEveryWhereTheme(darkTheme = false) {
        ComposeCreateQrScreen(rememberNavController())
    }
}

@Preview(name = "Create Screen Dark", showBackground = true)
@Composable
fun ComposeCreateQrScreenDarkPreview() {
    QrEveryWhereTheme(darkTheme = true) {
        ComposeCreateQrScreen(rememberNavController())
    }
}
