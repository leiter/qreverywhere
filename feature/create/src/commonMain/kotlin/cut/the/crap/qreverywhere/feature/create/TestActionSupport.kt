package cut.the.crap.qreverywhere.feature.create

import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cut.the.crap.qreverywhere.core.base.generated.resources.Res
import cut.the.crap.qreverywhere.core.base.generated.resources.cd_test_action
import cut.the.crap.qreverywhere.core.base.generated.resources.detail_test_action
import cut.the.crap.qreverywhere.core.base.generated.resources.action_open
import cut.the.crap.qreverywhere.core.base.generated.resources.feedback_no_handler_available
import cut.the.crap.qreverywhere.shared.domain.model.QrAction
import cut.the.crap.qreverywhere.shared.domain.model.WifiCredentials
import cut.the.crap.qreverywhere.shared.domain.model.snackbarLabel
import cut.the.crap.qreverywhere.shared.domain.usecase.QrActionResult
import cut.the.crap.qreverywhere.shared.domain.usecase.SafetyStatus
import cut.the.crap.qreverywhere.shared.domain.usecase.UrlSafetyChecker
import cut.the.crap.qreverywhere.shared.domain.usecase.UrlSafetyResult
import cut.the.crap.qreverywhere.shared.presentation.components.UrlWarningDialog
import cut.the.crap.qreverywhere.shared.presentation.components.WifiCredentialsCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * Shared "Test" action support for creation screens, mirroring the Test/Open flow on
 * [cut.the.crap.qreverywhere.feature.detail.DetailScreen] (Snackbar-with-action for
 * routine actions, [UrlWarningDialog] for risky URLs, [WifiCredentialsCard] reveal for
 * WiFi) so every CreateXScreen doesn't have to re-implement this branching by hand.
 *
 * Deliberate deviation from Detail's flow: Detail double-confirms a WARNING/DANGEROUS URL
 * (UrlWarningDialog "Open Anyway" -> Snackbar "Open" -> actually opens). Here, proceeding
 * past the safety dialog runs the action immediately with no second Snackbar confirm.
 * Pressing "Test" on a creation screen is already a deliberate, explicit dry-run action
 * (unlike Detail's FAB, which can sit on screen for an already-saved/scanned item across
 * multiple visits) - a second confirmation after "Open Anyway" adds friction without adding
 * safety, so we skip it here. SAFE URLs still go through the single Snackbar-with-action
 * confirm, same as Detail and same as every other action type.
 */
class TestActionState internal constructor(
    private val viewModel: CreateViewModel,
    private val snackbarHostState: SnackbarHostState,
    private val coroutineScope: CoroutineScope,
    private val openActionLabel: String,
    private val noHandlerMessage: String
) {
    var pendingUrlWarning by mutableStateOf<Pair<QrAction.OpenUrl, UrlSafetyResult>?>(null)
        private set
    var wifiCredentialsToShow by mutableStateOf<WifiCredentials?>(null)
        private set

    private val urlSafetyChecker = UrlSafetyChecker()

    private fun runAction(action: QrAction) {
        coroutineScope.launch {
            when (val result = viewModel.executeTestAction(action)) {
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

    private fun confirmActionViaSnackbar(action: QrAction) {
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

    /** Resolves [content] to the [QrAction] it represents, without dispatching anything. */
    fun resolve(content: String): QrAction = viewModel.resolveTestAction(content)

    /** Entry point for the Test button: resolves [content] and routes it to the right UX. */
    fun onTestActionClick(content: String) {
        when (val action = viewModel.resolveTestAction(content)) {
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

    /** Proceeds past a shown URL safety warning - see class doc for the no-double-confirm choice. */
    fun proceedPastUrlWarning() {
        val pending = pendingUrlWarning ?: return
        pendingUrlWarning = null
        runAction(pending.first)
    }

    fun dismissUrlWarning() {
        pendingUrlWarning = null
    }

    fun dismissWifiCredentials() {
        wifiCredentialsToShow = null
    }
}

@Composable
fun rememberTestActionState(
    viewModel: CreateViewModel,
    snackbarHostState: SnackbarHostState
): TestActionState {
    val coroutineScope = rememberCoroutineScope()
    val openActionLabel = stringResource(Res.string.action_open)
    val noHandlerMessage = stringResource(Res.string.feedback_no_handler_available)
    return remember(viewModel, snackbarHostState) {
        TestActionState(
            viewModel = viewModel,
            snackbarHostState = snackbarHostState,
            coroutineScope = coroutineScope,
            openActionLabel = openActionLabel,
            noHandlerMessage = noHandlerMessage
        )
    }
}

/**
 * Secondary "Test" button for a creation screen, resolving [content] on click and driving
 * [state]'s Snackbar/dialog/card flow. Never calls [CreateViewModel.createQrItem] - purely
 * a dry-run dispatch of the resolved [QrAction]. Hidden when [content] is blank or resolves
 * to [QrAction.NoAction] - same precedent as Detail's Test/Open FAB, which is only shown
 * when `resolveAction() !is QrAction.NoAction`. This also covers content that looks
 * plausible but isn't actually actionable yet (e.g. a bitcoin:/ethereum: URI, which has no
 * [QrAction] mapping and would otherwise show a button that silently does nothing on click).
 */
@Composable
fun TestActionButton(
    content: String,
    state: TestActionState,
    modifier: Modifier = Modifier
) {
    if (content.isBlank()) return
    if (state.resolve(content) is QrAction.NoAction) return

    OutlinedButton(
        onClick = { state.onTestActionClick(content) },
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(testActionIcon(content), stringResource(Res.string.cd_test_action))
        Text(
            text = stringResource(Res.string.detail_test_action),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

/** Renders the overlay dialogs (URL warning, WiFi credentials reveal) driven by [state]. */
@Composable
fun TestActionOverlays(state: TestActionState) {
    state.wifiCredentialsToShow?.let { credentials ->
        WifiCredentialsCard(
            credentials = credentials,
            onDismiss = { state.dismissWifiCredentials() }
        )
    }

    state.pendingUrlWarning?.let { (_, safetyResult) ->
        UrlWarningDialog(
            safetyResult = safetyResult,
            onDismiss = { state.dismissUrlWarning() },
            onProceed = { state.proceedPastUrlWarning() },
            onCancel = { state.dismissUrlWarning() }
        )
    }
}

private fun testActionIcon(content: String): ImageVector {
    // Icon only - resolution here is cheap/pure and avoids plumbing the resolved QrAction
    // through just for the icon; the actual click handler re-resolves via the ViewModel.
    return when {
        content.startsWith("tel:") -> Icons.Default.Call
        content.startsWith("sms:") || content.startsWith("smsto:") -> Icons.Default.Sms
        content.startsWith("mailto:") -> Icons.Default.Email
        content.startsWith("WIFI:") -> Icons.Default.Wifi
        content.startsWith("BEGIN:VCARD") || content.startsWith("MECARD:") -> Icons.Default.ContactPage
        content.startsWith("BEGIN:VEVENT") || content.startsWith("VEVENT") -> Icons.Default.Event
        content.startsWith("geo:") -> Icons.Default.LocationOn
        content.startsWith("http://") || content.startsWith("https://") -> Icons.Default.Link
        else -> Icons.Default.PlayArrow
    }
}
