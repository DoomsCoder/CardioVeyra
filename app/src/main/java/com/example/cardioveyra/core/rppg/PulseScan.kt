package com.example.cardioveyra.core.rppg

import com.example.cardioveyra.analyzer.FaceRegion
import com.example.cardioveyra.analyzer.RegionRgb
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

internal data class PulseReading(
    val bpm: Int,
    val quality: Double,
    val waveform: List<Float>,
    // Empty if this window has a missed or irregular beat. Used only for offline evaluation.
    val beatIntervalsMs: List<Double>
) {
    val sdnnMs: Double? get() {
        if (beatIntervalsMs.size < 2) return null
        val mean = beatIntervalsMs.average()
        return sqrt(beatIntervalsMs.sumOf { (it - mean) * (it - mean) } / (beatIntervalsMs.size - 1))
    }
    val rmssdMs: Double? get() {
        if (beatIntervalsMs.size < 2) return null
        return sqrt(beatIntervalsMs.zipWithNext { a, b -> (b - a) * (b - a) }.average())
    }
}

internal data class PulseResult(val bpm: Int?, val elapsedSeconds: Int, val quality: String)
private data class Sample(val time: Long, val rgb: RegionRgb)

// WINDOW and COUNT MUST describe the same duration (COUNT = seconds * RATE).
private const val WINDOW_SECONDS = 10
private const val WINDOW_NS = WINDOW_SECONDS * 1_000_000_000L
private const val RATE = 30
private const val COUNT = WINDOW_SECONDS * RATE   // 300
private const val SETTLE = 30                     // filter warm-up samples to discard

internal class PulseScan {
    private val tracks = FaceRegion.entries.associateWith { ArrayDeque<Sample>() }
    private var previousTime: Long? = null
    private var previousRegions = emptySet<FaceRegion>()
    private var lastAttempt: Long? = null
    private var lastAccepted: Long? = null
    private val accepted = mutableListOf<PulseReading>()
    private var usable = 0L
    var live: PulseReading? = null
        private set
    val progress get() = (usable / 60_000_000_000f).coerceIn(0f, 1f)

    fun interrupt() {
        tracks.values.forEach { it.clear() }
        previousTime = null
        previousRegions = emptySet()
        lastAttempt = null
        live = null
    }

    fun add(time: Long, rgb: Map<FaceRegion, RegionRgb>) {
        if (rgb.isEmpty()) {
            interrupt()
            return
        }
        val dt = previousTime?.let { time - it }
        if (dt != null && (dt <= 0 || dt > 500_000_000L)) interrupt()
        if (previousTime != null && dt != null && previousRegions.any { it in rgb }) usable += dt
        previousTime = time
        previousRegions = rgb.keys
        for ((region, samples) in tracks) {
            val color = rgb[region]
            if (color == null) {
                samples.clear()
                continue
            }
            samples.addLast(Sample(time, color))
            while (samples.size > 2 && samples[1].time <= time - WINDOW_NS) samples.removeFirst()
        }
        if (lastAttempt?.let { time - it < 500_000_000L } == true) return
        lastAttempt = time
        live = tracks.values.mapNotNull { estimate(it, time) }.maxByOrNull { it.quality }
        live?.let { reading ->
            if (lastAccepted?.let { time - it >= 1_000_000_000L } != false) {
                accepted.add(reading)
                lastAccepted = time
            }
        }
    }

    fun result(elapsedSeconds: Int): PulseResult? {
        if (elapsedSeconds < 60 && progress < 1f) return null

        // Honest failure: no valid pulse window at all -> no number.
        if (accepted.isEmpty()) return PulseResult(null, elapsedSeconds, "INSUFFICIENT")

        val center = median(accepted.map { it.bpm.toDouble() })
        val agreeing = accepted.filter { abs(it.bpm - center) <= maxOf(12.0, center * 0.15) }
        val finalBpm = if (agreeing.isNotEmpty()) {
            median(agreeing.map { it.bpm.toDouble() }).roundToInt()
        } else {
            center.roundToInt()
        }
        val agreement = agreeing.size.toDouble() / accepted.size
        val quality = when {
            accepted.size >= 8 && agreement >= 0.6 -> "OPTIMAL"
            else -> "MODERATE"
        }
        return PulseResult(finalBpm, elapsedSeconds, quality)
    }
}

