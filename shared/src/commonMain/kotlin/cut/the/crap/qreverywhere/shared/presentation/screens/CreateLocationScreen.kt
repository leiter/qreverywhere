package cut.the.crap.qreverywhere.shared.presentation.screens

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
import cut.the.crap.qreverywhere.shared.presentation.viewmodel.MainViewModel
import org.jetbrains.compose.resources.stringResource
import qreverywhere.shared.generated.resources.Res
import qreverywhere.shared.generated.resources.*

/**
 * Screen for creating Location QR codes (geo: URI format)
 * Output format: geo:latitude,longitude?q=label
 */
@Composable
fun CreateLocationScreen(
    viewModel: MainViewModel,
    onQrCreated: () -> Unit = {}
) {
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }
    var latitudeError by remember { mutableStateOf<String?>(null) }
    var longitudeError by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    // Get localized strings
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

        // Latitude Field
        OutlinedTextField(
            value = latitude,
            onValueChange = {
                latitude = it
                latitudeError = null
            },
            label = { Text(stringResource(Res.string.label_latitude)) },
            placeholder = { Text(stringResource(Res.string.placeholder_latitude)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = latitudeError != null,
            supportingText = latitudeError?.let { { Text(it) } }
                ?: { Text(stringResource(Res.string.hint_latitude)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            )
        )

        // Longitude Field
        OutlinedTextField(
            value = longitude,
            onValueChange = {
                longitude = it
                longitudeError = null
            },
            label = { Text(stringResource(Res.string.label_longitude)) },
            placeholder = { Text(stringResource(Res.string.placeholder_longitude)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = longitudeError != null,
            supportingText = longitudeError?.let { { Text(it) } }
                ?: { Text(stringResource(Res.string.hint_longitude)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            )
        )

        // Label Field (Optional)
        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text(stringResource(Res.string.label_location_label)) },
            placeholder = { Text(stringResource(Res.string.placeholder_location_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )

        Button(
            onClick = {
                // Validate latitude
                if (latitude.isBlank()) {
                    latitudeError = errorEmptyLatitude
                    return@Button
                }

                // Validate longitude
                if (longitude.isBlank()) {
                    longitudeError = errorEmptyLongitude
                    return@Button
                }

                // Parse and validate coordinates
                val lat = latitude.toDoubleOrNull()
                val lon = longitude.toDoubleOrNull()

                if (lat == null || lat !in -90.0..90.0) {
                    latitudeError = errorInvalidLatitude
                    return@Button
                }

                if (lon == null || lon !in -180.0..180.0) {
                    longitudeError = errorInvalidLongitude
                    return@Button
                }

                isCreating = true
                try {
                    val geoLocation = GeoLocation(
                        latitude = lat,
                        longitude = lon,
                        label = label.ifBlank { null }
                    )

                    val geoUri = geoLocation.toGeoUri()

                    // Create QR code using ViewModel
                    viewModel.saveQrItemFromText(geoUri, AcquireType.CREATED)

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
