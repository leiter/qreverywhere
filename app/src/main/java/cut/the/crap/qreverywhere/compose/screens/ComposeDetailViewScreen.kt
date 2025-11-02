package cut.the.crap.qreverywhere.compose.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import cut.the.crap.qreverywhere.MainActivityViewModel
import cut.the.crap.qreverywhere.saveQrImageOfDetailView
import cut.the.crap.qreverywhere.detailViewQrCodeItem
import cut.the.crap.qreverywhere.detailViewQrCodeItemState
import cut.the.crap.qreverywhere.R
import cut.the.crap.qreverywhere.compose.navigation.ComposeScreen
import cut.the.crap.qreverywhere.shared.presentation.state.State
import cut.the.crap.qreverywhere.shared.presentation.state.getData
import cut.the.crap.qreverywhere.shared.presentation.viewmodel.MainViewModel
import cut.the.crap.qreverywhere.utils.data.AcquireDateFormatter
import cut.the.crap.qreverywhere.utils.data.IntentGenerator
import cut.the.crap.qreverywhere.utils.data.ProtocolPrefix
import cut.the.crap.qreverywhere.utils.data.QrCodeType
import cut.the.crap.qreverywhere.utils.data.detailTitle
import cut.the.crap.qreverywhere.utils.data.determineType
import cut.the.crap.qreverywhere.utils.data.fabLaunchIcon
import cut.the.crap.qreverywhere.utils.data.isVcard
import cut.the.crap.qreverywhere.utils.ui.FROM_CREATE_CONTEXT
import cut.the.crap.qreverywhere.utils.ui.FROM_HISTORY_LIST
import cut.the.crap.qreverywhere.utils.ui.FROM_SCAN_QR
import cut.the.crap.qreverywhere.utils.ui.hasPermission
import cut.the.crap.qrrepository.QrItem
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import timber.log.Timber

/**
 * Helper function to get the correct storage write permission based on Android version
 * For Android 10+ (API 29+), writing to Downloads doesn't require permission with scoped storage
 * For Android 9 and below, WRITE_EXTERNAL_STORAGE is required
 */
private fun getWriteStoragePermission(): String? {
    return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
        // Android 9 (Pie) and below need WRITE_EXTERNAL_STORAGE
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    } else {
        // Android 10+ uses scoped storage, no permission needed for Downloads
        null
    }
}

