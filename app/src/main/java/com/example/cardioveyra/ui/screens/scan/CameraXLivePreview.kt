package com.example.cardioveyra.ui.screens.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.cardioveyra.analyzer.FaceAnalyzer
import com.example.cardioveyra.core.rppg.SignalBuffer
import java.util.concurrent.Executors

@Composable
fun CameraXLivePreview(
    modifier: Modifier = Modifier,
    onStateChanged: (ScanQualityState) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    
    if (!hasCameraPermission) return
    
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    // Initialize the signal buffer here so it persists across recompositions
    val signalBuffer = remember { SignalBuffer(capacity = 450) } // 15 seconds at 30 FPS

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                // FILL_CENTER ensures the video feed fills your circular crop perfectly
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val executor = ContextCompat.getMainExecutor(ctx)
            val analyzerExecutor = Executors.newSingleThreadExecutor()

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                // 1. Set up the Preview Use Case
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888) // TASK 1: Force RGBA
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(analyzerExecutor, FaceAnalyzer(
                            onStateChanged = { newState ->
                                onStateChanged(newState)
                                // TASK 4: Wiring logic
                                // If the state is not OPTIMAL, clear the buffer so we don't feed garbage to the ML model
                                if (newState == ScanQualityState.FACE_LOST || newState == ScanQualityState.MOTION_WARNING) {
                                    signalBuffer.clear()
                                }
                            },
                            onRgbExtracted = { r, g, b ->
                                // Feed the clean spatial averages into our time-series buffer
                                signalBuffer.addSample(r, g, b)

                                if (signalBuffer.isFull()) {
                                    // Buffer is full! Time to run TFLite Inference!
                                    val rawTfLiteInput = signalBuffer.toFlatFloatArray()
                                    // TODO: Pass rawTfLiteInput to your model
                                }
                            }
                        ))
                    }

                // 2. Select Front Camera (Crucial for rPPG)
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                try {
                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll()

                    // Bind use cases to camera
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis,
                        // NOTE: Later, you will also bind your ImageAnalysis use case here for rPPG!
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, executor)

            previewView
        },
        update = {
            // Can be left empty for now
        },
        onRelease = {
            // Explicitly unbind the camera when the AndroidView is removed from the screen
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    )
}