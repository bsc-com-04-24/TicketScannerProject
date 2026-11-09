package com.example.ticket_scanner.eventcreation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

/**
 * The whole feature as one composable:
 *
 *   Create Event form -> "Create tickets" -> "How many?" dialog -> Review QR codes
 *   -> "Save tickets" (stored on the phone) -> Saved tickets list
 *                                               |-> Edit event
 *                                               |-> Delete event / Delete ticket
 *
 * The form also has a "Saved tickets" button that opens the list directly.
 *
 * @param bottomBar the app's bottom navigation bar, shown under the form and the saved list.
 */
@Composable
fun EventCreationFlow(
    viewModel: EventCreationViewModel,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {}
) {
    val step by viewModel.step.collectAsState()

    // Back: leave the current step instead of closing the app
    BackHandler(enabled = step !is EventCreationStep.Form) {
        when (step) {
            is EventCreationStep.Tickets ->
                if (viewModel.isSaved) viewModel.done() else viewModel.discardTickets()
            is EventCreationStep.SavedList -> viewModel.closeSavedTickets()
            is EventCreationStep.EditEvent -> viewModel.cancelEdit()
            else -> Unit
        }
    }

    when (val current = step) {
        is EventCreationStep.Form -> {
            CreateEventScreen(
                form = viewModel.form,
                showErrors = viewModel.showErrors,
                onFormChange = viewModel::onFormChange,
                onCreateTickets = viewModel::onCreateTicketsClicked,
                onViewSaved = viewModel::openSavedTickets,
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
                isSaving = viewModel.isSaving,
                isSaved = viewModel.isSaved,
                saveError = viewModel.saveError,
                onSave = viewModel::saveTickets,
                onDiscard = viewModel::discardTickets,
                onViewSaved = viewModel::viewSavedAfterSave,
                onDone = viewModel::done,
                modifier = modifier
            )
        }

        is EventCreationStep.SavedList -> {
            SavedTicketsScreen(
                onBack = viewModel::closeSavedTickets,
                onEditEvent = viewModel::startEditEvent,
                onDeleteEvent = viewModel::deleteEvent,
                onDeleteTicket = viewModel::deleteTicket,
                modifier = modifier,
                bottomBar = bottomBar
            )
        }

        is EventCreationStep.EditEvent -> {
            EditEventScreen(
                form = viewModel.editForm,
                showErrors = viewModel.editShowErrors,
                isSaving = viewModel.isUpdating,
                saveError = viewModel.editError,
                onFormChange = viewModel::onEditFormChange,
                onSave = viewModel::saveEventChanges,
                onCancel = viewModel::cancelEdit,
                modifier = modifier
            )
        }
    }
}