package com.example.ticket_scanner.eventcreation.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ticket_scanner.eventcreation.model.EventForm
import com.example.ticket_scanner.eventcreation.model.FormErrors
import com.example.ticket_scanner.ui.theme.BlueHeaderBackground
import com.example.ticket_scanner.ui.theme.BluePrimary
import com.example.ticket_scanner.ui.theme.TextDark
import com.example.ticket_scanner.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Blue title bar, matching the other screens. Pass [onBack] to show a back arrow. */
@Composable
internal fun EventHeader(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BlueHeaderBackground)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        } else {
            Spacer(Modifier.width(12.dp))
        }
        Text(
            text = title,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/** A white card with a ticket's QR code, its code and a few details. */
@Composable
internal fun QrTicketCard(
    qrPayload: String,
    code: String,
    subtitle: String,
    eventName: String,
    status: String? = null,
    footer: @Composable () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QrCodeImage(content = qrPayload, modifier = Modifier.size(220.dp))
            Spacer(Modifier.height(12.dp))
            Text(code, color = TextDark, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = TextMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
            Text(eventName, color = TextMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
            if (status != null) {
                Spacer(Modifier.height(6.dp))
                Text(status, color = BluePrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            footer()
        }
    }
}

@Composable
internal fun EventFormFields(
    form: EventForm,
    errors: FormErrors,
    onFormChange: (EventForm) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = form.name,
            onValueChange = { onFormChange(form.copy(name = it)) },
            label = { Text("Event Name *") },
            isError = errors.name != null,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        if (errors.name != null) {
            Text(errors.name, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        OutlinedTextField(
            value = form.venue,
            onValueChange = { onFormChange(form.copy(venue = it)) },
            label = { Text("Venue *") },
            isError = errors.venue != null,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        if (errors.venue != null) {
            Text(errors.venue, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        OutlinedTextField(
            value = form.description,
            onValueChange = { onFormChange(form.copy(description = it)) },
            label = { Text("Description (Optional)") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        // Date Picker Button
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (form.dateUtcMillis != null) "Date: ${formatPickedDate(form.dateUtcMillis)}" else "Date: Not picked",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            OutlinedButton(
                onClick = {
                    val initialCalendar = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    if (form.dateUtcMillis != null) {
                        initialCalendar.timeInMillis = form.dateUtcMillis
                    }
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val picked = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                clear()
                                set(year, month, dayOfMonth)
                            }
                            onFormChange(form.copy(dateUtcMillis = picked.timeInMillis, startMinutes = 540, endMinutes = 1080))
                        },
                        initialCalendar.get(java.util.Calendar.YEAR),
                        initialCalendar.get(java.util.Calendar.MONTH),
                        initialCalendar.get(java.util.Calendar.DAY_OF_MONTH)
                    ).show()
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (form.dateUtcMillis != null) "Change Event Date" else "Pick Event Date")
            }
            if (errors.date != null) {
                Text(errors.date, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
        }

        // Time Selectors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Start: ${form.startMinutes?.let { formatMinutes(it) } ?: "Not set"}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                OutlinedButton(
                    onClick = { onFormChange(form.copy(startMinutes = 540)) }, // 9:00 AM
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("9:00 AM")
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "End: ${form.endMinutes?.let { formatMinutes(it) } ?: "Not set"}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                OutlinedButton(
                    onClick = { onFormChange(form.copy(endMinutes = 1080)) }, // 6:00 PM
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("6:00 PM")
                }
            }
        }
    }
}

/** The date picker returns midnight UTC, so it must be formatted in UTC. */
internal fun formatPickedDate(dateUtcMillis: Long): String =
    SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(Date(dateUtcMillis))

/** 570 becomes "9:30 AM". */
internal fun formatMinutes(minutes: Int): String {
    val hour24 = minutes / 60
    val minute = minutes % 60
    val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
    val suffix = if (hour24 < 12) "AM" else "PM"
    return String.format(Locale.US, "%d:%02d %s", hour12, minute, suffix)
}

internal fun formatDateTime(millis: Long): String =
    SimpleDateFormat("EEE, d MMM yyyy · h:mm a", Locale.getDefault()).format(Date(millis))