private fun estimate(samples: List<Sample>, end: Long): PulseReading? {
    val start = end - WINDOW_NS
    if (samples.size < 75 || samples.first().time > start) return null
    if ((1 until samples.size).any { samples[it].time - samples[it - 1].time > 500_000_000L }) return null

    // Resample to a uniform 30 Hz grid covering exactly the window.
    val channels = Array(3) { DoubleArray(COUNT) }
    var cursor = 0
    for (i in 0 until COUNT) {
        val time = start + i * 1_000_000_000L / RATE
        while (cursor + 1 < samples.size && samples[cursor + 1].time < time) cursor++
        if (cursor + 1 >= samples.size) return null   // never leave silent zeros
        val a = samples[cursor]
        val b = samples[cursor + 1]
        val span = (b.time - a.time).toDouble()
        if (span <= 0.0) return null
        val weight = ((time - a.time).toDouble() / span).coerceIn(0.0, 1.0)
        channels[0][i] = a.rgb.red + (b.rgb.red - a.rgb.red) * weight
        channels[1][i] = a.rgb.green + (b.rgb.green - a.rgb.green) * weight
        channels[2][i] = a.rgb.blue + (b.rgb.blue - a.rgb.blue) * weight
    }

    // POS (plane-orthogonal-to-skin) with overlap-add.
    val pos = DoubleArray(COUNT)
    val coverage = IntArray(COUNT)
    val length = 48
    for (offset in 0..COUNT - length) {
        val means = channels.map { channel -> (offset until offset + length).sumOf { channel[it] } / length }
        if (means.any { it < 1.0 }) continue
        val x = DoubleArray(length)
        val y = DoubleArray(length)
        for (i in 0 until length) {
            val r = channels[0][offset + i] / means[0]
            val g = channels[1][offset + i] / means[1]
            val b = channels[2][offset + i] / means[2]
            x[i] = g - b
            y[i] = g + b - 2 * r
        }
        val denominator = rms(y)
        if (denominator < 1e-8) continue
        val scale = rms(x) / denominator
        val mean = (x.indices).sumOf { x[it] + scale * y[it] } / length
        for (i in x.indices) {
            pos[offset + i] += x[i] + scale * y[i] - mean
            coverage[offset + i]++
        }
    }
    if (coverage.any { it == 0 }) return null
    for (i in pos.indices) pos[i] /= coverage[i]

    // Butterworth bandpass 0.7-4 Hz @ 30 Hz as two biquad sections.
    val filtered = section(
        section(pos, 0.07909371811698926, 0.15818743623397852, 0.07909371811698926,
            -1.0578730338698499, 0.45331893326639167),
        1.0, -2.0, 1.0, -1.8104676460327398, 0.8352161615670907
    ).copyOfRange(SETTLE, COUNT)

    val amplitude = rms(filtered)
    if (amplitude < 0.00001) return null

    return peakReading(filtered, amplitude) ?: spectralReading(filtered, amplitude)
}

/** Time-domain estimate: peak intervals + autocorrelation. Also gives beat intervals. */
private fun peakReading(filtered: DoubleArray, amplitude: Double): PulseReading? {
    val peaks = mutableListOf<Int>()
    for (i in 1 until filtered.lastIndex) {
        if (filtered[i] <= amplitude * 0.3 || filtered[i] <= filtered[i - 1] || filtered[i] < filtered[i + 1]) continue
        val previous = peaks.lastOrNull()
        if (previous == null || i - previous >= 8) peaks.add(i)
        else if (filtered[i] > filtered[previous]) peaks[peaks.lastIndex] = i
    }
    val refined = peaks.map { index ->
        val left = filtered[index - 1]
        val center = filtered[index]
        val right = filtered[index + 1]
        val curvature = left - 2 * center + right
        index + if (abs(curvature) < 1e-12) 0.0 else ((left - right) / (2 * curvature)).coerceIn(-0.5, 0.5)
    }
    val intervals = refined.zipWithNext { a, b -> b - a }
    if (intervals.size < 3) return null
    val period = median(intervals)
    if (period !in 10.0..42.0) return null
    val regular = intervals.count { abs(it - period) <= 0.25 * period }.toDouble() / intervals.size
    val lag = period.roundToInt()
    var product = 0.0
    var energyA = 0.0
    var energyB = 0.0
    for (i in lag until filtered.size) {
        product += filtered[i] * filtered[i - lag]
        energyA += filtered[i] * filtered[i]
        energyB += filtered[i - lag] * filtered[i - lag]
    }
    val correlation = product / sqrt(energyA * energyB)
    if (!correlation.isFinite() || correlation < 0.10 || regular < 0.20) return null
    return PulseReading(
        (60 * RATE / period).roundToInt(),
        (correlation + regular) / 2,
        waveform(filtered, amplitude),
        if (regular == 1.0) intervals.map { it * 1000 / RATE } else emptyList()
    )
}

/** Frequency-domain fallback: strongest component in 45-180 BPM. */
private fun spectralReading(filtered: DoubleArray, amplitude: Double): PulseReading? {
    val n = filtered.size
    val windowed = DoubleArray(n) { filtered[it] * (0.5 - 0.5 * cos(2 * PI * it / (n - 1))) }
    var bestBpm = 0
    var bestPower = 0.0
    var total = 0.0
    for (bpm in 45..180) {
        val w = 2 * PI * bpm / 60.0 / RATE
        var re = 0.0
        var im = 0.0
        for (i in 0 until n) {
            re += windowed[i] * cos(w * i)
            im -= windowed[i] * sin(w * i)
        }
        val power = re * re + im * im
        total += power
        if (power > bestPower) {
            bestPower = power
            bestBpm = bpm
        }
    }
    if (total <= 0.0 || bestBpm == 0) return null
    val dominance = bestPower / total          // uniform noise would be ~0.007
    if (dominance < 0.04) return null
    return PulseReading(bestBpm, dominance.coerceAtMost(1.0), waveform(filtered, amplitude), emptyList())
}

private fun waveform(filtered: DoubleArray, amplitude: Double): List<Float> =
    filtered.map { (it / (amplitude * 3)).coerceIn(-1.0, 1.0).toFloat() }

private fun rms(values: DoubleArray) = sqrt(values.sumOf { it * it } / values.size)

private fun median(values: List<Double>): Double {
    if (values.isEmpty()) return 0.0
    val sorted = values.sorted()
    return (sorted[(sorted.size - 1) / 2] + sorted[sorted.size / 2]) / 2
}

private fun section(input: DoubleArray, b0: Double, b1: Double, b2: Double, a1: Double, a2: Double): DoubleArray {
    var z1 = 0.0
    var z2 = 0.0
    return DoubleArray(input.size) { index ->
        val value = input[index]
        val output = value * b0 + z1
        z1 = value * b1 - output * a1 + z2
        z2 = value * b2 - output * a2
        output
    }
}