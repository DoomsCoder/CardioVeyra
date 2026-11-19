package com.example.cardioveyra.analyzer

import android.graphics.Rect
import android.graphics.RectF
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.TransformExperimental
import androidx.camera.view.transform.ImageProxyTransformFactory
import androidx.camera.view.transform.OutputTransform
import com.example.cardioveyra.ui.screens.scan.ScanQualityState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import java.util.concurrent.Executor
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(TransformExperimental::class)
internal data class FaceFrame(
    val timestampNs: Long,
    val quality: ScanQualityState,
    val transform: OutputTransform? = null,
    val bounds: RectF? = null,
    val outlines: Map<FaceRegion, FloatArray> = emptyMap(),
    val rgb: Map<FaceRegion, RegionRgb> = emptyMap()
)

@OptIn(ExperimentalGetImage::class, TransformExperimental::class)
internal class FaceAnalyzer(private val onFrame: (FaceFrame) -> Unit) : ImageAnalysis.Analyzer, AutoCloseable {
    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .enableTracking()
            .build()
    )
    private val transformFactory = ImageProxyTransformFactory().apply { isUsingRotationDegrees = true }
    private val stabilizer = FaceStabilizer()
    private val detectorLock = Any()
    @Volatile private var closed = false
    private var lastRotation: Int? = null

    override fun analyze(image: ImageProxy) {
        val timestamp = image.imageInfo.timestamp
        val rotation = image.imageInfo.rotationDegrees
        try {
            val sampler = YuvSampler(image)
            synchronized(detectorLock) {
                val mediaImage = image.image
                if (closed || mediaImage == null) {
                    image.close()
                    return
                }
                detector.process(sampler.detectionImage() ?: InputImage.fromMediaImage(mediaImage, rotation))
            }.addOnCompleteListener(Executor { it.run() }) { task ->
                val result = try {
                    if (closed) null else if (task.isSuccessful) {
                        if (rotation != lastRotation) stabilizer.reset()
                        lastRotation = rotation
                        evaluate(image, sampler, task.result)
                    } else {
                        reset(timestamp, ScanQualityState.CAMERA_ERROR)
                    }
                } catch (_: RuntimeException) {
                    reset(timestamp, ScanQualityState.CAMERA_ERROR)
                }
                try {
                    if (!closed && result != null) onFrame(result)
                } finally {
                    image.close()
                }
            }
        } catch (_: RuntimeException) {
            try {
                if (!closed) onFrame(reset(timestamp, ScanQualityState.CAMERA_ERROR))
            } finally {
                image.close()
            }
        }
    }

    override fun close() {
        synchronized(detectorLock) {
            if (!closed) {
                closed = true
                detector.close()
            }
        }
    }

    private fun reset(timestamp: Long, quality: ScanQualityState): FaceFrame {
        stabilizer.reset()
        return FaceFrame(timestamp, quality)
    }

    private fun evaluate(image: ImageProxy, sampler: YuvSampler, faces: List<Face>): FaceFrame {
        val time = image.imageInfo.timestamp
        if (faces.isEmpty()) {
            stabilizer.faceMissing(time)
            return FaceFrame(time, ScanQualityState.FACE_LOST)
        }
        if (faces.size != 1) {
            return reset(time, ScanQualityState.MULTIPLE_FACES)
        }
        val face = faces.single()
        val transform = transformFactory.getOutputTransform(image)
        val bounds = RectF(face.boundingBox)

        fun reject(quality: ScanQualityState): FaceFrame {
            stabilizer.reset()
            return FaceFrame(time, quality, transform, bounds)
        }

        if (abs(face.headEulerAngleY) > 25f || abs(face.headEulerAngleX) > 25f || abs(face.headEulerAngleZ) > 22f) {
            return reject(ScanQualityState.ALIGN_FACE)
        }
        val leftEye = face.point(FaceLandmark.LEFT_EYE) ?: return reject(ScanQualityState.ALIGN_FACE)
        val rightEye = face.point(FaceLandmark.RIGHT_EYE) ?: return reject(ScanQualityState.ALIGN_FACE)
        val (firstEye, secondEye) = if (leftEye.x < rightEye.x) leftEye to rightEye else rightEye to leftEye
        val anchors = FaceAnchors(
            firstEye, secondEye,
            face.point(FaceLandmark.LEFT_CHEEK),
            face.point(FaceLandmark.RIGHT_CHEEK)
        )
        if (anchors.eyeDistance < 40f || !sampler.hasMargin(anchors.midpoint)) {
            return reject(ScanQualityState.ALIGN_FACE)
        }
        val (tracked, stable) = stabilizer.update(anchors, time, face.trackingId)
        val regions = tracked.regions().filterValues { sampler.hasRegion(it) }
        val outlines = regions.mapValues { (_, region) ->
            FloatArray(OUTLINE.size * 2).also { points ->
                for ((i, unit) in OUTLINE.withIndex()) {
                    val point = region.point(unit.x, unit.y)
                    points[i * 2] = point.x
                    points[i * 2 + 1] = point.y
                }
            }
        }
        val rgb = regions.mapNotNull { (name, region) ->
            sampler.mean(region)?.let { name to it }
        }.toMap()
        val state = when {
            regions.isEmpty() -> ScanQualityState.ALIGN_FACE
            rgb.isEmpty() -> ScanQualityState.POOR_LIGHTING
            !stable -> ScanQualityState.MOTION_WARNING
            else -> ScanQualityState.OPTIMAL
        }
        return FaceFrame(time, state, transform, bounds, outlines, if (stable) rgb else emptyMap())
    }

    private fun Face.point(which: Int) = getLandmark(which)?.position?.let { Pixel(it.x, it.y) }

    companion object {
        private val OUTLINE = List(24) { index ->
            val angle = index * 2.0 * Math.PI / 24
            Pixel(cos(angle).toFloat(), sin(angle).toFloat())
        }
    }
}

