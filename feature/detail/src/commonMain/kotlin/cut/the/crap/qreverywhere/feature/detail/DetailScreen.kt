package cut.the.crap.qreverywhere.feature.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import cut.the.crap.qreverywhere.shared.domain.model.QrAction
import cut.the.crap.qreverywhere.shared.domain.model.snackbarLabel
import cut.the.crap.qreverywhere.shared.domain.usecase.QrActionResult
import cut.the.crap.qreverywhere.shared.domain.usecase.SafetyStatus
import cut.the.crap.qreverywhere.shared.domain.usecase.UrlSafetyChecker
import cut.the.crap.qreverywhere.shared.presentation.components.UrlWarningDialog
import cut.the.crap.qreverywhere.shared.presentation.components.WifiCredentialsCard
import cut.the.crap.qreverywhere.shared.presentation.state.State
import cut.the.crap.qreverywhere.shared.utils.toImagePainter
import cut.the.crap.qreverywhere.shared.utils.toReadableString
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import cut.the.crap.qreverywhere.core.base.generated.resources.Res
import cut.the.crap.qreverywhere.core.base.generated.resources.*

@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onNavigateBack: () -> Unit = {},
    onShare: () -> Unit = {},
    onCopyToClipboard: () -> Unit = {},
    onFullscreenClick: () -> Unit = {}
) {
    val detailItem by viewModel.detailViewItem.collectAsState()
    val detailState by viewModel.detailViewState.collectAsState()
    val lastDeletedItem by viewModel.lastDeletedItem.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val deletedMessage = stringResource(Res.string.detail_deleted)
    val undoLabel = stringResource(Res.string.action_undo)
    val savedMessage = stringResource(Res.string.feedback_saved)
    val saveErrorMessage = stringResource(Res.string.feedback_save_error)
    val openActionLabel = stringResource(Res.string.action_open)
    val noHandlerMessage = stringResource(Res.string.feedback_no_handler_available)

    // Test/Open action state: URL safety dialog and WiFi credentials reveal are both
    // presented as overlays rather than going through the Snackbar-with-action flow.
    val urlSafetyChecker = remember { UrlSafetyChecker() }
    var pendingUrlWarning by remember { mutableStateOf<Pair<QrAction.OpenUrl, cut.the.crap.qreverywhere.shared.domain.usecase.UrlSafetyResult>?>(null) }
    var wifiCredentialsToShow by remember { mutableStateOf<cut.the.crap.qreverywhere.shared.domain.model.WifiCredentials?>(null) }

    fun runAction(action: QrAction) {
        coroutineScope.launch {
            val result = viewModel.executeAction(action)
            when (result) {
                is QrActionResult.Success -> Unit
                is QrActionResult.NoHandlerAvailable -> {
                    snackbarHostState.showSnackbar(
                        message = noHandlerMessage,
                        duration = SnackbarDuration.Short
                    )
                }
                is QrActionResult.Failed -> {
                    snackbarHostState.showSnackbar(
                        message = result.reason,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    fun confirmActionViaSnackbar(action: QrAction) {
        coroutineScope.launch {
            val result = snackbarHostState.showSnackbar(
                message = action.snackbarLabel(),
                actionLabel = openActionLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                runAction(action)
            }
        }
    }

    fun onTestActionClick(action: QrAction) {
        when (action) {
            is QrAction.ConnectWifi -> {
                wifiCredentialsToShow = action.credentials
            }
            is QrAction.OpenUrl -> {
                val safetyResult = urlSafetyChecker.checkUrl(action.url)
                if (safetyResult.status == SafetyStatus.SAFE) {
                    confirmActionViaSnackbar(action)
                } else {
                    pendingUrlWarning = action to safetyResult
                }
            }
            is QrAction.NoAction -> Unit
            else -> confirmActionViaSnackbar(action)
        }
    }

    // Show undo snackbar when an item is deleted
    LaunchedEffect(lastDeletedItem) {
        if (lastDeletedItem != null) {
            val result = snackbarHostState.showSnackbar(
                message = deletedMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            } else {
                viewModel.clearLastDeletedItem()
            }
        }
    }

    // Show snackbar for save image result
    LaunchedEffect(Unit) {
        viewModel.saveQrImageEvent.collect { state ->
            when (state) {
                is State.Success -> {
                    snackbarHostState.showSnackbar(
                        message = savedMessage,
                        duration = SnackbarDuration.Short
                    )
                }
                is State.Error -> {
                    snackbarHostState.showSnackbar(
                        message = state.message ?: saveErrorMessage,
                        duration = SnackbarDuration.Short
                    )
                }
                is State.Loading -> {
                    // Optionally show loading state
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = detailState) {
                is State.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is State.Error -> {
                    Text(
                        text = stringResource(Res.string.detail_error, state.message),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                is State.Success, null -> {
                    detailItem?.let { qrItem ->
                        // QR preview animation
                        var animationStarted by remember { mutableStateOf(false) }
                        val scale by animateFloatAsState(
                            targetValue = if (animationStarted) 1f else 0.8f,
                            animationSpec = tween(durationMillis = 400)
                        )
                        val alpha by animateFloatAsState(
                            targetValue = if (animationStarted) 1f else 0f,
                            animationSpec = tween(durationMillis = 400)
                        )
                        LaunchedEffect(qrItem.id) { animationStarted = true }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            qrItem.imageData?.let { imageBytes ->
                                imageBytes.toImagePainter()?.let { painter ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp)
                                            .scale(scale)
                                            .alpha(alpha),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                        onClick = onFullscreenClick
                                    ) {
                                        Image(
                                            painter = painter,
                                            contentDescription = stringResource(Res.string.cd_qr_code),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .size(300.dp)
                                                .padding(16.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = stringResource(Res.string.detail_content),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = qrItem.textContent,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(Res.string.detail_details),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    DetailRow(stringResource(Res.string.detail_type), qrItem.acquireType.name)
                                    DetailRow(stringResource(Res.string.detail_created), qrItem.timestamp.toReadableString())
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ExtendedFloatingActionButton(
                                    onClick = onShare,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Share, stringResource(Res.string.cd_share))
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text(stringResource(Res.string.detail_share))
                                }

                                ExtendedFloatingActionButton(
                                    onClick = {
                                        viewModel.saveQrImageOfDetailView()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_save),
                                        contentDescription = stringResource(Res.string.cd_save)
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text(stringResource(Res.string.detail_save))
                                }

                                ExtendedFloatingActionButton(
                                    onClick = {
                                        viewModel.deleteCurrentDetailView()
                                        onNavigateBack()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ) {
                                    Icon(Icons.Default.Delete, stringResource(Res.string.cd_delete))
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text(stringResource(Res.string.detail_delete))
                                }

                                val resolvedAction = remember(qrItem) { viewModel.resolveAction() }
                                if (resolvedAction !is QrAction.NoAction) {
                                    ExtendedFloatingActionButton(
                                        onClick = { onTestActionClick(resolvedAction) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(actionIcon(resolvedAction), stringResource(Res.string.cd_test_action))
                                        Spacer(modifier = Modifier.size(8.dp))
                                        Text(stringResource(Res.string.detail_test_action))
                                    }
                                }
                            }
                        }
                    } ?: run {
                        Text(
                            text = stringResource(Res.string.detail_no_item),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }

    wifiCredentialsToShow?.let { credentials ->
        WifiCredentialsCard(
            credentials = credentials,
            onDismiss = { wifiCredentialsToShow = null }
        )
    }

    pendingUrlWarning?.let { (action, safetyResult) ->
        UrlWarningDialog(
            safetyResult = safetyResult,
            onDismiss = { pendingUrlWarning = null },
            onProceed = {
                pendingUrlWarning = null
                confirmActionViaSnackbar(action)
            },
            onCancel = { pendingUrlWarning = null }
        )
    }
}

/**
 * Icon reflecting the resolved [QrAction] type, shown on the Test/Open FAB.
 */
private fun actionIcon(action: QrAction): ImageVector = when (action) {
    is QrAction.OpenUrl -> Icons.Default.Link
    is QrAction.DialPhone -> Icons.Default.Call
    is QrAction.SendEmail -> Icons.Default.Email
    is QrAction.SendSms -> Icons.Default.Sms
    is QrAction.ConnectWifi -> Icons.Default.Wifi
    is QrAction.SaveContact -> Icons.Default.ContactPage
    is QrAction.AddCalendarEvent -> Icons.Default.Event
    is QrAction.ShowLocation -> Icons.Default.LocationOn
    is QrAction.NoAction -> Icons.Default.PlayArrow
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
