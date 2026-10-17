package com.example.ticket_scanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ticket_scanner.model.NotificationItem
import com.example.ticket_scanner.model.NotificationType
import com.example.ticket_scanner.ui.components.BottomNavBar
import com.example.ticket_scanner.ui.components.NavTab
import com.example.ticket_scanner.ui.theme.BlueHeaderBackground
import com.example.ticket_scanner.ui.theme.BluePrimary
import com.example.ticket_scanner.ui.theme.LightBackground
import com.example.ticket_scanner.ui.theme.RejectedRed
import com.example.ticket_scanner.ui.theme.TextDark
import com.example.ticket_scanner.ui.theme.TextMuted
import com.example.ticket_scanner.ui.theme.ValidGreen

@Composable
fun AlertsScreen(
    notifications: List<NotificationItem>,
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    onMarkAllAsRead: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 70.dp)
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BlueHeaderBackground)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notifications",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White
                    )
                }
            }

            // Notifications List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notification ->
                    NotificationCard(notification = notification)
                }
            }

            // Mark All as Read Button
            OutlinedButton(
                onClick = onMarkAllAsRead,
                shape = RoundedCornerShape(24.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = androidx.compose.ui.graphics.SolidColor(BluePrimary)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .height(48.dp)
            ) {
                Text(
                    text = "Mark all as read",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BluePrimary
                )
            }
        }

        // Bottom Navigation Bar
        BottomNavBar(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            isDark = false,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun NotificationCard(
    notification: NotificationItem
) {
    val (iconColor, iconVector) = when (notification.type) {
        NotificationType.INFO -> Pair(BluePrimary, Icons.Default.ConfirmationNumber)
        NotificationType.EVENT_ALERT -> Pair(Color(0xFF3B82F6), Icons.Default.EventRepeat)
        NotificationType.SYNC_SUCCESS -> Pair(ValidGreen, Icons.Default.CheckCircle)
        NotificationType.DUPLICATE_CHECKIN -> Pair(RejectedRed, Icons.Default.Warning)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Circle Icon Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = notification.title,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.description,
                    fontSize = 13.sp,
                    color = TextMuted,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = notification.timestamp,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }
    }
}
