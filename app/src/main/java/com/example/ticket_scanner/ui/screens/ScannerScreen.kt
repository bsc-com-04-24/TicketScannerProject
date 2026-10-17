package com.example.ticket_scanner.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ticket_scanner.model.ScanStats
import com.example.ticket_scanner.ui.components.BottomNavBar
import com.example.ticket_scanner.ui.components.NavTab
import com.example.ticket_scanner.ui.theme.BlueDarkBackground
import com.example.ticket_scanner.ui.theme.BluePrimary
import com.example.ticket_scanner.ui.theme.DarkSurface
import com.example.ticket_scanner.ui.theme.RejectedRed
import com.example.ticket_scanner.ui.theme.TextLightMuted
import com.example.ticket_scanner.ui.theme.ValidGreen

@Composable
fun ScannerScreen(
    stats: ScanStats,
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    onVerifyTicket: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFlashlightOn by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var showManualEntryDialog by remember { mutableStateOf(false) }
    var manualCodeInput by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BlueDarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 70.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            ScannerTopBar(
                eventName = "Malawi Music Concert",
                isOnline = stats.isOnline,
                isFlashlightOn = isFlashlightOn,
                onFlashlightToggle = { isFlashlightOn = !isFlashlightOn }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stats Cards Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Scanned",
                    count = stats.scannedCount,
                    countColor = Color.White,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Valid",
                    count = stats.validCount,
                    countColor = ValidGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Rejected",
                    count = stats.rejectedCount,
                    countColor = RejectedRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Scanner Target Viewfinder Frame
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clickable {
                        isVerifying = true
                        onVerifyTicket("TM-7843-2310")
                    },
                contentAlignment = Alignment.Center
            ) {
                // Viewfinder Corner Frame Lines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 5.dp.toPx()
                    val cornerLength = 40.dp.toPx()
                    val cornerRadius = 12.dp.toPx()

                    val w = size.width
                    val h = size.height

                    val color = Color.White

                    // Top Left Corner
                    drawPath(
                        path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, cornerLength)
                            lineTo(0f, cornerRadius)
                            quadraticTo(0f, 0f, cornerRadius, 0f)
                            lineTo(cornerLength, 0f)
                        },
                        color = color,
                        style = Stroke(width = strokeWidth)
                    )

                    // Top Right Corner
                    drawPath(
                        path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w - cornerLength, 0f)
                            lineTo(w - cornerRadius, 0f)
                            quadraticTo(w, 0f, w, cornerRadius)
                            lineTo(w, cornerLength)
                        },
                        color = color,
                        style = Stroke(width = strokeWidth)
                    )

                    // Bottom Left Corner
                    drawPath(
                        path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, h - cornerLength)
                            lineTo(0f, h - cornerRadius)
                            quadraticTo(0f, h, cornerRadius, h)
                            lineTo(cornerLength, h)
                        },
                        color = color,
                        style = Stroke(width = strokeWidth)
                    )

                    // Bottom Right Corner
                    drawPath(
                        path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w - cornerLength, h)
                            lineTo(w - cornerRadius, h)
                            quadraticTo(w, h, w, h - cornerRadius)
                            lineTo(w, h - cornerLength)
                        },
                        color = color,
                        style = Stroke(width = strokeWidth)
                    )
                }

                // Center Scanner Icon
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scan Target",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Align QR code or barcode\ninside the frame",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Manual Code Entry Button
            Button(
                onClick = { showManualEntryDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(52.dp)
            ) {
                Text(
                    text = "Enter Code Manually",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom Navigation Bar
        BottomNavBar(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Verifying Overlay State
        if (isVerifying) {
            VerifyingOverlay()
        }

        // Manual Code Entry Dialog
        if (showManualEntryDialog) {
            AlertDialog(
                onDismissRequest = { showManualEntryDialog = false },
                title = { Text("Enter Ticket Code") },
                text = {
                    Column {
                        Text(
                            "Type the ticket code printed on the ticket to check in manually.",
                            fontSize = 13.sp,
                            color = TextLightMuted
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = manualCodeInput,
                            onValueChange = { manualCodeInput = it },
                            placeholder = { Text("e.g. TM-7843-2310") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (manualCodeInput.isNotBlank()) {
                                showManualEntryDialog = false
                                isVerifying = true
                                onVerifyTicket(manualCodeInput)
                                manualCodeInput = ""
                            }
                        }
                    ) {
                        Text("Verify")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showManualEntryDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ScannerTopBar(
    eventName: String,
    isOnline: Boolean,
    isFlashlightOn: Boolean,
    onFlashlightToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = eventName,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Online Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isOnline) ValidGreen else RejectedRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOnline) "Online" else "Offline",
                        color = if (isOnline) ValidGreen else RejectedRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        IconButton(onClick = onFlashlightToggle) {
            Icon(
                imageVector = if (isFlashlightOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                contentDescription = "Toggle Flashlight",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: Int,
    countColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = TextLightMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = count.toString(),
                color = countColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
