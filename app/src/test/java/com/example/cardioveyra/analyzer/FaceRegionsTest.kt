package com.example.cardioveyra.analyzer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FaceRegionsTest {
    @Test
    fun mapsUprightLandmarksToTheSameSensorPixelAtEveryRotation() {
        val expected = Pixel(100f, 200f)
        assertEquals(expected, Pixel(100f, 200f).toSensor(640, 480, 0))
        assertEquals(expected, Pixel(280f, 100f).toSensor(640, 480, 90))
        assertEquals(expected, Pixel(540f, 280f).toSensor(640, 480, 180))
        assertEquals(expected, Pixel(200f, 540f).toSensor(640, 480, 270))
    }

    @Test
    fun stabilizerRequiresFreshSamplesAfterMotionGapOrSubjectChange() {
        val tracker = FaceStabilizer()
        val anchors = FaceAnchors(Pixel(25f, 50f), Pixel(75f, 50f), null, null)

        fun stable(ms: Long, id: Int = 1, shift: Float = 0f): Boolean {
            val translation = Pixel(shift, 0f)
            val current = anchors.copy(
                firstEye = anchors.firstEye + translation,
                secondEye = anchors.secondEye + translation
            )
            return tracker.update(current, ms * 1_000_000L, id).second
        }

        assertFalse(stable(0))
        assertFalse(stable(150))
        assertTrue(stable(300))
        assertFalse(stable(330, shift = 10f))
        assertFalse(stable(490, shift = 10f))
        assertTrue(stable(650, shift = 10f))
        assertFalse(stable(950, shift = 10f))
        assertFalse(stable(1100, shift = 10f))
        assertTrue(stable(1280, shift = 10f))
        assertFalse(stable(1310, id = 2, shift = 10f))
    }
}
