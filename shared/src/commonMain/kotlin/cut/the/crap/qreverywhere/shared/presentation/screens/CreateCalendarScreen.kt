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
import cut.the.crap.qreverywhere.shared.domain.model.CalendarEvent
import cut.the.crap.qreverywhere.shared.presentation.viewmodel.MainViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import qreverywhere.shared.generated.resources.Res
import qreverywhere.shared.generated.resources.*

/**
 * Screen for creating Calendar Event QR codes (VEVENT format)
 * Output format: BEGIN:VCALENDAR...END:VCALENDAR
 */
@Composable
fun CreateCalendarScreen(
    viewModel: MainViewModel,
    onQrCreated: () -> Unit = {}
) {
    var eventTitle by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isAllDay by remember { mutableStateOf(false) }
    var isCreating by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }

    // Date/time fields as strings for simplicity
    var startDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    // Get localized strings
    val errorEmptyTitle = stringResource(Res.string.error_empty_event_title)
    val errorInvalidDate = stringResource(Res.string.error_invalid_date)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.instruction_calendar),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Event Title Field
        OutlinedTextField(
            value = eventTitle,
            onValueChange = {
                eventTitle = it
                titleError = null
            },
            label = { Text(stringResource(Res.string.label_event_title)) },
            placeholder = { Text(stringResource(Res.string.placeholder_event_title)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = titleError != null,
            supportingText = titleError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        // All Day Checkbox
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isAllDay,
                onCheckedChange = { isAllDay = it }
            )
            Text(
                text = stringResource(Res.string.label_all_day_event),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Start Date Field
        OutlinedTextField(
            value = startDate,
            onValueChange = {
                startDate = it
                dateError = null
            },
            label = { Text(stringResource(Res.string.label_start_date)) },
            placeholder = { Text(stringResource(Res.string.placeholder_date)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = dateError != null,
            supportingText = { Text(stringResource(Res.string.hint_date_format)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )

        // Start Time Field (hidden for all-day events)
        if (!isAllDay) {
            OutlinedTextField(
                value = startTime,
                onValueChange = { startTime = it },
                label = { Text(stringResource(Res.string.label_start_time)) },
                placeholder = { Text(stringResource(Res.string.placeholder_time)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { Text(stringResource(Res.string.hint_time_format)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )
        }

        // End Date Field
        OutlinedTextField(
            value = endDate,
            onValueChange = { endDate = it },
            label = { Text(stringResource(Res.string.label_end_date)) },
            placeholder = { Text(stringResource(Res.string.placeholder_date)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )

        // End Time Field (hidden for all-day events)
        if (!isAllDay) {
            OutlinedTextField(
                value = endTime,
                onValueChange = { endTime = it },
                label = { Text(stringResource(Res.string.label_end_time)) },
                placeholder = { Text(stringResource(Res.string.placeholder_time)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )
        }

        // Location Field (Optional)
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text(stringResource(Res.string.label_event_location)) },
            placeholder = { Text(stringResource(Res.string.placeholder_event_location)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        // Description Field (Optional)
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(stringResource(Res.string.label_event_description)) },
            placeholder = { Text(stringResource(Res.string.placeholder_event_description)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
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
                // Validate title
                if (eventTitle.isBlank()) {
                    titleError = errorEmptyTitle
                    return@Button
                }

                // Parse dates
                val startInstant = parseDateTime(startDate, if (isAllDay) "00:00" else startTime)
                val endInstant = parseDateTime(
                    endDate.ifBlank { startDate },
                    if (isAllDay) "23:59" else endTime.ifBlank { startTime }
                )

                if (startInstant == null) {
                    dateError = errorInvalidDate
                    return@Button
                }

                isCreating = true
                try {
                    val calendarEvent = CalendarEvent(
                        title = eventTitle,
                        startDateTime = startInstant,
                        endDateTime = endInstant ?: startInstant,
                        location = location.ifBlank { null },
                        description = description.ifBlank { null },
                        isAllDay = isAllDay
                    )

                    val vEventText = calendarEvent.toVEvent()

                    // Create QR code using ViewModel
                    viewModel.saveQrItemFromText(vEventText, AcquireType.CREATED)

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
 * Parse date and time strings into Instant
 * Date format: YYYY-MM-DD or YYYYMMDD
 * Time format: HH:MM or HHMM
 */
private fun parseDateTime(dateStr: String, timeStr: String): Instant? {
    return try {
        val cleanDate = dateStr.replace("-", "").replace("/", "")
        if (cleanDate.length < 8) return null

        val year = cleanDate.substring(0, 4).toInt()
        val month = cleanDate.substring(4, 6).toInt()
        val day = cleanDate.substring(6, 8).toInt()

        val cleanTime = timeStr.replace(":", "")
        val hour = if (cleanTime.length >= 2) cleanTime.substring(0, 2).toIntOrNull() ?: 0 else 0
        val minute = if (cleanTime.length >= 4) cleanTime.substring(2, 4).toIntOrNull() ?: 0 else 0

        val localDateTime = LocalDateTime(year, month, day, hour, minute, 0)
        localDateTime.toInstant(TimeZone.currentSystemDefault())
    } catch (e: Exception) {
        null
    }
}
