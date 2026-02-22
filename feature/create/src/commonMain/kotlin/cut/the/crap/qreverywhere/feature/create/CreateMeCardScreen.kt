package cut.the.crap.qreverywhere.feature.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cut.the.crap.qreverywhere.shared.domain.model.AcquireType
import cut.the.crap.qreverywhere.shared.domain.model.MeCard
import org.jetbrains.compose.resources.stringResource
import qreverywhere.shared.generated.resources.Res
import qreverywhere.shared.generated.resources.*

@Composable
fun CreateMeCardScreen(
    viewModel: CreateViewModel,
    onQrCreated: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var organization by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current
    val errorEmptyName = stringResource(Res.string.error_empty_mecard_name)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.instruction_mecard),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it; nameError = null },
            label = { Text(stringResource(Res.string.label_mecard_name)) },
            placeholder = { Text(stringResource(Res.string.placeholder_mecard_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = nameError != null,
            supportingText = nameError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(stringResource(Res.string.label_phone_number)) },
            placeholder = { Text(stringResource(Res.string.placeholder_phone)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(Res.string.label_email_address)) },
            placeholder = { Text(stringResource(Res.string.placeholder_email)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = organization,
            onValueChange = { organization = it },
            label = { Text(stringResource(Res.string.label_organization)) },
            placeholder = { Text(stringResource(Res.string.placeholder_organization)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text(stringResource(Res.string.label_mecard_address)) },
            placeholder = { Text(stringResource(Res.string.placeholder_mecard_address)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text(stringResource(Res.string.label_website)) },
            placeholder = { Text(stringResource(Res.string.placeholder_website)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text(stringResource(Res.string.label_mecard_note)) },
            placeholder = { Text(stringResource(Res.string.placeholder_mecard_note)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )

        Button(
            onClick = {
                if (name.isBlank()) { nameError = errorEmptyName; return@Button }

                isCreating = true
                val meCard = MeCard(
                    name = name,
                    phone = phone.ifBlank { null },
                    email = email.ifBlank { null },
                    address = address.ifBlank { null },
                    organization = organization.ifBlank { null },
                    note = note.ifBlank { null },
                    url = url.ifBlank { null }
                )

                viewModel.createQrItem(meCard.toMeCard(), AcquireType.CREATED) { result ->
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
