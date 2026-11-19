package com.example.cardioveyra.core.rppg

import com.example.cardioveyra.analyzer.FaceRegion
import com.example.cardioveyra.analyzer.RegionRgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class PulseScanTest {
    @Test
    fun reportsLiveWaveformAndFinalBpmForTimestampedRgb() {
        val scan = PulseScan()
        trace(scan, 0.0, 13.0)
        val live = checkNotNull(scan.live)
        assertTrue(abs(live.bpm - 72) <= 2)
        assertEquals(270, live.waveform.size)
        assertTrue(checkNotNull(live.sdnnMs) < 20)
        assertTrue(checkNotNull(live.rmssdMs) < 20)
        trace(scan, 13.02, 32.0)
        val result = checkNotNull(scan.result(32))
        assertTrue(abs(checkNotNull(result.bpm) - 72) <= 2)
        assertEquals(32, result.elapsedSeconds)
        assertEquals("High", result.quality)
    }

    @Test
    fun switchingRegionsOrLosingTheFaceBreaksTheSignalWindow() {
        val scan = PulseScan()
        trace(scan, 0.0, 7.0)
        trace(scan, 7.02, 14.0, FaceRegion.LEFT_CHEEK)
        assertNull(scan.live)
        scan.add(14_100_000_000L, emptyMap())
        val progress = scan.progress
        trace(scan, 20.0, 26.0, FaceRegion.LEFT_CHEEK)
        assertNull(scan.live)
        assertTrue(scan.progress - progress < 0.21f)
        trace(scan, 26.02, 32.0, FaceRegion.LEFT_CHEEK)
        assertTrue(abs(checkNotNull(scan.live).bpm - 72) <= 2)
    }

    @Test
    fun rejectsIlluminationChangesAndInconsistentWindows() {
        val scan = PulseScan()
        trace(scan, 0.0, 31.0, bpm = 0)
        assertNull(scan.live)
        assertNull(scan.result(59))
        assertNull(checkNotNull(scan.result(60)).bpm)

        val inconsistent = PulseScan()
        trace(inconsistent, 0.0, 12.0)
        inconsistent.interrupt()
        trace(inconsistent, 13.0, 26.0, bpm = 108)
        assertNull(checkNotNull(inconsistent.result(60)).bpm)
    }

    private fun trace(
        scan: PulseScan, start: Double, end: Double,
        region: FaceRegion = FaceRegion.FOREHEAD, bpm: Int = 72
    ) {
        var time = (start * 1e9).toLong()
        while (time <= end * 1e9) {
            val seconds = time / 1e9
            val light = 1 + 0.03 * sin(2 * PI * 0.18 * seconds)
            val sample = sin(2 * PI * bpm / 60 * seconds)
            scan.add(time, mapOf(region to RegionRgb(
                130 * light * (1 + 0.003 * sample),
                105 * light * (1 + 0.012 * sample),
                80 * light * (1 + 0.004 * sample)
            )))
            time += 41_000_000 + (time / 1_000_000 % 7) * 1_000_000
        }
    }
}
