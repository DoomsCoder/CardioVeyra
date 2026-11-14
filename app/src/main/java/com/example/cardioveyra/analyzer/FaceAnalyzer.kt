package com.example.cardioveyra.analyzer

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.cardioveyra.ui.screens.scan.ScanQualityState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlin.math.abs

class FaceAnalyzer(
    private val onStateChanged: (ScanQualityState) -> Unit,
    private val onRgbExtracted: (r: Double, g: Double, b: Double) -> Unit
) : ImageAnalysis.Analyzer {

    private val detectorOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .build()

    private val detector = FaceDetection.getClient(detectorOptions)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image

        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            // Extract byte array once for performance
            val buffer = imageProxy.planes[0].buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)

            val pixelStride = imageProxy.planes[0].pixelStride
            val rowStride = imageProxy.planes[0].rowStride

            // 1. Fast overall luminance check (checking every 10th pixel for speed)
            var totalLuminance = 0L
            var lumPixels = 0
            for (i in data.indices step 40) {
                // In RGBA_8888, the first 3 bytes are R, G, B
                val r = data[i].toInt() and 0xFF
                val g = data.getOrNull(i + 1)?.toInt()?.and(0xFF) ?: 0
                val b = data.getOrNull(i + 2)?.toInt()?.and(0xFF) ?: 0
                // Rough luminance formula
                totalLuminance += (r + g + b) / 3
                lumPixels++
            }
            val averageLuminance = if (lumPixels > 0) totalLuminance.toDouble() / lumPixels else 255.0

            // 2. ML Kit Face Detection
            detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isEmpty()) {
                        onStateChanged(ScanQualityState.FACE_LOST)
                    } else {
                        val face = faces.first()
                        val isMovingOrLookingAway = (abs(face.headEulerAngleY) > 12f) || (abs(face.headEulerAngleZ) > 12f)

                        // Determine state
                        val currentState = when {
                            averageLuminance < 50.0 -> ScanQualityState.POOR_LIGHTING
                            isMovingOrLookingAway -> ScanQualityState.MOTION_WARNING
                            else -> ScanQualityState.OPTIMAL
                        }

                        onStateChanged(currentState)

                        // 3. Extract Forehead ROI RGB if OPTIMAL
                        if (currentState == ScanQualityState.OPTIMAL) {
                            val bounds = face.boundingBox

                            // Define ROI: 40-60% width (center), 15-25% height (forehead)
                            val left = maxOf(0, bounds.left + (bounds.width() * 0.40f).toInt())
                            val right = minOf(imageProxy.width - 1, bounds.left + (bounds.width() * 0.60f).toInt())
                            val top = maxOf(0, bounds.top + (bounds.height() * 0.15f).toInt())
                            val bottom = minOf(imageProxy.height - 1, bounds.top + (bounds.height() * 0.25f).toInt())

                            var sumR = 0.0
                            var sumG = 0.0
                            var sumB = 0.0
                            var pixelCount = 0

                            // Step = 4 pixels to optimize processing speed
                            for (y in top..bottom step 4) {
                                for (x in left..right step 4) {
                                    val index = y * rowStride + x * pixelStride
                                    if (index + 2 < data.size) {
                                        sumR += data[index].toInt() and 0xFF
                                        sumG += data[index + 1].toInt() and 0xFF
                                        sumB += data[index + 2].toInt() and 0xFF
                                        pixelCount++
                                    }
                                }
                            }

                            if (pixelCount > 0) {
                                onRgbExtracted(
                                    sumR / pixelCount,
                                    sumG / pixelCount,
                                    sumB / pixelCount
                                )
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    onStateChanged(ScanQualityState.FACE_LOST)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}