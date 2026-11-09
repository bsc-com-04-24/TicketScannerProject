package com.example.ticket_scanner.eventcreation.ui

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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