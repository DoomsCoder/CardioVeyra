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
            // ML Kit can directly accept YUV_420_888
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            // 1. Fast overall luminance check using only the Y plane (Luma)
            val yBuffer = imageProxy.planes[0].buffer
            val yData = ByteArray(yBuffer.remaining())
            yBuffer.get(yData)
            yBuffer.rewind() // Rewind in case it's needed again

            var totalLuminance = 0L
            var lumPixels = 0
            for (i in yData.indices step 40) {
                totalLuminance += yData[i].toInt() and 0xFF
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

                            val yPlane = imageProxy.planes[0]
                            val uPlane = imageProxy.planes[1]
                            val vPlane = imageProxy.planes[2]

                            val yBuffer = yPlane.buffer
                            val uBuffer = uPlane.buffer
                            val vBuffer = vPlane.buffer

                            val yRowStride = yPlane.rowStride
                            val yPixelStride = yPlane.pixelStride
                            val uvRowStride = uPlane.rowStride
                            val uvPixelStride = uPlane.pixelStride

                            // Step = 4 pixels to optimize processing speed
                            for (y in top..bottom step 4) {
                                for (x in left..right step 4) {
                                    val yIndex = y * yRowStride + x * yPixelStride
                                    // U and V planes are typically subsampled by a factor of 2 in both dimensions (YUV 4:2:0)
                                    val uvIndex = (y / 2) * uvRowStride + (x / 2) * uvPixelStride

                                    if (yIndex < yBuffer.capacity() && uvIndex < uBuffer.capacity() && uvIndex < vBuffer.capacity()) {
                                        val yVal = (yBuffer.get(yIndex).toInt() and 0xFF)
                                        // U and V are centered around 128
                                        val uVal = (uBuffer.get(uvIndex).toInt() and 0xFF) - 128
                                        val vVal = (vBuffer.get(uvIndex).toInt() and 0xFF) - 128

                                        // Standard YUV to RGB conversion formulas
                                        val r = (yVal + 1.370705f * vVal).toInt().coerceIn(0, 255)
                                        val g = (yVal - 0.337633f * uVal - 0.698001f * vVal).toInt().coerceIn(0, 255)
                                        val b = (yVal + 1.732446f * uVal).toInt().coerceIn(0, 255)

                                        sumR += r
                                        sumG += g
                                        sumB += b
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