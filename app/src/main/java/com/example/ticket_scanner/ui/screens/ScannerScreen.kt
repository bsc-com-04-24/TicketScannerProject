package com.example.ticket_scanner.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ticket_scanner.model.ScanItem
import com.example.ticket_scanner.model.ScanStats
import com.example.ticket_scanner.model.ScanStatus
import com.example.ticket_scanner.ui.components.BottomNavBar
import com.example.ticket_scanner.ui.components.NavTab
import com.example.ticket_scanner.ui.theme.BlueCardBackground
import com.example.ticket_scanner.ui.theme.BlueDarkBackground
import com.example.ticket_scanner.ui.theme.BluePrimary
import com.example.ticket_scanner.ui.theme.ExpiredOrange
import com.example.ticket_scanner.ui.theme.PendingGray
import com.example.ticket_scanner.ui.theme.RejectedRed
import com.example.ticket_scanner.ui.theme.TextLight
import com.example.ticket_scanner.ui.theme.TextLightMuted
import com.example.ticket_scanner.ui.theme.TextMuted
import com.example.ticket_scanner.ui.theme.ValidGreen
import com.example.ticket_scanner.validation.data.SampleScans
import com.example.ticket_scanner.validation.model.ValidationResult
import com.example.ticket_scanner.validation.model.ValidationStatus
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(
    stats: ScanStats,
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    onVerifyTicket: (String) -> Unit,
    isDarkMode: Boolean = true,
    modifier: Modifier = Modifier,
    onOpenOfflineMode: () -> Unit = {},
    recentScans: List<ScanItem> = emptyList(),
    lastValidationResult: ValidationResult? = null
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val bg = if (isDarkMode) BlueDarkBackground else MaterialTheme.colorScheme.background
    val cardBg = if (isDarkMode) BlueCardBackground else MaterialTheme.colorScheme.surface
    val textColor = if (isDarkMode) TextLight else MaterialTheme.colorScheme.onSurface
    val textMutedColor = if (isDarkMode) TextLightMuted else TextMuted

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var manualCodeInput by remember { mutableStateOf("") }
    var isFlashOn by remember { mutableStateOf(false) }
    var cameraControlRef by remember { mutableStateOf<Camera?>(null) }
    var isScanningEnabled by remember { mutableStateOf(true) }
    var isContinuousMode by remember { mutableStateOf(false) }

    // Always use the latest verification callback
    val currentVerifyCallback by rememberUpdatedState(onVerifyTicket)

    // Auto-resume scanning in continuous mode after delay
    LaunchedEffect(isScanningEnabled, isContinuousMode) {
        if (!isScanningEnabled && isContinuousMode) {
            delay(2000)
            isScanningEnabled = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "EVENT SCANNER",
                        color = BluePrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Text(
                        "Gate Check-In",
                        color = textColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ONLINE STATUS BADGE
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isDarkMode) Color.White.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.OfflinePin,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(Modifier.width(5.dp))

                        Text(
                            "Online",
                            color = textColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // FLASHLIGHT TOGGLE
                    IconButton(
                        onClick = {
                            isFlashOn = !isFlashOn
                            cameraControlRef?.cameraControl?.enableTorch(isFlashOn)
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                if (isFlashOn) BluePrimary
                                else if (isDarkMode) Color.White.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            )
                    ) {
                        Icon(
                            if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Toggle flashlight",
                            tint = if (isFlashOn || isDarkMode) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // RECENT SCAN TOAST / BANNER
            AnimatedVisibility(
                visible = lastValidationResult != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                lastValidationResult?.let { result ->
                    val bannerBg = when (result.status) {
                        ValidationStatus.VALID -> ValidGreen
                        ValidationStatus.INVALID -> RejectedRed
                        ValidationStatus.EXPIRED -> ExpiredOrange
                        ValidationStatus.ALREADY_USED -> Color(0xFF8B5CF6)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bannerBg)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (result.status == ValidationStatus.VALID) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = result.status.name,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${result.holderName ?: "Attendee"} • ${result.ticketCode}",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // OFFLINE MODE NAVIGATION CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { onOpenOfflineMode() },
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF173A4D) else cardBg
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.OfflinePin,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Switch to Offline Scanner",
                            color = textColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Scan tickets without an internet connection",
                            color = textMutedColor,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        "→",
                        color = BluePrimary,
                        fontSize = 22.sp
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // SCAN STATISTICS CARDS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    "Scanned",
                    stats.scannedCount,
                    BluePrimary,
                    cardBg,
                    textColor,
                    Modifier.weight(1f)
                )

                StatCard(
                    "Valid",
                    stats.validCount,
                    ValidGreen,
                    cardBg,
                    textColor,
                    Modifier.weight(1f)
                )

                StatCard(
                    "Rejected",
                    stats.rejectedCount,
                    RejectedRed,
                    cardBg,
                    textColor,
                    Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(14.dp))

            // CONTINUOUS SCAN MODE & CONTROLS BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isContinuousMode) BluePrimary.copy(alpha = 0.2f)
                            else if (isDarkMode) Color.White.copy(alpha = 0.08f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { isContinuousMode = !isContinuousMode }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = "Continuous Mode",
                        tint = if (isContinuousMode) BluePrimary else textMutedColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isContinuousMode) "Continuous Mode: ON" else "Continuous Mode: OFF",
                        color = if (isContinuousMode) BluePrimary else textColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (recentScans.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = textMutedColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${recentScans.size} recent",
                            color = textMutedColor,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // CAMERA VIEWFINDER BOX
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .height(290.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardBg),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    CameraPreviewView(
                        isScanningEnabled = isScanningEnabled,
                        onBarcodeDetected = { code ->
                            if (isScanningEnabled) {
                                isScanningEnabled = false
                                currentVerifyCallback(code)
                            }
                        },
                        onCameraBound = { camera ->
                            cameraControlRef = camera
                        }
                    )

                    ScannerReticleOverlay()

                    if (!isScanningEnabled && !isContinuousMode) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Ticket Captured!",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        isScanningEnabled = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BluePrimary
                                    )
                                ) {
                                    Text("Scan Next Ticket")
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            "Camera access is required to scan QR codes.",
                            color = textColor,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(14.dp))

                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    Manifest.permission.CAMERA
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BluePrimary
                            )
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            }

            // RECENT SCANS MINI CAROUSEL
            if (recentScans.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentScans.take(5)) { scanItem ->
                        RecentScanMiniChip(scanItem, isDarkMode = isDarkMode)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // MANUAL TICKET ENTRY CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardBg
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Manual Ticket Entry",
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        "Enter ticket ID if barcode is damaged or unreadable",
                        color = textMutedColor,
                        fontSize = 11.sp
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualCodeInput,
                            onValueChange = {
                                manualCodeInput = it
                            },
                            placeholder = {
                                Text(
                                    "e.g. MTIE-2026-004281",
                                    color = textMutedColor,
                                    fontSize = 13.sp
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (manualCodeInput.isNotBlank()) {
                                        focusManager.clearFocus()
                                        currentVerifyCallback(manualCodeInput.trim())
                                        manualCodeInput = ""
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = BluePrimary,
                                unfocusedBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.15f) else Color(0xFFE2E8F0),
                                focusedContainerColor = if (isDarkMode) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = if (isDarkMode) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.background
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (manualCodeInput.isNotBlank()) {
                                    focusManager.clearFocus()
                                    currentVerifyCallback(manualCodeInput.trim())
                                    manualCodeInput = ""
                                }
                            },
                            enabled = manualCodeInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BluePrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(52.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Verify"
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // DEMO SAMPLES CHIPS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    "Quick Demo Test Samples",
                    color = textMutedColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SampleScans.all.forEach { sample ->
                        val label = sample.expectedOutcome.substringBefore(" ")
                        val chipColor = when {
                            label.contains("Valid", ignoreCase = true) -> ValidGreen
                            label.contains("Invalid", ignoreCase = true) -> RejectedRed
                            label.contains("Expired", ignoreCase = true) -> ExpiredOrange
                            else -> Color(0xFF9333EA)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(chipColor.copy(alpha = 0.15f))
                                .border(
                                    1.dp,
                                    chipColor.copy(alpha = 0.4f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    currentVerifyCallback(sample.ticket.ticketCode)
                                }
                                .padding(
                                    horizontal = 12.dp,
                                    vertical = 8.dp
                                )
                        ) {
                            Column {
                                Text(
                                    sample.ticket.ticketCode,
                                    color = textColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    sample.expectedOutcome,
                                    color = chipColor,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // BOTTOM NAVIGATION BAR
        BottomNavBar(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            isDark = isDarkMode,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    count: Int,
    color: Color,
    cardBg: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                count.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Spacer(Modifier.height(2.dp))

            Text(
                title,
                fontSize = 11.sp,
                color = textColor
            )
        }
    }
}

@Composable
private fun RecentScanMiniChip(
    item: ScanItem,
    isDarkMode: Boolean
) {
    val statusColor = when (item.status) {
        ScanStatus.VALID -> ValidGreen
        ScanStatus.EXPIRED -> ExpiredOrange
        ScanStatus.CANCELLED, ScanStatus.REJECTED -> RejectedRed
        ScanStatus.ALREADY_USED -> Color(0xFF8B5CF6)
        ScanStatus.PENDING -> PendingGray
    }

    val cardBg = if (isDarkMode) BlueCardBackground else MaterialTheme.colorScheme.surface
    val textColor = if (isDarkMode) TextLight else MaterialTheme.colorScheme.onSurface
    val textMutedColor = if (isDarkMode) TextLightMuted else TextMuted

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(Modifier.width(6.dp))
            Column {
                Text(
                    text = item.attendeeName,
                    color = textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = item.ticketCode,
                    color = textMutedColor,
                    fontSize = 9.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreviewView(
    isScanningEnabled: Boolean,
    onBarcodeDetected: (String) -> Unit,
    onCameraBound: (Camera) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentScanningState by rememberUpdatedState(isScanningEnabled)
    val currentBarcodeCallback by rememberUpdatedState(onBarcodeDetected)
    val currentCameraBoundCallback by rememberUpdatedState(onCameraBound)

    val barcodeScanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
        BarcodeScanning.getClient(options)
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            barcodeScanner.close()
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val selector = CameraSelector.DEFAULT_BACK_CAMERA

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(
                            ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                        )
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        if (!currentScanningState) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        val mediaImage = imageProxy.image
                        if (mediaImage == null) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        val inputImage = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        barcodeScanner.process(inputImage)
                            .addOnSuccessListener { barcodes ->
                                val code = barcodes.firstNotNullOfOrNull { barcode ->
                                    barcode.rawValue?.takeIf { value ->
                                        value.isNotBlank()
                                    }
                                }

                                if (code != null && currentScanningState) {
                                    previewView.post {
                                        currentBarcodeCallback(code)
                                    }
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    }

                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                        imageAnalysis
                    )

                    currentCameraBoundCallback(camera)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun ScannerReticleOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "scan_laser")

    val laserY by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .border(
                    2.dp,
                    BluePrimary.copy(alpha = 0.8f),
                    RoundedCornerShape(16.dp)
                )
        )

        Box(
            modifier = Modifier
                .size(190.dp)
                .padding(horizontal = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (200.dp.value * laserY).dp)
                    .background(BluePrimary)
            )
        }

        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(8.dp)
        ) {
            Text(
                "Point camera at ticket QR",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(
                        horizontal = 8.dp,
                        vertical = 2.dp
                    )
            )
        }
    }
}
