package com.example.ticket_scanner.eventcreation.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ticket_scanner.eventcreation.domain.TicketGenerator
import com.example.ticket_scanner.eventcreation.model.EventDetails
import com.example.ticket_scanner.eventcreation.model.EventForm
import com.example.ticket_scanner.eventcreation.model.GeneratedTicket
import com.example.ticket_scanner.localdb.LocalTicketStore
import com.example.ticket_scanner.localdb.StoredEvent
import com.example.ticket_scanner.network.CreateEventRequest
import com.example.ticket_scanner.network.CreateTicketTypeRequest
import com.example.ticket_scanner.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.TimeZone

/** Which part of the flow is showing. */
sealed interface EventCreationStep {
    /** The Create Event form. */
    data object Form : EventCreationStep

    /** The generated QR codes, to review and save. */
    data class Tickets(
        val event: EventDetails,
        val tickets: List<GeneratedTicket>
    ) : EventCreationStep

    /** The list of tickets saved on this phone. */
    data object SavedList : EventCreationStep

    /** Editing a saved event. */
    data class EditEvent(val eventId: Long) : EventCreationStep
}

class EventCreationViewModel : ViewModel() {

    private val _step = MutableStateFlow<EventCreationStep>(EventCreationStep.Form)
    val step: StateFlow<EventCreationStep> = _step.asStateFlow()

    // ---- Create Event form ----
    var form by mutableStateOf(EventForm())
        private set

    var showErrors by mutableStateOf(false)
        private set

    fun onFormChange(newForm: EventForm) {
        form = newForm
    }

    /** "Create tickets" button: check the form, then ask how many tickets. */
    fun onCreateTicketsClicked() {
        if (!form.errors().isValid) {
            showErrors = true
            return
        }
        showQuantityDialog = true
    }

    // ---- "How many tickets?" dialog ----
    var showQuantityDialog by mutableStateOf(false)
        private set

    var quantityInput by mutableStateOf("10")
        private set

    var ticketTypeInput by mutableStateOf("General Admission")
        private set

    var priceInput by mutableStateOf("10.00")
        private set

    var quantityError by mutableStateOf<String?>(null)
        private set

    fun onQuantityChange(text: String) {
        quantityInput = text.filter { it.isDigit() }.take(3)
        quantityError = null
    }

    fun onTicketTypeChange(text: String) {
        ticketTypeInput = text
    }

    fun onPriceChange(text: String) {
        priceInput = text.filter { it.isDigit() || it == '.' }
    }

    fun dismissQuantityDialog() {
        showQuantityDialog = false
        quantityError = null
    }

    /** "OK" in the dialog: generate the tickets and show them for review (not saved yet). */
    fun confirmQuantity() {
        val quantity = quantityInput.toIntOrNull()
        if (quantity == null || quantity < 1 || quantity > TicketGenerator.MAX_TICKETS) {
            quantityError = "Enter a number from 1 to ${TicketGenerator.MAX_TICKETS}"
            return
        }

        val event = form.toEventDetails()
        val ticketType = ticketTypeInput.trim().ifBlank { "General Admission" }

        val tickets = TicketGenerator.generate(
            event = event,
            quantity = quantity,
            ticketType = ticketType,
            // never reuse a code that is already saved on this phone
            codeExists = { code -> LocalTicketStore.tickets.value.any { it.code == code } }
        )

        isSaved = false
        isSaving = false
        saveError = null
        showQuantityDialog = false
        _step.value = EventCreationStep.Tickets(event, tickets)
    }

    // ---- Saving the generated tickets ----
    var isSaving by mutableStateOf(false)
        private set

    var isSaved by mutableStateOf(false)
        private set

    var saveError by mutableStateOf<String?>(null)
        private set

