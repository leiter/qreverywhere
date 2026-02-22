package cut.the.crap.qreverywhere.feature.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import cut.the.crap.qreverywhere.shared.camera.ImagePickerResult
import cut.the.crap.qreverywhere.shared.camera.QrCodeDetector
import cut.the.crap.qreverywhere.shared.camera.QrCodeResult
import cut.the.crap.qreverywhere.shared.camera.rememberCameraPermissionManager
import cut.the.crap.qreverywhere.shared.camera.rememberImagePicker
import cut.the.crap.qreverywhere.shared.utils.Logger
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import qreverywhere.shared.generated.resources.Res
import qreverywhere.shared.generated.resources.action_dismiss
import qreverywhere.shared.generated.resources.action_ok
import qreverywhere.shared.generated.resources.feedback_no_qr_in_image
import qreverywhere.shared.generated.resources.ic_image_search
import qreverywhere.shared.generated.resources.permission_camera_denied
import qreverywhere.shared.generated.resources.permission_camera_permanently_denied
import qreverywhere.shared.generated.resources.permission_camera_required
import qreverywhere.shared.generated.resources.permission_grant
import qreverywhere.shared.generated.resources.permission_open_settings
import qreverywhere.shared.generated.resources.permission_ready_to_scan
import qreverywhere.shared.generated.resources.permission_start_scanning
import qreverywhere.shared.generated.resources.permission_start_scanning_hint
import qreverywhere.shared.generated.resources.scan_from_file
import qreverywhere.shared.generated.resources.scan_qr_detected
import qreverywhere.shared.generated.resources.cd_torch_on
import qreverywhere.shared.generated.resources.cd_torch_off

@Composable
fun ScanScreen(
    onQrCodeScanned: (String) -> Unit = {}
) {
    val permissionManager = rememberCameraPermissionManager()
    var permissionState by remember { mutableStateOf(CameraPermissionState.NOT_REQUESTED) }

    val imagePicker = rememberImagePicker()
    val qrCodeDetector = remember { QrCodeDetector() }
    var isPickingImage by remember { mutableStateOf(false) }

    val dismissLabel = stringResource(Res.string.action_dismiss)
    val okLabel = stringResource(Res.string.action_ok)
    val qrDetectedMessage = stringResource(Res.string.scan_qr_detected)
    val noQrFoundMessage = stringResource(Res.string.feedback_no_qr_in_image)
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

    LaunchedEffect(Unit) {
        try {
            permissionState = permissionManager.getPermissionState()
            if (permissionState == CameraPermissionState.NOT_REQUESTED) {
                permissionState = permissionManager.requestCameraPermission()
            }
        } catch (e: Exception) {
            Logger.e("ScanScreen") { "Error checking camera permission: ${e.message}" }
            permissionState = CameraPermissionState.DENIED
        }
    }

    val handlePickImage: () -> Unit = {
        scope.launch {
            isPickingImage = true
            when (val result = imagePicker.pickImage()) {
                is ImagePickerResult.Success -> {
                    val detectedCodes = qrCodeDetector.detectQrCodes(result.imageData)
                    if (detectedCodes.isNotEmpty()) {
                        val qrResult = detectedCodes.first()
                        handleQrCodeDetected(
                            result = qrResult,
                            lastScannedCode = lastScannedCode,
                            onUpdate = { lastScannedCode = it },
                            onQrCodeScanned = { code ->
                                hasScanned = true
                                onQrCodeScanned(code)
                            },
                            scope = scope,
                            snackbarHostState = snackbarHostState,
                            qrDetectedMessage = qrDetectedMessage,
                            okLabel = okLabel
                        )
                    } else {
                        snackbarHostState.showSnackbar(
                            message = noQrFoundMessage,
                            actionLabel = dismissLabel
                        )
                    }
                }
                is ImagePickerResult.Cancelled -> {}
                is ImagePickerResult.Error -> {
                    snackbarHostState.showSnackbar(
                        message = result.message,
                        actionLabel = dismissLabel
                    )
                }
            }
            isPickingImage = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (permissionState == CameraPermissionState.GRANTED && !hasScanned) {
                ExtendedFloatingActionButton(
                    onClick = handlePickImage,
                    icon = {
                        if (isPickingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Icon(
                                painter = painterResource(Res.drawable.ic_image_search),
                                contentDescription = stringResource(Res.string.scan_from_file)
                            )
                        }
                    },
                    text = { Text(stringResource(Res.string.scan_from_file)) },
                    expanded = !isPickingImage
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (permissionState) {
                CameraPermissionState.GRANTED -> {
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
                                        snackbarHostState = snackbarHostState,
                                        qrDetectedMessage = qrDetectedMessage,
                                        okLabel = okLabel
                                    )
                                }
                            },
                            onError = { errorMessage ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = errorMessage,
                                        actionLabel = dismissLabel
                                    )
                                }
                                Logger.e("ScanScreen") { errorMessage }
                            }
                        )

                        CameraControlsOverlay(
                            cameraConfig = cameraConfig,
                            onConfigChange = { cameraConfig = it },
                            modifier = Modifier.align(Alignment.TopEnd)
                        )

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
    val torchDescription = stringResource(
        if (cameraConfig.enableTorch) Res.string.cd_torch_on else Res.string.cd_torch_off
    )
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
                text = if (cameraConfig.enableTorch) "\uD83D\uDCA1" else "\uD83D\uDD26",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics { contentDescription = torchDescription }
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
    )
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
            text = "\uD83D\uDCF7",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.permission_camera_required),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isPermanentlyDenied) {
                stringResource(Res.string.permission_camera_permanently_denied)
            } else {
                stringResource(Res.string.permission_camera_denied)
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
                text = if (isPermanentlyDenied)
                    stringResource(Res.string.permission_open_settings)
                else
                    stringResource(Res.string.permission_grant)
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
            text = "\uD83D\uDCF7",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.permission_ready_to_scan),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.permission_start_scanning_hint),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.permission_start_scanning))
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
    snackbarHostState: SnackbarHostState,
    qrDetectedMessage: String,
    okLabel: String
) {
    if (result.text != lastScannedCode) {
        onUpdate(result.text)
        onQrCodeScanned(result.text)

        val displayText = result.text.take(50) + if (result.text.length > 50) "..." else ""
        scope.launch {
            snackbarHostState.showSnackbar(
                message = qrDetectedMessage.replace("%1\$s", displayText),
                actionLabel = okLabel
            )
        }

        Logger.d("ScanScreen") {
            "QR Code detected: ${result.text}"
        }
    }
}
