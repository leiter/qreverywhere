package cut.the.crap.qreverywhere.shared.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cut.the.crap.qreverywhere.shared.domain.usecase.SafetyStatus
import cut.the.crap.qreverywhere.shared.domain.usecase.UrlSafetyResult
import org.jetbrains.compose.resources.stringResource
import qreverywhere.shared.generated.resources.Res
import qreverywhere.shared.generated.resources.*

/**
 * Dialog showing URL safety warnings before opening a potentially dangerous link
 */
@Composable
fun UrlWarningDialog(
    safetyResult: UrlSafetyResult,
    onDismiss: () -> Unit,
    onProceed: () -> Unit,
    onCancel: () -> Unit
) {
    val titleText = when (safetyResult.status) {
        SafetyStatus.DANGEROUS -> stringResource(Res.string.url_warning_dangerous)
        SafetyStatus.WARNING -> stringResource(Res.string.url_warning_suspicious)
        SafetyStatus.SAFE -> stringResource(Res.string.url_warning_check)
    }
    val descriptionText = when (safetyResult.status) {
        SafetyStatus.DANGEROUS -> stringResource(Res.string.url_warning_dangerous_desc)
        SafetyStatus.WARNING -> stringResource(Res.string.url_warning_suspicious_desc)
        SafetyStatus.SAFE -> stringResource(Res.string.url_warning_safe_desc)
    }
    val urlPrefixText = stringResource(Res.string.url_warning_url_prefix)
    val warningsLabelText = stringResource(Res.string.url_warning_warnings_label)
    val openAnywayText = stringResource(Res.string.url_warning_open_anyway)
    val closeText = stringResource(Res.string.url_warning_close)
    val cancelText = stringResource(Res.string.url_warning_cancel)

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = stringResource(Res.string.cd_warning),
                tint = when (safetyResult.status) {
                    SafetyStatus.DANGEROUS -> MaterialTheme.colorScheme.error
                    SafetyStatus.WARNING -> MaterialTheme.colorScheme.tertiary
                    SafetyStatus.SAFE -> MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = titleText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "$urlPrefixText ${safetyResult.originalUrl.take(50)}${if (safetyResult.originalUrl.length > 50) "..." else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (safetyResult.warnings.isNotEmpty()) {
                    Text(
                        text = warningsLabelText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    safetyResult.warnings.forEach { warning ->
                        Row(
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "\u2022 ",
                                color = when (safetyResult.status) {
                                    SafetyStatus.DANGEROUS -> MaterialTheme.colorScheme.error
                                    SafetyStatus.WARNING -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                            Text(
                                text = warning,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = descriptionText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            if (safetyResult.status != SafetyStatus.DANGEROUS) {
                TextButton(onClick = onProceed) {
                    Text(openAnywayText)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(
                    text = if (safetyResult.status == SafetyStatus.DANGEROUS) closeText else cancelText
                )
            }
        }
    )
}

/**
 * Simplified composable function to check and show warning if needed
 */
@Composable
fun UrlSafetyCheck(
    url: String,
    urlSafetyChecker: cut.the.crap.qreverywhere.shared.domain.usecase.UrlSafetyChecker,
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onProceed: () -> Unit,
    onCancel: () -> Unit
) {
    if (showDialog) {
        val safetyResult = urlSafetyChecker.checkUrl(url)

        if (!safetyResult.isSafe) {
            UrlWarningDialog(
                safetyResult = safetyResult,
                onDismiss = onDismiss,
                onProceed = onProceed,
                onCancel = onCancel
            )
        } else {
            onProceed()
        }
    }
}
