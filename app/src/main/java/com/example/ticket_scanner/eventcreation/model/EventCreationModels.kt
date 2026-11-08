package com.example.ticket_scanner.eventcreation.model

import java.util.Calendar
import java.util.TimeZone

/** A finished event, ready to be stored (and later sent to POST /api/events). */
data class EventDetails(
    val name: String,
    val description: String,
    val venue: String,
    val startMillis: Long,
    val endMillis: Long
)

/** One generated ticket and the text its QR code carries. */
data class GeneratedTicket(
    val code: String,
    val ticketType: String,
    val eventName: String,
    val venue: String,
    val startMillis: Long,
    val ticketNumber: Int,
    val totalTickets: Int,
    val qrPayload: String
)

/** Error messages for the form fields. A null message means the field is fine. */
data class FormErrors(
    val name: String? = null,
    val venue: String? = null,
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null
) {
    val isValid: Boolean
        get() = name == null && venue == null && date == null &&
                startTime == null && endTime == null
}

/** What the person has typed or picked so far on the Create Event screen. */
data class EventForm(
    val name: String = "",
    val description: String = "",
    val venue: String = "",
    val dateUtcMillis: Long? = null,   // the date picker returns midnight UTC
    val startMinutes: Int? = null,     // minutes since midnight, e.g. 9:30 = 570
    val endMinutes: Int? = null
) {
    fun errors(): FormErrors {
        val start = startMinutes
        val end = endMinutes
        return FormErrors(
            name = if (name.isBlank()) "Enter the event name" else null,
            venue = if (venue.isBlank()) "Enter the venue" else null,
            date = if (dateUtcMillis == null) "Pick the event date" else null,
            startTime = if (start == null) "Pick the start time" else null,
            endTime = when {
                end == null -> "Pick the end time"
                start != null && end <= start -> "End time must be after the start time"
                else -> null
            }
        )
    }

    /** Only call this when [errors] is valid. */
    fun toEventDetails(): EventDetails {
        val date = requireNotNull(dateUtcMillis)
        return EventDetails(
            name = name.trim(),
            description = description.trim(),
            venue = venue.trim(),
            startMillis = combine(date, requireNotNull(startMinutes)),
            endMillis = combine(date, requireNotNull(endMinutes))
        )
    }
}

/** Joins the picked date (UTC midnight) with a time of day in the phone's time zone. */
private fun combine(dateUtcMillis: Long, minutes: Int): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = dateUtcMillis
    }
    return Calendar.getInstance().apply {
        clear()
        set(
            utc.get(Calendar.YEAR),
            utc.get(Calendar.MONTH),
            utc.get(Calendar.DAY_OF_MONTH),
            minutes / 60,
            minutes % 60
        )
    }.timeInMillis
}