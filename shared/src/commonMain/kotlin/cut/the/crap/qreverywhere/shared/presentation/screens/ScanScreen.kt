package cut.the.crap.qreverywhere.shared.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cut.the.crap.qreverywhere.shared.camera.CameraConfig
import cut.the.crap.qreverywhere.shared.camera.CameraFacing
import cut.the.crap.qreverywhere.shared.camera.CameraPermissionState
import cut.the.crap.qreverywhere.shared.camera.CameraView
import cut.the.crap.qreverywhere.shared.camera.QrCodeResult
import cut.the.crap.qreverywhere.shared.camera.rememberCameraPermissionManager
import cut.the.crap.qreverywhere.shared.utils.Logger
import kotlinx.coroutines.launch

/**
 * QR Code Scanning Screen with Camera Support
 */
@Composable
fun ScanScreen(
    onQrCodeScanned: (String) -> Unit = {}
) {
    val permissionManager = rememberCameraPermissionManager()
    var permissionState by remember { mutableStateOf(CameraPermissionState.NOT_REQUESTED) }
    var cameraConfig by remember {
        mutableStateOf(
            CameraConfig(
                enableTorch = false,
                enableAutoFocus = true,
                enableQrDetection = true,
                cameraFacing = CameraFacing.BACK
            )
        )
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var lastScannedCode by remember { mutableStateOf<String?>(null) }
    var hasScanned by remember { mutableStateOf(false) }

    // Check permission on first composition
    LaunchedEffect(Unit) {
        permissionState = permissionManager.getPermissionState()
        if (permissionState == CameraPermissionState.NOT_REQUESTED) {
            permissionState = permissionManager.requestCameraPermission()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (permissionState) {
                CameraPermissionState.GRANTED -> {
                    // Camera View with QR detection - only show if not already scanned
                    if (!hasScanned) {
                        CameraView(
                            modifier = Modifier.fillMaxSize(),
                            config = cameraConfig,
                            onQrCodeDetected = { result ->
                                if (!hasScanned) {
                                    handleQrCodeDetected(
                                        result = result,
                                        lastScannedCode = lastScannedCode,
                                        onUpdate = { lastScannedCode = it },
                                        onQrCodeScanned = { code ->
                                            hasScanned = true
                                            onQrCodeScanned(code)
                                        },
                                        scope = scope,
                                        snackbarHostState = snackbarHostState
                                    )
                                }
                            },
                            onError = { errorMessage ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = errorMessage,
                                        actionLabel = "Dismiss"
                                    )
                                }
                                Logger.e("ScanScreen") { errorMessage }
                            }
                        )

                        // Camera controls overlay
                        CameraControlsOverlay(
                            cameraConfig = cameraConfig,
                            onConfigChange = { cameraConfig = it },
                            modifier = Modifier.align(Alignment.TopEnd)
                        )

                        // Scan indicator overlay
                        ScanIndicatorOverlay(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                CameraPermissionState.DENIED,
                CameraPermissionState.PERMANENTLY_DENIED -> {
                    PermissionDeniedContent(
                        isPermanentlyDenied = permissionState == CameraPermissionState.PERMANENTLY_DENIED,
                        onRequestPermission = {
                            scope.launch {
                                if (permissionState == CameraPermissionState.PERMANENTLY_DENIED) {
                                    permissionManager.openAppSettings()
                                } else {
                                    permissionState = permissionManager.requestCameraPermission()
                                }
                            }
                        }
                    )
                }

                CameraPermissionState.NOT_REQUESTED -> {
                    PermissionNotRequestedContent(
                        onRequestPermission = {
                            scope.launch {
                                permissionState = permissionManager.requestCameraPermission()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraControlsOverlay(
    cameraConfig: CameraConfig,
    onConfigChange: (CameraConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        IconButton(
            onClick = {
                onConfigChange(cameraConfig.copy(enableTorch = !cameraConfig.enableTorch))
            }
        ) {
            Text(
                text = if (cameraConfig.enableTorch) "💡" else "🔦",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ScanIndicatorOverlay(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.7f)
            .height(250.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent)
    ) {
        // You can add a scanning frame indicator here if needed
    }
}

@Composable
private fun PermissionDeniedContent(
    isPermanentlyDenied: Boolean,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "📷",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isPermanentlyDenied) {
                "Camera permission has been permanently denied. Please enable it in your device settings to scan QR codes."
            } else {
                "Camera permission is required to scan QR codes. Please grant the permission to continue."
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isPermanentlyDenied) "Open Settings" else "Grant Permission"
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PermissionNotRequestedContent(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "📷",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Ready to Scan",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tap the button below to start scanning QR codes with your camera.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Scanning")
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

private fun handleQrCodeDetected(
    result: QrCodeResult,
    lastScannedCode: String?,
    onUpdate: (String) -> Unit,
    onQrCodeScanned: (String) -> Unit,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    // Avoid duplicate detections
    if (result.text != lastScannedCode) {
        onUpdate(result.text)
        onQrCodeScanned(result.text)

        scope.launch {
            snackbarHostState.showSnackbar(
                message = "QR Code detected: ${result.text.take(50)}${if (result.text.length > 50) "..." else ""}",
                actionLabel = "OK"
            )
        }

        Logger.d("ScanScreen") {
            "QR Code detected: ${result.text}"
        }
    }
}