private class YuvSampler(image: ImageProxy) {
    private val width = image.width
    private val height = image.height
    private val rotation = image.imageInfo.rotationDegrees
    private val crop = Rect(image.cropRect)
    private val y = Plane(image.planes[0])
    private val u = Plane(image.planes[1])
    private val v = Plane(image.planes[2])

    // Improve only the detector input. ROI colors come from the original sensor planes.
    fun detectionImage(): InputImage? {
        if (width % 2 != 0 || height % 2 != 0) return null
        val levels = IntArray(256)
        var samples = 0
        for (row in height / 4 until height * 3 / 4 step 12) {
            for (column in width / 4 until width * 3 / 4 step 12) {
                val value = y.get(column, row) ?: continue
                levels[value]++
                samples++
            }
        }
        if (samples == 0) return null
        val target = (samples * 7 + 9) / 10
        var cumulative = 0
        val level = levels.indices.first { cumulative += levels[it]; cumulative >= target }
        if (level !in 24..92) return null
        val lut = if (level < 55) DARK_LUMA else DIM_LUMA
        val pixels = ByteArray(width * height * 3 / 2)
        var index = 0
        for (row in 0 until height) {
            for (column in 0 until width) pixels[index++] = lut[y.get(column, row) ?: return null]
        }
        for (row in 0 until height / 2) {
            for (column in 0 until width / 2) {
                pixels[index++] = (v.get(column, row) ?: return null).toByte()
                pixels[index++] = (u.get(column, row) ?: return null).toByte()
            }
        }
        return InputImage.fromByteArray(pixels, width, height, rotation, InputImage.IMAGE_FORMAT_NV21)
    }

    private companion object {
        val DARK_LUMA = lumaLookup(0.62)
        val DIM_LUMA = lumaLookup(0.80)
        fun lumaLookup(gamma: Double) = ByteArray(256) {
            (16 + 219 * ((it - 16) / 219.0).coerceIn(0.0, 1.0).pow(gamma)).roundToInt().toByte()
        }
    }

    private fun raw(point: Pixel) = point.toSensor(width, height, rotation)

    fun hasMargin(point: Pixel): Boolean {
        val center = raw(point)
        return abs(center.x - crop.exactCenterX()) < crop.width() * 0.35f &&
            abs(center.y - crop.exactCenterY()) < crop.height() * 0.35f
    }

    fun hasRegion(region: SkinRegion): Boolean = listOf(
        region.point(-1f, -1f), region.point(1f, -1f),
        region.point(-1f, 1f), region.point(1f, 1f)
    ).all {
        val point = raw(it)
        point.x >= crop.left && point.x < crop.right && point.y >= crop.top && point.y < crop.bottom
    }

    fun mean(region: SkinRegion): RegionRgb? {
        var red = 0.0
        var green = 0.0
        var blue = 0.0
        var luminance = 0.0
        var clipped = 0
        var count = 0
        val center = raw(region.center)
        val across = raw(region.center + region.across) - center
        val down = raw(region.center + region.down) - center
        for (dy in -7..7) {
            for (dx in -7..7) {
                if (dx * dx + dy * dy > 49) continue
                val x = (center.x + (across.x * dx + down.x * dy) / 7).toInt()
                val row = (center.y + (across.y * dx + down.y * dy) / 7).toInt()
                val luma = y.get(x, row) ?: continue
                val cb = (u.get(x / 2, row / 2) ?: continue) - 128
                val cr = (v.get(x / 2, row / 2) ?: continue) - 128
                val l = (luma - 16) * 1.164
                red += (l + 1.596 * cr).coerceIn(0.0, 255.0)
                green += (l - 0.392 * cb - 0.813 * cr).coerceIn(0.0, 255.0)
                blue += (l + 2.017 * cb).coerceIn(0.0, 255.0)
                luminance += luma
                if (luma <= 20 || luma >= 232) clipped++
                count++
            }
        }
        if (count < 10 || luminance / count < 15) return null // Massively relaxed lighting requirements for demo
        return RegionRgb(red / count, green / count, blue / count)
    }

    private class Plane(plane: ImageProxy.PlaneProxy) {
        private val buffer = plane.buffer.duplicate()
        private val offset = buffer.position()
        private val rowStride = plane.rowStride
        private val pixelStride = plane.pixelStride

        fun get(x: Int, y: Int): Int? {
            val index = offset + y * rowStride + x * pixelStride
            return if (index in offset until buffer.limit()) buffer.get(index).toInt() and 0xff else null
        }
    }
}
