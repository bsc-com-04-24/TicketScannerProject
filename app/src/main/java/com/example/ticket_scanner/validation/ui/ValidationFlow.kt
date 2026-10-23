package com.example.ticket_scanner.validation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ticket_scanner.validation.model.ScannedTicket
import com.example.ticket_scanner.validation.model.ValidationResult
import com.example.ticket_scanner.validation.model.ValidationStatus

/**
 * The whole validation feature as one composable.
 *
 *  - Idle    -> [idleContent] is shown. It receives an `onValidate` function; calling it with a
 *               ScannedTicket starts validation. (Default: the demo sample picker.)
 *  - Showing -> the matching result screen is shown. "Scan Next" or Back returns to Idle.
 */
@Composable
fun ValidationFlow(
    viewModel: ValidationViewModel,
    modifier: Modifier = Modifier,
    idleContent: @Composable (onValidate: (ScannedTicket) -> Unit) -> Unit = { onValidate ->
        SampleTicketPicker(onValidate = onValidate)
    }
) {
    val state by viewModel.state.collectAsState()

    BackHandler(enabled = state is ValidationUiState.Showing) { viewModel.scanNext() }

    when (val current = state) {
        is ValidationUiState.Idle -> idleContent(viewModel::validate)

        is ValidationUiState.Showing ->
            ValidationResultScreen(
                result = current.result,
                onScanNext = viewModel::scanNext,
                modifier = modifier
            )
    }
}

/** Picks the right one of the four result screens. */
@Composable
fun ValidationResultScreen(
    result: ValidationResult,
    onScanNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (result.status) {
        ValidationStatus.VALID -> ValidResultScreen(result, onScanNext, modifier)
        ValidationStatus.INVALID -> InvalidResultScreen(result, onScanNext, modifier)
        ValidationStatus.EXPIRED -> ExpiredResultScreen(result, onScanNext, modifier)
        ValidationStatus.ALREADY_USED -> AlreadyUsedResultScreen(result, onScanNext, modifier)
    }
}