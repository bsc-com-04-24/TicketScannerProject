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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ticket_scanner.ui.components.BottomNavBar
import com.example.ticket_scanner.ui.components.NavTab
import com.example.ticket_scanner.ui.theme.BlueHeaderBackground
import com.example.ticket_scanner.ui.theme.BluePrimary
import com.example.ticket_scanner.ui.theme.LightBackground
import com.example.ticket_scanner.ui.theme.TextDark
import com.example.ticket_scanner.ui.theme.TextMuted

@Composable
fun SettingsScreen(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    var autoSyncEnabled by remember { mutableStateOf(true) }
    var soundVibrationEnabled by remember { mutableStateOf(true) }
    var pushNotificationsEnabled by remember { mutableStateOf(true) }

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SettingToggleRow(
                    title = "Auto Synchronisation",
                    subtitle = "Automatically sync scanned tickets to cloud server",
                    icon = Icons.Default.CloudSync,
                    isChecked = autoSyncEnabled,
                    onCheckedChange = { autoSyncEnabled = it }
                )

                SettingToggleRow(
                    title = "Haptic & Sound Feedback",
                    subtitle = "Vibrate and play chime on ticket verification",
                    icon = Icons.Default.Vibration,
                    isChecked = soundVibrationEnabled,
                    onCheckedChange = { soundVibrationEnabled = it }
                )

                SettingToggleRow(
                    title = "Push Notifications",
                    subtitle = "Receive alert updates during live event scanning",
                    icon = Icons.Default.Notifications,
                    isChecked = pushNotificationsEnabled,
                    onCheckedChange = { pushNotificationsEnabled = it }
                )

                SettingItemRow(
                    title = "Scanner Security & Access",
                    subtitle = "Manage operator PIN and gate permissions",
                    icon = Icons.Default.Lock
                )

                SettingItemRow(
                    title = "About Ticket Scanner",
                    subtitle = "Version 1.0.0 • Build 2025.1",
                    icon = Icons.Default.Info
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
fun SettingToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = BluePrimary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = BluePrimary
                )
            )
        }
    }
}

@Composable
fun SettingItemRow(
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = BluePrimary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
