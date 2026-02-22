package cut.the.crap.qreverywhere.feature.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cut.the.crap.qreverywhere.shared.domain.model.AcquireType
import org.jetbrains.compose.resources.stringResource
import qreverywhere.shared.generated.resources.Res
import qreverywhere.shared.generated.resources.*

private enum class PaymentProvider { PAYPAL, VENMO }

@Composable
fun CreatePaymentScreen(
    viewModel: CreateViewModel,
    onQrCreated: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf(PaymentProvider.PAYPAL) }
    var isCreating by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    val errorEmptyUsername = stringResource(Res.string.error_empty_payment_username)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.instruction_payment),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = stringResource(Res.string.label_select_provider),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        )

        PaymentProvider.entries.forEach { provider ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = (selectedProvider == provider), onClick = { selectedProvider = provider }, role = Role.RadioButton)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = (selectedProvider == provider), onClick = null)
                Text(
                    text = when (provider) {
                        PaymentProvider.PAYPAL -> stringResource(Res.string.provider_paypal)
                        PaymentProvider.VENMO -> stringResource(Res.string.provider_venmo)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        OutlinedTextField(
            value = username,
            onValueChange = { username = it; usernameError = null },
            label = { Text(stringResource(Res.string.label_payment_username)) },
            placeholder = {
                Text(when (selectedProvider) {
                    PaymentProvider.PAYPAL -> stringResource(Res.string.placeholder_paypal_username)
                    PaymentProvider.VENMO -> stringResource(Res.string.placeholder_venmo_username)
                })
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = usernameError != null,
            supportingText = usernameError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text(stringResource(Res.string.label_payment_amount)) },
            placeholder = { Text(stringResource(Res.string.placeholder_payment_amount)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
        )

        if (selectedProvider == PaymentProvider.VENMO) {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(Res.string.label_payment_note)) },
                placeholder = { Text(stringResource(Res.string.placeholder_payment_note)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )
        }

        Button(
            onClick = {
                if (username.isBlank()) { usernameError = errorEmptyUsername; return@Button }

                isCreating = true
                val paymentUrl = when (selectedProvider) {
                    PaymentProvider.PAYPAL -> {
                        val base = "https://paypal.me/${username.trim()}"
                        if (amount.isNotBlank()) "$base/${amount.trim()}" else base
                    }
                    PaymentProvider.VENMO -> {
                        val base = "https://venmo.com/${username.trim()}"
                        val params = mutableListOf("txn=pay")
                        if (amount.isNotBlank()) params.add("amount=${amount.trim()}")
                        if (note.isNotBlank()) params.add("note=${note.trim()}")
                        "$base?${params.joinToString("&")}"
                    }
                }

                viewModel.createQrItem(paymentUrl, AcquireType.CREATED) { result ->
                    isCreating = false
                    result.onSuccess { onQrCreated() }
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
