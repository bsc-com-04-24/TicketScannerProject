package com.example.ticket_scanner.validation.ui

import androidx.lifecycle.ViewModel
import com.example.ticket_scanner.validation.domain.TicketValidator
import com.example.ticket_scanner.validation.model.ScannedTicket
import com.example.ticket_scanner.validation.model.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface ValidationUiState {
    /** Nothing validated yet: the screen that supplies tickets is showing. */
    data object Idle : ValidationUiState

    /** A result is showing. */
    data class Showing(val result: ValidationResult) : ValidationUiState
}

class ValidationViewModel : ViewModel() {

    private val _state = MutableStateFlow<ValidationUiState>(ValidationUiState.Idle)
    val state: StateFlow<ValidationUiState> = _state.asStateFlow()

    /** THE ENTRY POINT: call this with the scanned ticket to validate it. */
    fun validate(scanned: ScannedTicket) {
        _state.value = ValidationUiState.Showing(TicketValidator.validate(scanned))
    }

    /** "Scan Next": leave the result screen. */
    fun scanNext() {
        _state.value = ValidationUiState.Idle
    }
}