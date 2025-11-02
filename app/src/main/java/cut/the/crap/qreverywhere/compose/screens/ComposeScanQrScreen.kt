package cut.the.crap.qreverywhere.compose.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.zxing.BinaryBitmap
import com.google.zxing.ChecksumException
import com.google.zxing.FormatException
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Reader
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import cut.the.crap.qreverywhere.MainActivity
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.compose.navigation.ComposeScreen
import cut.the.crap.qreverywhere.utils.data.IntentGenerator
import cut.the.crap.qreverywhere.utils.ui.FROM_SCAN_QR
import cut.the.crap.qreverywhere.utils.ui.hasPermission
import cut.the.crap.qrrepository.Acquire
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Helper function to get the correct storage permission based on Android version
 */
private fun getStoragePermission(): String {
    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2)
        Manifest.permission.READ_MEDIA_IMAGES
    else
        Manifest.permission.READ_EXTERNAL_STORAGE
}

/**
 * Scans QR code from an image URI
 */
private fun scanQrImage(uri: Uri, context: Context): Result? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val intArray = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        val source: LuminanceSource = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        val reader: Reader = MultiFormatReader()
        reader.decode(binaryBitmap)
    } catch (e: NotFoundException) {
        Timber.e(e, "No QR code found in image")
        null
    } catch (e: ChecksumException) {
        Timber.e(e, "Checksum error while decoding QR code")
        null
    } catch (e: FormatException) {
        Timber.e(e, "Format error while decoding QR code")
        null
    } catch (e: Exception) {
        Timber.e(e, "Error scanning QR code from image")
        null
    }
}

/**
 * Compose version of HomeFragment (Scan QR Code screen)
 * Handles camera permission and QR scanning
 */
@Composable
fun ComposeScanQrScreen(
    navController: NavController,
    viewModel: MainActivityViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var hasCameraPermission by remember { mutableStateOf(context.hasPermission(Manifest.permission.CAMERA)) }
    var hasStoragePermission by remember { mutableStateOf(context.hasPermission(getStoragePermission())) }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        Timber.d("Camera permission granted: $isGranted")
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            Timber.d("Image selected: $uri")
            val result = scanQrImage(uri, context)
            result?.let { qrResult ->
                Timber.d("QR code found in image: ${qrResult.text}")
                viewModel.saveQrItemFromFile(qrResult.text, Acquire.FROM_FILE)
                navController.navigate(ComposeScreen.DetailView.createRoute(FROM_SCAN_QR))
            } ?: scope.launch {
                snackbarHostState.showSnackbar("No QR code found in image")
            }
        }
    }

    // Storage permission launcher
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasStoragePermission = isGranted
        Timber.d("Storage permission granted: $isGranted")
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        }
    }

    // Check permission state on first composition
    LaunchedEffect(Unit) {
        hasCameraPermission = context.hasPermission(Manifest.permission.CAMERA)
        hasStoragePermission = context.hasPermission(getStoragePermission())
        Timber.d("Initial camera permission state: $hasCameraPermission")
        Timber.d("Initial storage permission state: $hasStoragePermission")
    }

    // Helper function to handle file picker launch with permission check
    val launchFilePicker: () -> Unit = {
        if (context.hasPermission(getStoragePermission())) {
            Timber.d("Storage permission granted, launching image picker")
            imagePickerLauncher.launch("image/*")
        } else {
            if (context is androidx.activity.ComponentActivity &&
                ActivityCompat.shouldShowRequestPermissionRationale(context, getStoragePermission())) {
                // User denied permission previously, open app settings
                Timber.d("Opening app settings for storage permission")
                context.startActivity(IntentGenerator.OpenAppSettings.getIntent())
            } else {
                // Request permission
                Timber.d("Requesting storage permission")
                storagePermissionLauncher.launch(getStoragePermission())
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            // Show FAB when camera permission is granted
            if (hasCameraPermission) {
                ExtendedFloatingActionButton(
                    onClick = launchFilePicker,
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_image_search),
                            contentDescription = "Scan from file"
                        )
                    },
                    text = { Text(context.getString(R.string.qrScanFabTextFromFile)) }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                hasCameraPermission -> {
                    // TODO: Show camera preview here
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Camera Preview Coming Soon",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "CameraX integration pending",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                else -> {
                    // Show permission request UI
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
                    ) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                // Check if we should show rationale or request permission
                                if (context is androidx.activity.ComponentActivity &&
                                    ActivityCompat.shouldShowRequestPermissionRationale(
                                        context, Manifest.permission.CAMERA)) {
                                    // User denied permission previously, open app settings
                                    Timber.d("Opening app settings for camera permission")
                                    context.startActivity(IntentGenerator.OpenAppSettings.getIntent())
                                } else {
                                    // First time requesting or user hasn't denied permanently
                                    Timber.d("Requesting camera permission")
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(context.getString(R.string.requestCameraPermission))
                        }

                        ExtendedFloatingActionButton(
                            onClick = launchFilePicker,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(context.getString(R.string.requestQrFromFile))
                        }
                    }
                }
            }
        }
    }
}

// Preview removed - requires ViewModel parameter
