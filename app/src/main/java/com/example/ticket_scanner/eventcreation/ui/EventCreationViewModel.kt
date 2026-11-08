package com.example.ticket_scanner.eventcreation.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.ticket_scanner.eventcreation.data.EventRepository
import com.example.ticket_scanner.eventcreation.domain.TicketGenerator
import com.example.ticket_scanner.eventcreation.model.EventDetails
import com.example.ticket_scanner.eventcreation.model.EventForm
import com.example.ticket_scanner.eventcreation.model.GeneratedTicket
import com.example.ticket_scanner.validation.data.TicketRepository
import com.example.ticket_scanner.validation.model.TicketRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Which part of the flow is showing. */
sealed interface EventCreationStep {
    data object Form : EventCreationStep
    data class Tickets(
        val event: EventDetails,
        val tickets: List<GeneratedTicket>
    ) : EventCreationStep
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

    var quantityError by mutableStateOf<String?>(null)
        private set

    fun onQuantityChange(text: String) {
        quantityInput = text.filter { it.isDigit() }.take(3)
        quantityError = null
    }

    fun onTicketTypeChange(text: String) {
        ticketTypeInput = text
    }

    fun dismissQuantityDialog() {
        showQuantityDialog = false
        quantityError = null
    }

    /** "OK" in the dialog: create the tickets. */
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
            codeExists = { TicketRepository.find(it) != null }
        )

        // Register the new tickets with validation, so scanning one of these QR codes
        // gives "Valid" (and "Already used" the second time).
        val now = System.currentTimeMillis()
        tickets.forEach { ticket ->
            TicketRepository.add(
                TicketRecord(
                    code = ticket.code,
                    type = ticket.ticketType,
                    event = ticket.eventName,
                    purchasedAt = now,
                    expiresAt = event.endMillis
                )
            )
        }

        EventRepository.add(event)

        showQuantityDialog = false
        _step.value = EventCreationStep.Tickets(event, tickets)
    }

    /** "Done" on the tickets screen, or Back: clear everything and start again. */
    fun done() {
        form = EventForm()
        showErrors = false
        quantityInput = "10"
        ticketTypeInput = "General Admission"
        quantityError = null
        showQuantityDialog = false
        _step.value = EventCreationStep.Form
    }
}