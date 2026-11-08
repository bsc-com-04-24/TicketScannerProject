package com.example.ticket_scanner.eventcreation.data

import com.example.ticket_scanner.eventcreation.model.EventDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Keeps the events created in this session (in memory).
 *
 * Later, replace add() with a call to POST /api/events (and read the list back
 * from GET /api/organizer/events). Other screens, such as Home, can already
 * observe [events].
 */
object EventRepository {

    private val _events = MutableStateFlow<List<EventDetails>>(emptyList())
    val events: StateFlow<List<EventDetails>> = _events.asStateFlow()

    fun add(event: EventDetails) {
        _events.value = listOf(event) + _events.value
    }
}