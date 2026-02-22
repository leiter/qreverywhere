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

private enum class CryptoType { BITCOIN, ETHEREUM }

@Composable
fun CreateCryptoScreen(
    viewModel: CreateViewModel,
    onQrCreated: () -> Unit = {}
) {
    var address by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedCrypto by remember { mutableStateOf(CryptoType.BITCOIN) }
    var isCreating by remember { mutableStateOf(false) }
    var addressError by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    val errorEmptyAddress = stringResource(Res.string.error_empty_crypto_address)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.instruction_crypto),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = stringResource(Res.string.label_select_crypto),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        )

        CryptoType.entries.forEach { crypto ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = (selectedCrypto == crypto), onClick = { selectedCrypto = crypto }, role = Role.RadioButton)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = (selectedCrypto == crypto), onClick = null)
                Text(
                    text = when (crypto) {
                        CryptoType.BITCOIN -> stringResource(Res.string.crypto_bitcoin)
                        CryptoType.ETHEREUM -> stringResource(Res.string.crypto_ethereum)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        OutlinedTextField(
            value = address,
            onValueChange = { address = it; addressError = null },
            label = { Text(stringResource(Res.string.label_crypto_address)) },
            placeholder = {
                Text(when (selectedCrypto) {
                    CryptoType.BITCOIN -> stringResource(Res.string.placeholder_bitcoin_address)
                    CryptoType.ETHEREUM -> stringResource(Res.string.placeholder_ethereum_address)
                })
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = addressError != null,
            supportingText = addressError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = {
                Text(when (selectedCrypto) {
                    CryptoType.BITCOIN -> stringResource(Res.string.label_crypto_amount_btc)
                    CryptoType.ETHEREUM -> stringResource(Res.string.label_crypto_amount_eth)
                })
            },
            placeholder = { Text(stringResource(Res.string.placeholder_crypto_amount)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
        )

        if (selectedCrypto == CryptoType.BITCOIN) {
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text(stringResource(Res.string.label_crypto_label)) },
                placeholder = { Text(stringResource(Res.string.placeholder_crypto_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text(stringResource(Res.string.label_crypto_message)) },
                placeholder = { Text(stringResource(Res.string.placeholder_crypto_message)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )
        }

        Button(
            onClick = {
                if (address.isBlank()) { addressError = errorEmptyAddress; return@Button }

                isCreating = true
                val cryptoUri = when (selectedCrypto) {
                    CryptoType.BITCOIN -> {
                        val params = mutableListOf<String>()
                        if (amount.isNotBlank()) params.add("amount=${amount.trim()}")
                        if (label.isNotBlank()) params.add("label=${label.trim()}")
                        if (message.isNotBlank()) params.add("message=${message.trim()}")
                        val base = "bitcoin:${address.trim()}"
                        if (params.isNotEmpty()) "$base?${params.joinToString("&")}" else base
                    }
                    CryptoType.ETHEREUM -> {
                        val base = "ethereum:${address.trim()}"
                        if (amount.isNotBlank()) "$base?value=${amount.trim()}" else base
                    }
                }

                viewModel.createQrItem(cryptoUri, AcquireType.CREATED) { result ->
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
