package com.example.ticket_scanner.events

import androidx.compose.ui.graphics.Color

/** A static sample event for the Events tab. */
data class SampleEvent(
    val name: String,
    val venue: String,
    val date: String,
    val emoji: String,
    val colorStart: Color,
    val colorEnd: Color
)

/** Five sample events. Replace with real events later (for example from GET /api/events). */
object SampleEvents {
    val all = listOf(
        SampleEvent(
            name = "Malawi Tech & Innovation Expo",
            venue = "Bingu International Convention Centre, Lilongwe",
            date = "Sat, 14 Nov 2026 · 9:00 AM",
            emoji = "💻",
            colorStart = Color(0xFF3A3FB5),
            colorEnd = Color(0xFF1B7FD4)
        ),
        SampleEvent(
            name = "Malawi Music Concert",
            venue = "Bingu International Convention Centre, Lilongwe",
            date = "Sat, 5 Dec 2026 · 7:00 PM",
            emoji = "🎵",
            colorStart = Color(0xFF7B2FBE),
            colorEnd = Color(0xFFE0407B)
        ),
        SampleEvent(
            name = "Flames vs Zambia",
            venue = "Bingu National Stadium, Lilongwe",
            date = "Sun, 13 Dec 2026 · 3:00 PM",
            emoji = "⚽",
            colorStart = Color(0xFF1B9E4B),
            colorEnd = Color(0xFF0E5E2D)
        ),
        SampleEvent(
            name = "Comedy Night Live",
            venue = "Bingu International Convention Centre, Lilongwe",
            date = "Fri, 18 Dec 2026 · 6:00 PM",
            emoji = "🎤",
            colorStart = Color(0xFFE59A0B),
            colorEnd = Color(0xFFD1481B)
        ),
        SampleEvent(
            name = "Mulanje Charity Run",
            venue = "Likhubula, Mulanje",
            date = "Sat, 9 Jan 2027 · 6:30 AM",
            emoji = "🏃",
            colorStart = Color(0xFF0E7490),
            colorEnd = Color(0xFF164E63)
        )
    )
}