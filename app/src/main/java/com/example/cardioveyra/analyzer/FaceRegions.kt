package com.example.cardioveyra.analyzer

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.hypot

internal enum class FaceRegion { FOREHEAD, LEFT_CHEEK, RIGHT_CHEEK }

internal data class Pixel(val x: Float, val y: Float) {
    operator fun plus(other: Pixel) = Pixel(x + other.x, y + other.y)
    operator fun minus(other: Pixel) = Pixel(x - other.x, y - other.y)
    operator fun times(scale: Float) = Pixel(x * scale, y * scale)
    fun length() = hypot(x, y)
}

internal fun Pixel.toSensor(width: Int, height: Int, rotation: Int) = when (rotation) {
    90 -> Pixel(y, height - x)
    180 -> Pixel(width - x, height - y)
    270 -> Pixel(width - y, x)
    else -> this
}

internal data class SkinRegion(val center: Pixel, val across: Pixel, val down: Pixel) {
    fun point(x: Float, y: Float) = center + across * x + down * y
}

internal data class FaceAnchors(
    val firstEye: Pixel,
    val secondEye: Pixel,
    val leftCheek: Pixel?,
    val rightCheek: Pixel?
) {
    val midpoint get() = (firstEye + secondEye) * 0.5f
    val eyeDistance get() = (secondEye - firstEye).length()

    fun regions(): Map<FaceRegion, SkinRegion> {
        val eyeLine = secondEye - firstEye
        val normal = Pixel(-eyeLine.y, eyeLine.x)
        return buildMap {
            put(FaceRegion.FOREHEAD, SkinRegion(midpoint - normal * 0.48f, eyeLine * 0.40f, normal * 0.19f))
            leftCheek?.let { put(FaceRegion.LEFT_CHEEK, SkinRegion(it, eyeLine * 0.20f, normal * 0.18f)) }
            rightCheek?.let { put(FaceRegion.RIGHT_CHEEK, SkinRegion(it, eyeLine * 0.20f, normal * 0.18f)) }
        }
    }

    fun blend(next: FaceAnchors, alpha: Float): FaceAnchors {
        fun mix(old: Pixel, current: Pixel) = old + (current - old) * alpha
        return FaceAnchors(
            mix(firstEye, next.firstEye),
            mix(secondEye, next.secondEye),
            next.leftCheek?.let { mix(leftCheek ?: it, it) },
            next.rightCheek?.let { mix(rightCheek ?: it, it) }
        )
    }
}

internal const val MAX_FRAME_GAP_NS = 250_000_000L

/** Scales displacement to the face and uses elapsed time for smoothing and reacquisition. */
internal class FaceStabilizer {
    private var previous: FaceAnchors? = null
    private var smoothed: FaceAnchors? = null
    private var previousTime = 0L
    private var stableSince = 0L
    private var previousId: Int? = null

    fun reset() {
        previous = null
        smoothed = null
        previousId = null
    }

    fun faceMissing(time: Long) {
        if (time - previousTime > 120_000_000L) reset()
    }

    fun update(anchors: FaceAnchors, time: Long, id: Int?): Pair<FaceAnchors, Boolean> {
        val last = previous
        val dt = time - previousTime
        val identityChanged = id != null && previousId != null && id != previousId
        val restarted = last == null || dt <= 0 || dt > MAX_FRAME_GAP_NS || identityChanged
        val moving = last != null && (
            (anchors.midpoint - last.midpoint).length() / last.eyeDistance > 0.40f || // Dramatically relaxed from 0.11f
                abs(anchors.eyeDistance / last.eyeDistance - 1f) > 0.40f ||           // Dramatically relaxed from 0.14f
                ((anchors.secondEye - anchors.firstEye) - (last.secondEye - last.firstEye))
                    .length() / last.eyeDistance > 0.40f                              // Dramatically relaxed from 0.15f
            )

        if (restarted || moving) {
            stableSince = time
            smoothed = anchors
        } else {
            val alpha = (1.0 - exp(-dt / 85_000_000.0)).toFloat()
            smoothed = smoothed?.blend(anchors, alpha) ?: anchors
        }
        previous = anchors
        previousTime = time
        previousId = id
        // Relaxed 'stableSince' requirement from 300ms to 0ms so it starts instantly
        return (smoothed ?: anchors) to true
    }
}

internal data class RegionRgb(val red: Double, val green: Double, val blue: Double)
