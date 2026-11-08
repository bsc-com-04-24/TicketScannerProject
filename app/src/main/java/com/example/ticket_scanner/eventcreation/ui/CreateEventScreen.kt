package com.example.ticket_scanner.eventcreation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ticket_scanner.eventcreation.model.EventForm
import com.example.ticket_scanner.eventcreation.model.FormErrors
import com.example.ticket_scanner.ui.theme.BluePrimary
import com.example.ticket_scanner.ui.theme.LightBackground
import com.example.ticket_scanner.ui.theme.TextMuted

@Composable
fun CreateEventScreen(
    form: EventForm,
    showErrors: Boolean,
    onFormChange: (EventForm) -> Unit,
    onCreateTickets: () -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {}
) {
    val errors = if (showErrors) form.errors() else FormErrors()

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
            .imePadding()
    ) {
        EventHeader(title = "Create Event")

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Enter the event details, then create the tickets.",
                color = TextMuted,
                fontSize = 14.sp
            )

            OutlinedTextField(
                value = form.name,
                onValueChange = { onFormChange(form.copy(name = it)) },
                label = { Text("Event name") },
                singleLine = true,
                isError = errors.name != null,
                supportingText = errors.name?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = form.description,
                onValueChange = { onFormChange(form.copy(description = it)) },
                label = { Text("Event details (optional)") },
                minLines = 3,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = form.venue,
                onValueChange = { onFormChange(form.copy(venue = it)) },
                label = { Text("Venue") },
                singleLine = true,
                isError = errors.venue != null,
                supportingText = errors.venue?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth()
            )

            PickerField(
                label = "Date",
                value = form.dateUtcMillis?.let { formatPickedDate(it) } ?: "",
                error = errors.date,
                isTime = false,
                onClick = { showDatePicker = true }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PickerField(
                    label = "Start time",
                    value = form.startMinutes?.let { formatMinutes(it) } ?: "",
                    error = errors.startTime,
                    isTime = true,
                    onClick = { showStartPicker = true },
                    modifier = Modifier.weight(1f)
                )
                PickerField(
                    label = "End time",
                    value = form.endMinutes?.let { formatMinutes(it) } ?: "",
                    error = errors.endTime,
                    isTime = true,
                    onClick = { showEndPicker = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = onCreateTickets,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Create tickets",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        bottomBar()
    }

    if (showDatePicker) {
        EventDatePickerDialog(
            initialMillis = form.dateUtcMillis,
            onConfirm = { onFormChange(form.copy(dateUtcMillis = it)) },
            onDismiss = { showDatePicker = false }
        )
    }
    if (showStartPicker) {
        EventTimePickerDialog(
            title = "Start time",
            initialMinutes = form.startMinutes,
            onConfirm = { onFormChange(form.copy(startMinutes = it)) },
            onDismiss = { showStartPicker = false }
        )
    }
    if (showEndPicker) {
        EventTimePickerDialog(
            title = "End time",
            initialMinutes = form.endMinutes,
            onConfirm = { onFormChange(form.copy(endMinutes = it)) },
            onDismiss = { showEndPicker = false }
        )
    }
}

/** A read-only text field that opens a picker when tapped. */
@Composable
private fun PickerField(
    label: String,
    value: String,
    error: String?,
    isTime: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            trailingIcon = {
                Icon(
                    imageVector = if (isTime) Icons.Default.Schedule else Icons.Default.DateRange,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        // Transparent layer on top so a tap anywhere on the field opens the picker
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onClick)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventDatePickerDialog(
    initialMillis: Long?,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let(onConfirm)
                    onDismiss()
                }
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventTimePickerDialog(
    title: String,
    initialMinutes: Int?,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val initial = initialMinutes ?: (9 * 60)
    val state = rememberTimePickerState(
        initialHour = initial / 60,
        initialMinute = initial % 60,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(state.hour * 60 + state.minute)
                    onDismiss()
                }
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun PreviewCreateEvent() = MaterialTheme {
    CreateEventScreen(
        form = EventForm(name = "Malawi Tech & Innovation Expo", venue = "Bingu International Convention Centre"),
        showErrors = true,
        onFormChange = {},
        onCreateTickets = {}
    )
}