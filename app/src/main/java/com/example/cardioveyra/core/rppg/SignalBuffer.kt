package com.example.cardioveyra.core.rppg

import androidx.annotation.GuardedBy
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Thread-safe buffer to store raw RGB spatial averages over time.
 * Designed to feed directly into a TensorFlow Lite end-to-end model.
 */
class SignalBuffer(private val capacity: Int = 300) {

    private val lock = ReentrantLock()

    @GuardedBy("lock")
    private val rBuffer = DoubleArray(capacity)

    @GuardedBy("lock")
    private val gBuffer = DoubleArray(capacity)

    @GuardedBy("lock")
    private val bBuffer = DoubleArray(capacity)

    @GuardedBy("lock")
    private var count = 0

    fun addSample(r: Double, g: Double, b: Double) = lock.withLock {
        if (count < capacity) {
            rBuffer[count] = r
            gBuffer[count] = g
            bBuffer[count] = b
            count++
        }
    }

    fun isFull(): Boolean = lock.withLock {
        count >= capacity
    }

    fun clear() = lock.withLock {
        count = 0
    }

    fun getCurrentSampleCount(): Int = lock.withLock {
        count
    }

    /**
     * Converts the buffer into an interleaved 1D FloatArray of shape [capacity * 3].
     * Format: [R0, G0, B0, R1, G1, B1, ... Rn, Gn, Bn]
     * Ready to be wrapped in a ByteBuffer/TensorBuffer for TFLite inference.
     */
    fun toFlatFloatArray(): FloatArray = lock.withLock {
        val result = FloatArray(count * 3)
        for (i in 0 until count) {
            result[(i * 3)] = rBuffer[i].toFloat()
            result[(i * 3) + 1] = gBuffer[i].toFloat()
            result[(i * 3) + 2] = bBuffer[i].toFloat()
        }
        return result
    }
}