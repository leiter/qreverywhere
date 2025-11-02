package cut.the.crap.qreverywhere.shared.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cut.the.crap.qreverywhere.shared.utils.Logger

/**
 * Scan Screen Placeholder for Compose Multiplatform
 *
 * Camera/QR scanning requires platform-specific implementations:
 * - Android: CameraX APIs
 * - iOS: AVFoundation (AVCaptureSession)
 * - Desktop: Webcam libraries
 *
 * This will be implemented using expect/actual pattern
 */
@Composable
fun ScanScreen(
    onQrCodeScanned: (String) -> Unit = {}
) {
    Logger.w("ScanScreen") {
        "Camera scanning not yet implemented for Compose Multiplatform. " +
                "Requires platform-specific camera APIs (expect/actual)"
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "📷",
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Camera Scanning",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "QR code scanning requires platform-specific camera APIs.\n\n" +
                        "This feature will be implemented using expect/actual declarations:\n" +
                        "• Android: CameraX\n" +
                        "• iOS: AVFoundation\n" +
                        "• Desktop: Webcam libraries",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
