package com.example.ticket_scanner.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ticket_scanner.network.EventResponse
import com.example.ticket_scanner.ui.components.BottomNavBar
import com.example.ticket_scanner.ui.components.NavTab
import com.example.ticket_scanner.ui.theme.BlueHeaderBackground
import com.example.ticket_scanner.ui.theme.BluePrimary
import com.example.ticket_scanner.ui.theme.RejectedRed
import com.example.ticket_scanner.ui.theme.TextMuted

/** The Events tab: a dynamic page displaying real backend events while preserving the original UI design language. */
@Composable
fun EventsScreen(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    viewModel: EventsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    isDarkMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        viewModel.fetchEvents()
    }

    val events = viewModel.events
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BlueHeaderBackground)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Events",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Refresh",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { viewModel.fetchEvents() }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    isLoading && events.isEmpty() -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = BluePrimary
                        )
                    }
                    errorMessage != null && events.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = errorMessage,
                                color = RejectedRed,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.fetchEvents() },
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Retry", color = Color.White)
                            }
                        }
                    }
                    events.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No events found in backend.\nCreate an event from the Create tab to see it here!",
                                color = TextMuted,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(events, key = { it.id }) { event ->
                                EventCard(event)
                            }
                        }
                    }
                }
            }
        }

        BottomNavBar(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            isDark = isDarkMode,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

private val posterGradients = listOf(
    Pair(Color(0xFF3A3FB5), Color(0xFF1B7FD4)) to "💻",
    Pair(Color(0xFF7B2FBE), Color(0xFFE0407B)) to "🎵",
    Pair(Color(0xFF1B9E4B), Color(0xFF0E5E2D)) to "⚽",
    Pair(Color(0xFFE59A0B), Color(0xFFD1481B)) to "🎤",
    Pair(Color(0xFF0E7490), Color(0xFF164E63)) to "🏃"
)

private fun getGradientAndEmoji(id: Long): Pair<Pair<Color, Color>, String> {
    val index = (id % posterGradients.size).toInt()
    return posterGradients[index]
}

@Composable
private fun EventCard(event: EventResponse) {
    val (gradientPair, emoji) = getGradientAndEmoji(event.id)
    val (colorStart, colorEnd) = gradientPair
    val formattedDate = try {
        val parser = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val dt = java.time.LocalDateTime.parse(event.startTime, parser)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("EEE, d MMM yyyy '·' h:mm a", java.util.Locale.US)
        dt.format(formatter)
    } catch (e: Exception) {
        event.startTime.replace("T", "  ")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Poster: a gradient with a large emoji and the event name
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(
                        Brush.linearGradient(listOf(colorStart, colorEnd))
                    )
            ) {
                Text(
                    text = emoji,
                    fontSize = 64.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
                Text(
                    text = event.title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = event.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(text = "📍 ${event.venue} (${event.location})", color = TextMuted, fontSize = 13.sp)
                Spacer(Modifier.height(2.dp))
                Text(text = "🗓 $formattedDate", color = TextMuted, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                Text(text = "Status: ${event.status}", color = BluePrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
