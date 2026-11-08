package com.example.ticket_scanner.eventcreation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ticket_scanner.ui.theme.BlueHeaderBackground
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Blue title bar, matching the other screens. */
@Composable
internal fun EventHeader(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BlueHeaderBackground)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
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