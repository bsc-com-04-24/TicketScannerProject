
package com.example.ticket_scanner

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private val Navy = Color(0xFF0B1020)
private val CardNavy = Color(0xFF151D32)
private val Purple = Color(0xFF8065F7)
private val Blue = Color(0xFF398BFF)
private val White = Color(0xFFF5F6FF)
private val Muted = Color(0xFF9AA5BD)
private val Green = Color(0xFF35D49A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                TicketScannerScreen()
            }
        }
    }
}

@Composable
fun TicketScannerScreen() {
    val context = LocalContext.current

    var hasPermission by remember {
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
        hasPermission = granted
    }

    var scannedCode by remember { mutableStateOf("") }
    var scanEnabled by remember { mutableStateOf(true) }
    var torchEnabled by remember { mutableStateOf(false) }
    var showManualEntry by remember { mutableStateOf(false) }
    var manualTicketId by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("Scan") }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var totalScans by remember { mutableIntStateOf(0) }

    LaunchedEffect(torchEnabled, camera) {
        camera?.cameraControl?.enableTorch(torchEnabled)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .systemBarsPadding()
    ) {
        if (selectedTab == "Scan") {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(22.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Purple),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("QR", color = White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            "EVENT TICKET",
                            color = White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            "SCANNING SYSTEM",
                            color = Muted,
                            fontSize = 10.sp,
                            letterSpacing = 2.sp
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF193A37))
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Green)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("ONLINE", color = Green, fontSize = 10.sp)
                        }
                    }
                }

                Spacer(Modifier.height(26.dp))

                Text(
                    "Ticket Scanner",
                    color = White,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Scan QR codes and barcodes for event entry.",
                    color = Muted,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "TOTAL SCANS",
                        value = totalScans.toString(),
                        accent = Blue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "SCANNER",
                        value = if (hasPermission) "READY" else "OFF",
                        accent = Green,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "MODE",
                        value = "QR + BAR",
                        accent = Purple,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(22.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "SCAN TICKET",
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "AUTO DETECTION",
                        color = Green,
                        fontSize = 10.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF050914))
                        .border(
                            1.dp,
                            Color(0xFF29344E),
                            RoundedCornerShape(22.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasPermission) {
                        CameraScannerPreview(
                            scanEnabled = scanEnabled,
                            onCodeScanned = { code ->
                                scannedCode = code
                                scanEnabled = false
                                totalScans++
                            },
                            onCameraReady = { camera = it },
                            modifier = Modifier.fillMaxSize()
                        )

                        ScannerFrame(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(25.dp)
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                if (scanEnabled) {
                                    "Position the ticket inside the frame"
                                } else {
                                    "Ticket code detected"
                                },
                                color = White,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("CAMERA ACCESS REQUIRED", color = White)
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    permissionLauncher.launch(
                                        Manifest.permission.CAMERA
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Purple
                                )
                            ) {
                                Text("Allow Camera")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            torchEnabled = !torchEnabled
                        },
                        enabled = hasPermission,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = White
                        )
                    ) {
                        Text(if (torchEnabled) "Flash: ON" else "Flashlight")
                    }

                    Button(
                        onClick = { showManualEntry = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Purple
                        )
                    ) {
                        Text("Manual Entry", color = White)
                    }
                }

                Spacer(Modifier.height(14.dp))

                if (scannedCode.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = CardNavy
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(15.dp)) {
                            Text(
                                "DETECTED TICKET CODE",
                                color = Green,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(
                                scannedCode,
                                color = White,
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    scannedCode = ""
                                    scanEnabled = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Blue
                                )
                            ) {
                                Text("Scan Another Ticket")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    selectedTab,
                    color = White,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    when (selectedTab) {
                        "History" -> "Your scan history will appear here."
                        "Alerts" -> "Scanner alerts will appear here."
                        else -> "Scanner settings will appear here."
                    },
                    color = Muted
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "This section is a user-interface placeholder. " +
                            "Scan records and alerts are not stored yet.",
                    color = Muted,
                    fontSize = 12.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF10182A))
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Scan", "History", "Alerts", "Settings").forEach { tab ->
                TextButton(onClick = { selectedTab = tab }) {
                    Text(
                        tab,
                        color = if (selectedTab == tab) Purple else Muted,
                        fontWeight = if (selectedTab == tab) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        },
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    if (showManualEntry) {
        Dialog(onDismissRequest = { showManualEntry = false }) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = CardNavy
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(22.dp)) {
                    Text(
                        "Manual Ticket Entry",
                        color = White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Enter the ticket code manually.",
                        color = Muted,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = manualTicketId,
                        onValueChange = { manualTicketId = it },
                        label = { Text("Ticket ID") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (manualTicketId.isNotBlank()) {
                                scannedCode = manualTicketId.trim()
                                scanEnabled = false
                                totalScans++
                                showManualEntry = false
                                manualTicketId = ""
                            }
                        },
                        enabled = manualTicketId.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Purple
                        )
                    ) {
                        Text("Submit Ticket")
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardNavy
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                title,
                color = Muted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                color = accent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ScannerFrame(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val lineLength = 38.dp.toPx()
        val stroke = 4.dp.toPx()
        val inset = 8.dp.toPx()

        val corners = listOf(
            0f to 0f,
            size.width to 0f,
            0f to size.height,
            size.width to size.height
        )

        corners.forEachIndexed { index, point ->
            val x = point.first
            val y = point.second
            val xDirection = if (index % 2 == 0) 1f else -1f
            val yDirection = if (index < 2) 1f else -1f

            drawLine(
                Purple,
                androidx.compose.ui.geometry.Offset(
                    x + xDirection * inset,
                    y + yDirection * inset
                ),
                androidx.compose.ui.geometry.Offset(
                    x + xDirection * (inset + lineLength),
                    y + yDirection * inset
                ),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawLine(
                Blue,
                androidx.compose.ui.geometry.Offset(
                    x + xDirection * inset,
                    y + yDirection * inset
                ),
                androidx.compose.ui.geometry.Offset(
                    x + xDirection * inset,
                    y + yDirection * (inset + lineLength)
                ),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun CameraScannerPreview(
    scanEnabled: Boolean,
    onCodeScanned: (String) -> Unit,
    onCameraReady: (Camera) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentScanEnabled by rememberUpdatedState(scanEnabled)
    val currentOnCodeScanned by rememberUpdatedState(onCodeScanned)
    val currentOnCameraReady by rememberUpdatedState(onCameraReady)

    val scanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
        BarcodeScanning.getClient(options)
    }

    val executor = remember { Executors.newSingleThreadExecutor() }
    val alreadyScanned = remember { AtomicBoolean(false) }

    LaunchedEffect(scanEnabled) {
        if (scanEnabled) alreadyScanned.set(false)
    }

    DisposableEffect(Unit) {
        onDispose {
            scanner.close()
            executor.shutdown()
        }
    }

    AndroidView(
        factory = { viewContext ->
            val previewView = PreviewView(viewContext).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture =
                ProcessCameraProvider.getInstance(viewContext)

            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(
                            ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                        )
                        .build()

                    imageAnalysis.setAnalyzer(executor) { imageProxy ->
                        if (!currentScanEnabled || alreadyScanned.get()) {
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

                        scanner.process(inputImage)
                            .addOnSuccessListener { barcodes ->
                                val code = barcodes.firstNotNullOfOrNull {
                                    it.rawValue?.takeIf { value ->
                                        value.isNotBlank()
                                    }
                                }

                                if (
                                    code != null &&
                                    alreadyScanned.compareAndSet(false, true)
                                ) {
                                    previewView.post {
                                        currentOnCodeScanned(code)
                                    }
                                }
                            }
                            .addOnFailureListener { error ->
                                Log.e(
                                    "TicketScanner",
                                    "Barcode scan failed",
                                    error
                                )
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    }

                    cameraProvider.unbindAll()

                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )

                    currentOnCameraReady(camera)

                } catch (error: Exception) {
                    Log.e(
                        "TicketScanner",
                        "Unable to start camera",
                        error
                    )
                }
            }, ContextCompat.getMainExecutor(viewContext))

            previewView
        },
        modifier = modifier
    )
}
