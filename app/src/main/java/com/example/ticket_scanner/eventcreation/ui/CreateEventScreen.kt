package com.example.ticket_scanner.eventcreation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ticket_scanner.eventcreation.model.EventForm
import com.example.ticket_scanner.eventcreation.model.FormErrors
import com.example.ticket_scanner.ui.theme.BluePrimary
import com.example.ticket_scanner.ui.theme.LightBackground
import com.example.ticket_scanner.ui.theme.TextMuted

@Composable
fun CreateEventScreen(
    form: EventForm,
    showErrors: Boolean,
    onFormChange: (EventForm) -> Unit,
    onCreateTickets: () -> Unit,
    onViewSaved: () -> Unit = {},
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {}
) {
    val errors = if (showErrors) form.errors() else FormErrors()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
            .imePadding()
    ) {
        EventHeader(title = "Create Event")

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Enter the event details, then create the tickets.",
                color = TextMuted,
                fontSize = 14.sp
            )

            EventFormFields(form = form, errors = errors, onFormChange = onFormChange)

            Button(
                onClick = onCreateTickets,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Create tickets",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            OutlinedButton(
                onClick = onViewSaved,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Saved tickets",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BluePrimary
                )
            }
        }

        bottomBar()
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun PreviewCreateEvent() = MaterialTheme {
    CreateEventScreen(
        form = EventForm(name = "Malawi Tech & Innovation Expo", venue = "Bingu International Convention Centre"),
        showErrors = true,
        onFormChange = {},
        onCreateTickets = {}
    )
}