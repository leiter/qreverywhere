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

private enum class AppStore { APPLE, GOOGLE }

@Composable
fun CreateAppStoreLinkScreen(
    viewModel: CreateViewModel,
    onQrCreated: () -> Unit = {}
) {
    var appId by remember { mutableStateOf("") }
    var selectedStore by remember { mutableStateOf(AppStore.APPLE) }
    var isCreating by remember { mutableStateOf(false) }
    var appIdError by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    val errorEmptyAppId = stringResource(Res.string.error_empty_app_id)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.instruction_app_store),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = stringResource(Res.string.label_select_store),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        )

        AppStore.entries.forEach { store ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = (selectedStore == store), onClick = { selectedStore = store }, role = Role.RadioButton)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = (selectedStore == store), onClick = null)
                Text(
                    text = when (store) {
                        AppStore.APPLE -> stringResource(Res.string.store_apple)
                        AppStore.GOOGLE -> stringResource(Res.string.store_google)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        OutlinedTextField(
            value = appId,
            onValueChange = { appId = it; appIdError = null },
            label = {
                Text(when (selectedStore) {
                    AppStore.APPLE -> stringResource(Res.string.label_apple_app_id)
                    AppStore.GOOGLE -> stringResource(Res.string.label_google_package_name)
                })
            },
            placeholder = {
                Text(when (selectedStore) {
                    AppStore.APPLE -> stringResource(Res.string.placeholder_apple_app_id)
                    AppStore.GOOGLE -> stringResource(Res.string.placeholder_google_package_name)
                })
            },
            supportingText = {
                Text(when (selectedStore) {
                    AppStore.APPLE -> stringResource(Res.string.hint_apple_app_id)
                    AppStore.GOOGLE -> stringResource(Res.string.hint_google_package_name)
                })
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = appIdError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )

        Button(
            onClick = {
                if (appId.isBlank()) { appIdError = errorEmptyAppId; return@Button }

                isCreating = true
                val storeUrl = when (selectedStore) {
                    AppStore.APPLE -> "https://apps.apple.com/app/id${appId.trim()}"
                    AppStore.GOOGLE -> "https://play.google.com/store/apps/details?id=${appId.trim()}"
                }

                viewModel.createQrItem(storeUrl, AcquireType.CREATED) { result ->
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