/**
 * Compose version of DetailViewFragment
 * Displays QR code details with image, content, and action buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeDetailViewScreen(
    navController: NavController,
    originFlag: Int,
    viewModel: MainActivityViewModel,
    dateFormatter: AcquireDateFormatter = koinInject()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val activityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { }

    // Storage permission launcher for saving QR code images
    val writeStoragePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Timber.d("Write storage permission granted, saving QR image")
            viewModel.saveQrImageOfDetailView(context)
        } else {
            Timber.w("Write storage permission denied")
            scope.launch {
                snackbarHostState.showSnackbar("Storage permission is required to save QR code")
            }
        }
    }

    // Helper function to handle save QR code with permission check
    val handleSaveQrCode: () -> Unit = {
        val requiredPermission = getWriteStoragePermission()

        if (requiredPermission == null) {
            // Android 10+ doesn't need permission for Downloads directory
            Timber.d("No permission needed for saving (Android 10+)")
            viewModel.saveQrImageOfDetailView(context)
        } else {
            // Android 9 and below need WRITE_EXTERNAL_STORAGE
            if (context.hasPermission(requiredPermission)) {
                Timber.d("Write storage permission already granted")
                viewModel.saveQrImageOfDetailView(context)
            } else {
                // Check if we should show rationale
                if (context is androidx.activity.ComponentActivity &&
                    ActivityCompat.shouldShowRequestPermissionRationale(context, requiredPermission)) {
                    // User has denied before, open app settings
                    Timber.d("Opening app settings for write storage permission")
                    scope.launch {
                        snackbarHostState.showSnackbar("Please grant storage permission in app settings")
                    }
                    context.startActivity(IntentGenerator.OpenAppSettings.getIntent())
                } else {
                    // Request permission
                    Timber.d("Requesting write storage permission")
                    writeStoragePermissionLauncher.launch(requiredPermission)
                }
            }
        }
    }

    // For scanned items, collect StateFlow
    val scannedItemState by viewModel.detailViewQrCodeItemState.collectAsStateWithLifecycle()

    // For history/create items, use state to hold the captured item
    var capturedItem by remember { mutableStateOf<QrItem?>(null) }

    // Capture the item from ViewModel when screen loads
    // Poll until we get a valid item (not the default empty one)
    LaunchedEffect(originFlag) {
        if (originFlag == FROM_HISTORY_LIST || originFlag == FROM_CREATE_CONTEXT) {
            // Poll for a valid item with a small delay
            var attempts = 0
            while (attempts < 20) { // Try for up to 1 second
                val item = viewModel.detailViewQrCodeItem
                // Check if this is a valid item (not the default empty one with id=0 and 1x1 bitmap)
                if (item.id != 0 || item.img.width > 1) {
                    capturedItem = item
                    Timber.d("Captured VALID item from ViewModel: id=${item.id}, bitmap=${item.img.width}x${item.img.height}, content=${item.textContent.take(50)}")
                    break
                } else {
                    Timber.w("Attempt $attempts: Item not ready yet (id=${item.id}, bitmap=${item.img.width}x${item.img.height}), waiting...")
                    kotlinx.coroutines.delay(50) // Wait 50ms before retrying
                    attempts++
                }
            }
        }
    }

    // Determine which item to display based on origin
    val qrItem: QrItem? = when (originFlag) {
        FROM_HISTORY_LIST, FROM_CREATE_CONTEXT -> capturedItem
        FROM_SCAN_QR -> scannedItemState?.getData()
        else -> null
    }

    // Check loading state for scanned items
    val isLoading = if (originFlag == FROM_SCAN_QR) {
        scannedItemState is State.Loading
    } else {
        false
    }

    // Collect save QR image events from SharedFlow
    LaunchedEffect(Unit) {
        viewModel.saveQrImageEvent.collect { state ->
            when (state) {
                is State.Success -> {
                    snackbarHostState.showSnackbar("QR code saved to file")
                }
                is State.Error -> {
                    snackbarHostState.showSnackbar("Error saving QR code")
                }
                is State.Loading -> {
                    // Loading state handled elsewhere
                }
            }
        }
    }

    val item = qrItem
    val title = item?.let { stringResource(it.detailTitle) } ?: ""
    val subtitle = item?.let { dateFormatter.getTimeTemplate(it) } ?: ""

    // Debug logging
    LaunchedEffect(item, originFlag) {
        Timber.d("DetailViewScreen - originFlag: $originFlag")
        if (item != null) {
            Timber.d("DetailViewScreen - QrItem loaded: id=${item.id}, bitmapSize=${item.img.width}x${item.img.height}, content=${item.textContent.take(50)}")
        } else {
            Timber.w("DetailViewScreen - QrItem is NULL!")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title)
                        if (subtitle.isNotEmpty()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (originFlag == FROM_CREATE_CONTEXT) {
                            navController.navigate(ComposeScreen.History.route) {
                                popUpTo(ComposeScreen.Create.route) { inclusive = true }
                            }
                        } else {
                            navController.navigateUp()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Save to file") },
                            onClick = {
                                showMenu = false
                                handleSaveQrCode()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Share, "Save")
                            }
                        )
                        if (originFlag != FROM_SCAN_QR) {
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    showMenu = false
                                    viewModel.deleteCurrentDetailView()
                                    navController.navigate(ComposeScreen.History.route) {
                                        popUpTo(ComposeScreen.History.route) { inclusive = true }
                                    }
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, "Delete")
                                }
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            item?.let { qr ->
                if (qr.determineType() != QrCodeType.UNKNOWN_CONTENT) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            val intent = IntentGenerator.QrStartIntent(qr.textContent).getIntent()
                            try {
                                activityLauncher.launch(intent)
                            } catch (e: Exception) {
                                // Handle error gracefully
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = qr.fabLaunchIcon),
                                contentDescription = "Launch"
                            )
                        },
                        text = { Text(getLaunchButtonText(qr)) }
                    )
                }
            }
        }
    ) { paddingValues ->
        when {
            item == null && isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Loading QR code...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item == null -> {
                // Debug: Show when item is null but not loading
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "No QR code data available",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Origin flag: $originFlag",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // QR Code Image Card
                Card(
                    modifier = Modifier
                        .size(240.dp)
                        .clickable {
                            navController.navigate(ComposeScreen.Fullscreen.createRoute(FROM_HISTORY_LIST))
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = item.img.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier
                                .size(224.dp)
                                .padding(8.dp)
                        )
                    }
                }

                Text(
                    text = "Tap to view fullscreen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Content Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Content Title with Divider
                    Text(
                        text = stringResource(R.string.detailview_content_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    HorizontalDivider()

                    // Content Card
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Content Text
                            Text(
                                text = Uri.decode(item.textContent),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth()
                            )

                            HorizontalDivider()

                            // Copy Button
                            Surface(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("QR Content", Uri.decode(item.textContent))
                                    clipboard.setPrimaryClip(clip)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Copied to clipboard")
                                    }
                                },
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "📋 Copy to clipboard",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Add spacing at bottom for FAB
                Spacer(modifier = Modifier.height(80.dp))
            }
            }
        }
    }
}

@Composable
private fun getLaunchButtonText(qrItem: QrItem): String {
    val context = LocalContext.current
    val decoded = Uri.decode(qrItem.textContent)
    val launchTextTemplate = context.getString(R.string.qr_detail_launch_template)

    return when {
        decoded.startsWith(ProtocolPrefix.TEL) ->
            launchTextTemplate.format(context.getString(R.string.ic_open_phone_app))
        decoded.startsWith(ProtocolPrefix.MAILTO) ->
            launchTextTemplate.format(context.getString(R.string.ic_open_mail_app))
        decoded.startsWith(ProtocolPrefix.HTTP) || decoded.startsWith(ProtocolPrefix.HTTPS) ->
            launchTextTemplate.format(context.getString(R.string.ic_open_in_browser))
        qrItem.isVcard() ->
            context.getString(R.string.ic_import_contact)
        else ->
            context.getString(R.string.app_name)
    }
}

// Preview removed - requires ViewModel parameter
