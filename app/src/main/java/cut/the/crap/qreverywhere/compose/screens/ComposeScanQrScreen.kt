package cut.the.crap.qreverywhere.compose.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import cut.the.crap.qreverywhere.MainActivity
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.compose.navigation.ComposeScreen
import cut.the.crap.qreverywhere.ui.theme.QrEveryWhereTheme

/**
 * Compose version of HomeFragment (Scan QR Code screen)
 * TODO: Implement camera preview and QR scanning functionality
 */
@Composable
fun ComposeScanQrScreen(
    navController: NavController,
    viewModel: MainActivityViewModel
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = "Scan QR Code",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Camera preview will be here",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Button(onClick = {
            // TODO: Open file picker for QR code from file
        }) {
            Text("Load from File")
        }

        Button(onClick = {
            navController.navigate(ComposeScreen.Settings.route)
        }) {
            Text("Settings")
        }

        Text(
            text = "This is a placeholder for the Scan screen.\nCameraX integration coming soon.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Button to switch to classic fragment-based UI
        OutlinedButton(onClick = {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }) {
            Text("Switch to Classic UI")
        }
    }
}

// Preview removed - requires ViewModel parameter
