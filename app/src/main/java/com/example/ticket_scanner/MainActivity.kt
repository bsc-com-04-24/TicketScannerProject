@file:OptIn(ExperimentalGetImage::class)

package com.example.ticket_scanner

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TicketScannerScreen()
                }
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
        contract = ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        hasPermission = granted
    }

    var scannedCode by remember { mutableStateOf("") }
    var scanEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "EVENT TICKET SCANNER",
            style = MaterialTheme.typography.headlineSmall
        )

        Text("Point the camera at a ticket QR code or barcode.")

        if (!hasPermission) {
            Button(
                onClick = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            ) {
                Text("Allow Camera")
            }

            Text("Camera access is required to scan tickets.")
        } else {
            CameraScannerPreview(
                scanEnabled = scanEnabled,
                onCodeScanned = { code ->
                    scannedCode = code
                    scanEnabled = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Text(
                text = if (scannedCode.isBlank()) {
                    "Waiting for a ticket..."
                } else {
                    "Scanned ticket code:\n$scannedCode"
                },
                style = MaterialTheme.typography.bodyLarge
            )

            Button(
                onClick = {
                    scannedCode = ""
                    scanEnabled = true
                }
            ) {
                Text("Scan Another Ticket")
            }
        }
    }
}

@Composable
fun CameraScannerPreview(
    scanEnabled: Boolean,
    onCodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentScanEnabled by rememberUpdatedState(scanEnabled)
    val currentOnCodeScanned by rememberUpdatedState(onCodeScanned)

    val scanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
        BarcodeScanning.getClient(options)
    }

    val executor = remember { Executors.newSingleThreadExecutor() }
    val alreadyScanned = remember { AtomicBoolean(false) }

    LaunchedEffect(scanEnabled) {
        if (scanEnabled) {
            alreadyScanned.set(false)
        }
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

            val cameraProviderFuture = ProcessCameraProvider.getInstance(viewContext)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
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
                                val code = barcodes.firstNotNullOfOrNull { barcode ->
                                    barcode.rawValue?.takeIf { value ->
                                        value.isNotBlank()
                                    }
                                }

                                if (code != null &&
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
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
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
