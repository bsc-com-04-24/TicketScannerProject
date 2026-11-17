package com.example.ticket_scanner.eventcreation.localdb

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.ticket_scanner.eventcreation.domain.TicketGenerator
import com.example.ticket_scanner.eventcreation.model.EventDetails
import com.example.ticket_scanner.eventcreation.model.GeneratedTicket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * THE ONE PLACE tickets are stored on the phone. Shared by:
 *  - event creation (saves, edits and deletes events and their tickets)
 *  - ticket validation (looks tickets up and marks them used)
 *  - ticket management (lists, views and manages them)
 *
 * Every change is written to the database first, then [tickets] is reloaded,
 * so every screen that observes it updates by itself.
 *
 * Call [init] once, at app start (in MainActivity.onCreate).
 * Database work runs off the main thread.
 */
object LocalTicketStore {

    private var helper: TicketDbHelper? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _tickets = MutableStateFlow<List<StoredTicket>>(emptyList())

    /** All saved tickets, newest event first. Updates after every change. */
    val tickets: StateFlow<List<StoredTicket>> = _tickets.asStateFlow()

    fun init(context: Context) {
        if (helper != null) return
        helper = TicketDbHelper(context.applicationContext)
        scope.launch { refresh() }
    }

    private fun db(): TicketDbHelper =
        checkNotNull(helper) { "Call LocalTicketStore.init(context) in MainActivity.onCreate first" }

    // ---------------------------------------------------------------- create

    /** Saves the event and all its tickets together (all or nothing). Returns the ticket count. */
    suspend fun saveEventWithTickets(event: EventDetails, tickets: List<GeneratedTicket>): Int =
        withContext(Dispatchers.IO) {
            val database = db().writableDatabase
            val now = System.currentTimeMillis()

            database.beginTransaction()
            try {
                val eventId = database.insertOrThrow(
                    "events", null,
                    ContentValues().apply {
                        put("name", event.name)
                        put("description", event.description)
                        put("venue", event.venue)
                        put("start_millis", event.startMillis)
                        put("end_millis", event.endMillis)
                        put("created_at", now)
                    }
                )
                tickets.forEach { t ->
                    database.insertOrThrow(
                        "tickets", null,
                        ContentValues().apply {
                            put("code", t.code)
                            put("event_id", eventId)
                            put("event_name", t.eventName)
                            put("venue", t.venue)
                            put("start_millis", t.startMillis)
                            put("ticket_type", t.ticketType)
                            put("ticket_number", t.ticketNumber)
                            put("total_tickets", t.totalTickets)
                            put("qr_payload", t.qrPayload)
                            put("purchased_at", now)
                            put("expires_at", event.endMillis)
                        }
                    )
                }
                database.setTransactionSuccessful()
            } finally {
                database.endTransaction()
            }

            refresh()
            tickets.size
        }

    // ------------------------------------------------------------------ read

    /** Finds a ticket by its code, or null if this phone has no such ticket. */
    suspend fun find(code: String): StoredTicket? = withContext(Dispatchers.IO) {
        db().readableDatabase
            .query("tickets", null, "code = ?", arrayOf(code), null, null, null, "1")
            .use { if (it.moveToFirst()) it.toStoredTicket() else null }
    }

    suspend fun getEvent(eventId: Long): StoredEvent? = withContext(Dispatchers.IO) {
        db().readableDatabase
            .query("events", null, "id = ?", arrayOf(eventId.toString()), null, null, null, "1")
            .use { c ->
                if (c.moveToFirst()) {
                    StoredEvent(
                        id = c.getLong(c.getColumnIndexOrThrow("id")),
                        name = c.getString(c.getColumnIndexOrThrow("name")),
                        description = c.getString(c.getColumnIndexOrThrow("description")),
                        venue = c.getString(c.getColumnIndexOrThrow("venue")),
                        startMillis = c.getLong(c.getColumnIndexOrThrow("start_millis")),
                        endMillis = c.getLong(c.getColumnIndexOrThrow("end_millis"))
                    )
                } else null
            }
    }

    // ---------------------------------------------------------------- update

    /** Marks a ticket as used (checked in). */
    suspend fun markCheckedIn(code: String, time: Long, gate: String) {
        withContext(Dispatchers.IO) {
            db().writableDatabase.update(
                "tickets",
                ContentValues().apply {
                    put("checked_in_at", time)
                    put("gate", gate)
                },
                "code = ?",
                arrayOf(code)
            )
            refresh()
        }
    }