    /** "Save tickets": store the event and its tickets in the MySQL database via backend API and locally. */
    fun saveTickets() {
        val current = _step.value as? EventCreationStep.Tickets ?: return
        if (isSaving || isSaved) return

        isSaving = true
        saveError = null
        viewModelScope.launch {
            try {
                // 1. Format start and end times to ISO-8601 for Spring Boot backend
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                val startTimeStr = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(current.event.startMillis),
                    ZoneId.systemDefault()
                ).format(formatter)
                val endTimeStr = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(current.event.endMillis),
                    ZoneId.systemDefault()
                ).format(formatter)

                val eventRequest = CreateEventRequest(
                    title = current.event.name,
                    venue = current.event.venue,
                    location = current.event.venue, // fallback to venue
                    startTime = startTimeStr,
                    endTime = endTimeStr
                )

                // 2. Call backend to create event in MySQL database
                val eventResponse = RetrofitClient.eventApi.createEvent(eventRequest)
                if (!eventResponse.isSuccessful) {
                    val errCode = eventResponse.code()
                    val errBody = eventResponse.errorBody()?.string() ?: eventResponse.message()
                    saveError = "Failed to save event to database (HTTP $errCode): $errBody"
                    isSaving = false
                    return@launch
                }

                val createdEvent = eventResponse.body()
                if (createdEvent == null) {
                    saveError = "Backend returned empty event response."
                    isSaving = false
                    return@launch
                }

                // 3. Create ticket type(s) on backend
                val priceVal = priceInput.toBigDecimalOrNull() ?: BigDecimal("10.00")
                val ticketTypeRequest = CreateTicketTypeRequest(
                    name = current.tickets.firstOrNull()?.ticketType ?: "General Admission",
                    price = priceVal,
                    totalQuantity = current.tickets.size
                )

                val ticketTypeResponse = RetrofitClient.eventApi.createTicketType(createdEvent.id, ticketTypeRequest)
                if (!ticketTypeResponse.isSuccessful) {
                    val errCode = ticketTypeResponse.code()
                    val errBody = ticketTypeResponse.errorBody()?.string() ?: ticketTypeResponse.message()
                    saveError = "Event created, but failed to save ticket types (HTTP $errCode): $errBody"
                    isSaving = false
                    return@launch
                }

                // 4. Save locally for offline redundancy
                LocalTicketStore.saveEventWithTickets(current.event, current.tickets)
                isSaved = true
            } catch (e: Exception) {
                saveError = "Could not save the tickets: ${e.message}"
            }
            isSaving = false
        }
    }

    /** Back or "Discard and edit" before saving: return to the form, keeping what was typed. */
    fun discardTickets() {
        _step.value = EventCreationStep.Form
    }

    // ---- Saved tickets list ----
    fun openSavedTickets() {
        _step.value = EventCreationStep.SavedList
    }

    fun closeSavedTickets() {
        _step.value = EventCreationStep.Form
    }

    /** "View saved tickets" right after saving: clear the form, then open the list. */
    fun viewSavedAfterSave() {
        resetAll()
        _step.value = EventCreationStep.SavedList
    }

    fun deleteTicket(code: String) {
        viewModelScope.launch {
            runCatching { LocalTicketStore.deleteTicket(code) }
        }
    }

    fun deleteEvent(eventId: Long) {
        viewModelScope.launch {
            runCatching { LocalTicketStore.deleteEvent(eventId) }
        }
    }

    // ---- Editing a saved event ----
    var editForm by mutableStateOf(EventForm())
        private set

    var editShowErrors by mutableStateOf(false)
        private set

    var isUpdating by mutableStateOf(false)
        private set

    var editError by mutableStateOf<String?>(null)
        private set

    /** "Edit event": load the event from the database into the form. */
    fun startEditEvent(eventId: Long) {
        viewModelScope.launch {
            val event = LocalTicketStore.getEvent(eventId) ?: return@launch
            editForm = event.toForm()
            editShowErrors = false
            editError = null
            isUpdating = false
            _step.value = EventCreationStep.EditEvent(eventId)
        }
    }

    fun onEditFormChange(newForm: EventForm) {
        editForm = newForm
    }

    /** "Save changes": write the new details to the database (and to all the event's tickets). */
    fun saveEventChanges() {
        val current = _step.value as? EventCreationStep.EditEvent ?: return
        if (isUpdating) return
        if (!editForm.errors().isValid) {
            editShowErrors = true
            return
        }

        isUpdating = true
        editError = null
        viewModelScope.launch {
            try {
                LocalTicketStore.updateEvent(current.eventId, editForm.toEventDetails())
                _step.value = EventCreationStep.SavedList
            } catch (e: Exception) {
                editError = "Could not save the changes. Try again."
            }
            isUpdating = false
        }
    }

    fun cancelEdit() {
        _step.value = EventCreationStep.SavedList
    }

    // ---- Finish ----
    /** "Done" after saving: clear everything and start a new event. */
    fun done() {
        resetAll()
        _step.value = EventCreationStep.Form
    }

    private fun resetAll() {
        form = EventForm()
        showErrors = false
        quantityInput = "10"
        ticketTypeInput = "General Admission"
        priceInput = "10.00"
        quantityError = null
        showQuantityDialog = false
        isSaving = false
        isSaved = false
        saveError = null
    }
}

/** Fills the form from a saved event. The date picker uses midnight UTC for the chosen day. */
private fun StoredEvent.toForm(): EventForm {
    val start = Calendar.getInstance().apply { timeInMillis = startMillis }
    val end = Calendar.getInstance().apply { timeInMillis = endMillis }
    val dateUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis

    return EventForm(
        name = name,
        description = description,
        venue = venue,
        dateUtcMillis = dateUtc,
        startMinutes = start.get(Calendar.HOUR_OF_DAY) * 60 + start.get(Calendar.MINUTE),
        endMinutes = end.get(Calendar.HOUR_OF_DAY) * 60 + end.get(Calendar.MINUTE)
    )
}
