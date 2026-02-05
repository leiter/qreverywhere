package cut.the.crap.qreverywhere.shared.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cut.the.crap.qreverywhere.shared.domain.model.AcquireType
import cut.the.crap.qreverywhere.shared.presentation.viewmodel.MainViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import qreverywhere.shared.generated.resources.Res
import qreverywhere.shared.generated.resources.*

/**
 * WiFi security type options
 */
enum class WiFiSecurityType(val code: String) {
    WPA("WPA"),
    WEP("WEP"),
    OPEN("nopass")
}

/**
 * Screen for creating QR codes with WiFi network credentials
 * Output format: WIFI:T:WPA;S:NetworkName;P:Password;H:true;;
 */
@Composable
fun CreateWiFiScreen(
    viewModel: MainViewModel,
    onQrCreated: () -> Unit = {}
) {
    var networkName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var securityType by remember { mutableStateOf(WiFiSecurityType.WPA) }
    var isHidden by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isCreating by remember { mutableStateOf(false) }
    var ssidError by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    // Get localized strings
    val errorEmptySsid = stringResource(Res.string.error_empty_ssid)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.instruction_wifi),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Network Name (SSID) Field
        OutlinedTextField(
            value = networkName,
            onValueChange = {
                networkName = it
                ssidError = null
            },
            label = { Text(stringResource(Res.string.label_network_name)) },
            placeholder = { Text(stringResource(Res.string.placeholder_network_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = ssidError != null,
            supportingText = ssidError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        // Security Type Selection
        Text(
            text = stringResource(Res.string.label_security_type),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = securityType == WiFiSecurityType.WPA,
                onClick = { securityType = WiFiSecurityType.WPA },
                label = { Text(stringResource(Res.string.security_wpa)) }
            )
            FilterChip(
                selected = securityType == WiFiSecurityType.WEP,
                onClick = { securityType = WiFiSecurityType.WEP },
                label = { Text(stringResource(Res.string.security_wep)) }
            )
            FilterChip(
                selected = securityType == WiFiSecurityType.OPEN,
                onClick = { securityType = WiFiSecurityType.OPEN },
                label = { Text(stringResource(Res.string.security_open)) }
            )
        }

        // Password Field (hidden for open networks)
        if (securityType != WiFiSecurityType.OPEN) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(Res.string.label_password)) },
                placeholder = { Text(stringResource(Res.string.placeholder_password)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                if (isPasswordVisible) Res.drawable.visibility_off
                                else Res.drawable.visibility
                            ),
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )
        }

        // Hidden Network Checkbox
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isHidden,
                onCheckedChange = { isHidden = it }
            )
            Text(
                text = stringResource(Res.string.label_hidden_network),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Button(
            onClick = {
                // Validate SSID
                if (networkName.isBlank()) {
                    ssidError = errorEmptySsid
                    return@Button
                }

                isCreating = true
                try {
                    // Create WiFi QR code string
                    // Format: WIFI:T:WPA;S:NetworkName;P:Password;H:true;;
                    val wifiText = buildString {
                        append("WIFI:")
                        append("T:${securityType.code};")
                        append("S:${escapeWiFiSpecialChars(networkName)};")
                        if (securityType != WiFiSecurityType.OPEN && password.isNotEmpty()) {
                            append("P:${escapeWiFiSpecialChars(password)};")
                        }
                        if (isHidden) {
                            append("H:true;")
                        }
                        append(";")
                    }

                    // Create QR code using ViewModel
                    viewModel.saveQrItemFromText(wifiText, AcquireType.CREATED)

                    onQrCreated()
                } finally {
                    isCreating = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isCreating
        ) {
            Text(
                if (isCreating) stringResource(Res.string.create_button_creating)
                else stringResource(Res.string.create_button)
            )
        }
    }
}

/**
 * Escape special characters for WiFi QR code format
 * Characters ;:,"\ must be escaped with backslash
 */
private fun escapeWiFiSpecialChars(text: String): String {
    return text
        .replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(":", "\\:")
        .replace(",", "\\,")
        .replace("\"", "\\\"")
}
