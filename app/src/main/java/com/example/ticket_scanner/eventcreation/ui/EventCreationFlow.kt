package com.example.ticket_scanner.eventcreation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

/**
 * The whole event creation feature as one composable:
 *
 *   Create Event form  ->  "Create tickets"  ->  "How many?" dialog  ->  QR codes screen
 *
 * @param bottomBar the app's bottom navigation bar, shown under the form
 *                  (the QR codes screen is full screen, without it).
 */
@Composable
fun EventCreationFlow(
    viewModel: EventCreationViewModel,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {}
) {
    val step by viewModel.step.collectAsState()

    // Back on the QR codes screen finishes the flow instead of closing the app
    BackHandler(enabled = step is EventCreationStep.Tickets) { viewModel.done() }

    when (val current = step) {
        is EventCreationStep.Form -> {
            CreateEventScreen(
                form = viewModel.form,
                showErrors = viewModel.showErrors,
                onFormChange = viewModel::onFormChange,
                onCreateTickets = viewModel::onCreateTicketsClicked,
                modifier = modifier,
                bottomBar = bottomBar
            )

            if (viewModel.showQuantityDialog) {
                QuantityDialog(
                    quantity = viewModel.quantityInput,
                    ticketType = viewModel.ticketTypeInput,
                    error = viewModel.quantityError,
                    onQuantityChange = viewModel::onQuantityChange,
                    onTicketTypeChange = viewModel::onTicketTypeChange,
                    onConfirm = viewModel::confirmQuantity,
                    onDismiss = viewModel::dismissQuantityDialog
                )
            }
        }

        is EventCreationStep.Tickets -> {
            GeneratedTicketsScreen(
                event = current.event,
                tickets = current.tickets,
                onDone = viewModel::done,
                modifier = modifier
            )
        }
    }
}