    /**
     * Changes an event's details and applies them to every ticket of that event
     * (name, venue, start time, and the expiry time = the event's end).
     * Ticket codes do not change, so QR codes that were already shared keep working.
     */
    suspend fun updateEvent(eventId: Long, event: EventDetails) {
        withContext(Dispatchers.IO) {
            val database = db().writableDatabase
            val id = eventId.toString()

            database.beginTransaction()
            try {
                database.update(
                    "events",
                    ContentValues().apply {
                        put("name", event.name)
                        put("description", event.description)
                        put("venue", event.venue)
                        put("start_millis", event.startMillis)
                        put("end_millis", event.endMillis)
                    },
                    "id = ?",
                    arrayOf(id)
                )

                val rows = database
                    .query("tickets", null, "event_id = ?", arrayOf(id), null, null, null)
                    .use { c -> buildList { while (c.moveToNext()) add(c.toStoredTicket()) } }

                rows.forEach { t ->
                    database.update(
                        "tickets",
                        ContentValues().apply {
                            put("event_name", event.name)
                            put("venue", event.venue)
                            put("start_millis", event.startMillis)
                            put("expires_at", event.endMillis)
                            put(
                                "qr_payload",
                                TicketGenerator.buildPayload(
                                    code = t.code,
                                    ticketType = t.ticketType,
                                    eventName = event.name,
                                    startMillis = event.startMillis,
                                    ticketNumber = t.ticketNumber,
                                    totalTickets = t.totalTickets
                                )
                            )
                        },
                        "code = ?",
                        arrayOf(t.code)
                    )
                }
                database.setTransactionSuccessful()
            } finally {
                database.endTransaction()
            }
            refresh()
        }
    }

    // ---------------------------------------------------------------- delete

    /** Deletes one ticket. Scanning its QR code afterwards shows "Invalid". */
    suspend fun deleteTicket(code: String) {
        withContext(Dispatchers.IO) {
            val database = db().writableDatabase
            database.delete("tickets", "code = ?", arrayOf(code))
            // an event with no tickets left is removed too
            database.execSQL("DELETE FROM events WHERE id NOT IN (SELECT DISTINCT event_id FROM tickets)")
            refresh()
        }
    }

    /** Deletes an event and all of its tickets. */
    suspend fun deleteEvent(eventId: Long) {
        withContext(Dispatchers.IO) {
            val database = db().writableDatabase
            val id = eventId.toString()
            database.beginTransaction()
            try {
                database.delete("tickets", "event_id = ?", arrayOf(id))
                database.delete("events", "id = ?", arrayOf(id))
                database.setTransactionSuccessful()
            } finally {
                database.endTransaction()
            }
            refresh()
        }
    }

    // --------------------------------------------------------------- helpers

    private fun refresh() {
        val list = db().readableDatabase
            .query("tickets", null, null, null, null, null, "event_id DESC, ticket_number ASC")
            .use { cursor ->
                buildList {
                    while (cursor.moveToNext()) add(cursor.toStoredTicket())
                }
            }
        _tickets.value = list
    }

    private fun Cursor.toStoredTicket(): StoredTicket {
        fun longOrNull(column: String): Long? =
            getColumnIndexOrThrow(column).let { if (isNull(it)) null else getLong(it) }

        fun stringOrNull(column: String): String? =
            getColumnIndexOrThrow(column).let { if (isNull(it)) null else getString(it) }

        return StoredTicket(
            code = getString(getColumnIndexOrThrow("code")),
            eventId = getLong(getColumnIndexOrThrow("event_id")),
            eventName = getString(getColumnIndexOrThrow("event_name")),
            venue = getString(getColumnIndexOrThrow("venue")),
            startMillis = getLong(getColumnIndexOrThrow("start_millis")),
            ticketType = getString(getColumnIndexOrThrow("ticket_type")),
            ticketNumber = getInt(getColumnIndexOrThrow("ticket_number")),
            totalTickets = getInt(getColumnIndexOrThrow("total_tickets")),
            qrPayload = getString(getColumnIndexOrThrow("qr_payload")),
            purchasedAt = getLong(getColumnIndexOrThrow("purchased_at")),
            expiresAt = getLong(getColumnIndexOrThrow("expires_at")),
            checkedInAt = longOrNull("checked_in_at"),
            gate = stringOrNull("gate")
        )
    }
}
