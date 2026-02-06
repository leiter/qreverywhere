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
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
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
                text = when (safetyResult.status) {
                    SafetyStatus.DANGEROUS -> "Dangerous Link Detected"
                    SafetyStatus.WARNING -> "Suspicious Link"
                    SafetyStatus.SAFE -> "URL Check"
                },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Show the URL (truncated if too long)
                Text(
                    text = "URL: ${safetyResult.originalUrl.take(50)}${if (safetyResult.originalUrl.length > 50) "..." else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Show warnings
                if (safetyResult.warnings.isNotEmpty()) {
                    Text(
                        text = "Warnings:",
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
                    text = when (safetyResult.status) {
                        SafetyStatus.DANGEROUS -> "Opening this link is not recommended as it may harm your device or compromise your data."
                        SafetyStatus.WARNING -> "This link has some characteristics that may indicate a security risk. Proceed with caution."
                        SafetyStatus.SAFE -> "No issues were detected with this URL."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            if (safetyResult.status != SafetyStatus.DANGEROUS) {
                TextButton(onClick = onProceed) {
                    Text("Open Anyway")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(
                    text = if (safetyResult.status == SafetyStatus.DANGEROUS) "Close" else "Cancel"
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
            // If safe, proceed automatically
            onProceed()
        }
    }
}
