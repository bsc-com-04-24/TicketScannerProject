
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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.OfflinePin
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
import com.example.ticket_scanner.model.ScanStats
import com.example.ticket_scanner.ui.components.BottomNavBar
import com.example.ticket_scanner.ui.components.NavTab
import com.example.ticket_scanner.ui.theme.BlueCardBackground
import com.example.ticket_scanner.ui.theme.BlueDarkBackground
import com.example.ticket_scanner.ui.theme.BluePrimary
import com.example.ticket_scanner.ui.theme.RejectedRed
import com.example.ticket_scanner.ui.theme.TextLight
import com.example.ticket_scanner.ui.theme.TextLightMuted
import com.example.ticket_scanner.ui.theme.ValidGreen
import com.example.ticket_scanner.validation.data.SampleScans
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(
    stats: ScanStats,
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    onVerifyTicket: (String) -> Unit,
    modifier: Modifier = Modifier,
    onOpenOfflineMode: () -> Unit = {}
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

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

    // Always use the latest verification callback.
    val currentVerifyCallback by rememberUpdatedState(onVerifyTicket)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BlueDarkBackground)
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
                        color = TextLight,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ONLINE STATUS
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.1f))
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
                            color = TextLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // FLASHLIGHT
                    IconButton(
                        onClick = {
                            isFlashOn = !isFlashOn
                            cameraControlRef?.cameraControl
                                ?.enableTorch(isFlashOn)
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                if (isFlashOn) BluePrimary
                                else Color.White.copy(alpha = 0.12f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            if (isFlashOn) Icons.Default.FlashOn
                            else Icons.Default.FlashOff,
                            contentDescription = "Toggle flashlight",
                            tint = Color.White
                        )
                    }
                }
            }

            // OFFLINE MODE NAVIGATION
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onOpenOfflineMode() },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF173A4D)
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
                            color = TextLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Scan tickets without an internet connection",
                            color = TextLightMuted,
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

            Spacer(Modifier.height(16.dp))

            // STATISTICS
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
                    Modifier.weight(1f)
                )

                StatCard(
                    "Valid",
                    stats.validCount,
                    ValidGreen,
                    Modifier.weight(1f)
                )

                StatCard(
                    "Rejected",
                    stats.rejectedCount,
                    RejectedRed,
                    Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(18.dp))

            // CAMERA VIEWFINDER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(290.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(BlueCardBackground),
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

                    if (!isScanningEnabled) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.55f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Ticket captured!",
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
                            "Camera Permission Needed",
                            color = TextLight,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            "Camera access is required to scan QR codes.",
                            color = TextLightMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(16.dp))

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

            Spacer(Modifier.height(18.dp))

            // MANUAL TICKET ENTRY
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BlueCardBackground
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Manual Ticket Entry",
                        color = TextLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        "Enter ticket ID if barcode is damaged or unreadable",
                        color = TextLightMuted,
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
                                    color = TextLightMuted,
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
                                        currentVerifyCallback(
                                            manualCodeInput.trim()
                                        )
                                        manualCodeInput = ""
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextLight,
                                unfocusedTextColor = TextLight,
                                focusedBorderColor = BluePrimary,
                                unfocusedBorderColor =
                                    Color.White.copy(alpha = 0.15f),
                                focusedContainerColor =
                                    Color.White.copy(alpha = 0.05f),
                                unfocusedContainerColor =
                                    Color.White.copy(alpha = 0.05f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (manualCodeInput.isNotBlank()) {
                                    focusManager.clearFocus()
                                    currentVerifyCallback(
                                        manualCodeInput.trim()
                                    )
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

            // DEMO SAMPLES
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    "Quick Demo Test Samples",
                    color = TextLightMuted,
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

                        val label =
                            sample.expectedOutcome.substringBefore(" ")

                        val chipColor = when {
                            label.contains(
                                "Valid",
                                ignoreCase = true
                            ) -> ValidGreen

                            label.contains(
                                "Invalid",
                                ignoreCase = true
                            ) -> RejectedRed

                            label.contains(
                                "Expired",
                                ignoreCase = true
                            ) -> Color(0xFFF59E0B)

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
                                    currentVerifyCallback(
                                        sample.ticket.ticketCode
                                    )
                                }
                                .padding(
                                    horizontal = 12.dp,
                                    vertical = 8.dp
                                )
                        ) {
                            Column {
                                Text(
                                    sample.ticket.ticketCode,
                                    color = Color.White,
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

        // BOTTOM NAVIGATION
        BottomNavBar(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            isDark = true,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// --------------------------------------------------
// STATISTICS CARD
// --------------------------------------------------

@Composable
private fun StatCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = BlueCardBackground
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 10.dp),
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
                color = TextLightMuted,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// --------------------------------------------------
// CAMERA PREVIEW + ML KIT QR SCANNER
// --------------------------------------------------

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreviewView(
    isScanningEnabled: Boolean,
    onBarcodeDetected: (String) -> Unit,
    onCameraBound: (Camera) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Keep callbacks and scanning state updated without
    // recreating the camera analyzer on every recomposition.
    val currentScanningState by rememberUpdatedState(isScanningEnabled)
    val currentBarcodeCallback by rememberUpdatedState(onBarcodeDetected)
    val currentCameraCallback by rememberUpdatedState(onCameraBound)

    val cameraExecutor = remember {
        Executors.newSingleThreadExecutor()
    }

    val barcodeScanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_DATA_MATRIX
            )
            .build()

        BarcodeScanning.getClient(options)
    }

    var cameraProvider by remember {
        mutableStateOf<ProcessCameraProvider?>(null)
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            cameraProvider?.unbindAll()
            barcodeScanner.close()
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->

            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture =
                ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({

                val provider = cameraProviderFuture.get()
                cameraProvider = provider

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(
                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                    )
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->

                    val mediaImage = imageProxy.image

                    if (mediaImage == null ||
                        !currentScanningState
                    ) {
                        imageProxy.close()
                    } else {

                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        barcodeScanner.process(image)
                            .addOnSuccessListener { barcodes ->

                                if (currentScanningState) {
                                    for (barcode in barcodes) {

                                        val rawValue = barcode.rawValue

                                        if (!rawValue.isNullOrBlank()) {
                                            currentBarcodeCallback(rawValue)
                                            break
                                        }
                                    }
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    }
                }

                try {
                    provider.unbindAll()

                    val camera = provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )

                    currentCameraCallback(camera)

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

// --------------------------------------------------
// ANIMATED SCANNER RETICLE
// --------------------------------------------------

@Composable
private fun ScannerReticleOverlay() {

    val infiniteTransition =
        rememberInfiniteTransition(label = "scan_laser")

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
                .padding(horizontal = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (190 * laserY).dp)
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
