package com.example.ticket_scanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ticket_scanner.ui.theme.BluePrimary
import com.example.ticket_scanner.ui.theme.DarkSurface
import com.example.ticket_scanner.ui.theme.TextLightMuted

enum class NavTab(
    val title: String,
    val icon: ImageVector
) {
    SCAN("Scan", Icons.Default.QrCodeScanner),
    HISTORY("History", Icons.Default.History),
    ALERTS("Alerts", Icons.Default.Notifications),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun BottomNavBar(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    val backgroundColor = if (isDark) DarkSurface else Color.White

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab

            val itemBg = if (isSelected) BluePrimary else Color.Transparent
            val itemColor = if (isSelected) Color.White else (if (isDark) TextLightMuted else Color(0xFF64748B))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(itemBg)
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = itemColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = tab.title,
                        color = itemColor,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
