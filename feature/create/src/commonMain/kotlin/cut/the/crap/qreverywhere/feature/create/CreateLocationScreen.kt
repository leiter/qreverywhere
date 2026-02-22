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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cut.the.crap.qreverywhere.shared.domain.model.AcquireType
import cut.the.crap.qreverywhere.shared.domain.model.GeoLocation
import org.jetbrains.compose.resources.stringResource
import qreverywhere.shared.generated.resources.Res
import qreverywhere.shared.generated.resources.*

@Composable
fun CreateLocationScreen(
    viewModel: CreateViewModel,
    onQrCreated: () -> Unit = {}
) {
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }
    var latitudeError by remember { mutableStateOf<String?>(null) }
    var longitudeError by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    val errorEmptyLatitude = stringResource(Res.string.error_empty_latitude)
    val errorEmptyLongitude = stringResource(Res.string.error_empty_longitude)
    val errorInvalidLatitude = stringResource(Res.string.error_invalid_latitude)
    val errorInvalidLongitude = stringResource(Res.string.error_invalid_longitude)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.instruction_location),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = latitude,
            onValueChange = { latitude = it; latitudeError = null },
            label = { Text(stringResource(Res.string.label_latitude)) },
            placeholder = { Text(stringResource(Res.string.placeholder_latitude)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = latitudeError != null,
            supportingText = latitudeError?.let { { Text(it) } }
                ?: { Text(stringResource(Res.string.hint_latitude)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = longitude,
            onValueChange = { longitude = it; longitudeError = null },
            label = { Text(stringResource(Res.string.label_longitude)) },
            placeholder = { Text(stringResource(Res.string.placeholder_longitude)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = longitudeError != null,
            supportingText = longitudeError?.let { { Text(it) } }
                ?: { Text(stringResource(Res.string.hint_longitude)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text(stringResource(Res.string.label_location_label)) },
            placeholder = { Text(stringResource(Res.string.placeholder_location_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )

        Button(
            onClick = {
                if (latitude.isBlank()) { latitudeError = errorEmptyLatitude; return@Button }
                if (longitude.isBlank()) { longitudeError = errorEmptyLongitude; return@Button }

                val lat = latitude.toDoubleOrNull()
                val lon = longitude.toDoubleOrNull()

                if (lat == null || lat !in -90.0..90.0) { latitudeError = errorInvalidLatitude; return@Button }
                if (lon == null || lon !in -180.0..180.0) { longitudeError = errorInvalidLongitude; return@Button }

                isCreating = true
                val geoLocation = GeoLocation(latitude = lat, longitude = lon, label = label.ifBlank { null })

                viewModel.createQrItem(geoLocation.toGeoUri(), AcquireType.CREATED) { result ->
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
