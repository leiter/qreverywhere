package cut.the.crap.qreverywhere.shared.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * iOS Camera View implementation
 *
 * Since live camera preview requires complex AVFoundation integration,
 * this implementation provides an image picker alternative for QR code scanning.
 * Users can select images from their photo library to scan for QR codes.
 *
 * TODO: Future enhancement - implement live camera using AVCaptureSession
 */
@Composable
actual fun CameraView(
    modifier: Modifier,
    config: CameraConfig,
    onQrCodeDetected: (QrCodeResult) -> Unit,
    onError: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val imagePicker = rememberImagePicker()
    val qrDetector = remember { QrCodeDetector() }

    var isProcessing by remember { mutableStateOf(false) }
    var lastError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "🖼️",
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Scan QR Code from Photo",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select an image from your photo library to scan for QR codes",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isProcessing) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Scanning...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Button(
                    onClick = {
                        scope.launch {
                            isProcessing = true
                            lastError = null

                            when (val result = imagePicker.pickImage()) {
                                is ImagePickerResult.Success -> {
                                    val detectedCodes = qrDetector.detectQrCodes(result.imageData)
                                    if (detectedCodes.isNotEmpty()) {
                                        onQrCodeDetected(detectedCodes.first())
                                    } else {
                                        lastError = "No QR code found in the selected image"
                                        onError("No QR code found in the selected image")
                                    }
                                }
                                is ImagePickerResult.Cancelled -> {
                                    // User cancelled, no action needed
                                }
                                is ImagePickerResult.Error -> {
                                    lastError = result.message
                                    onError(result.message)
                                }
                            }

                            isProcessing = false
                        }
                    }
                ) {
                    Text(
                        text = "🖼️",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Choose from Library")
                }
            }

            // Show error message if any
            lastError?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Info about live camera
            Text(
                text = "Live camera scanning coming soon",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}