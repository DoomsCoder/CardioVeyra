package com.example.cardioveyra.ui.screens.scan

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardioveyra.analyzer.FaceRegion
import com.example.cardioveyra.analyzer.RegionRgb
import com.example.cardioveyra.core.rppg.PulseReading
import com.example.cardioveyra.core.rppg.PulseResult
import com.example.cardioveyra.core.rppg.PulseScan
import com.example.cardioveyra.ui.theme.CardioVeyraTheme
import com.example.cardioveyra.ui.theme.ThemeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

val NavyBg = Color(0xFF0B0F19)
val SignalGreen = Color(0xFF10B981)
val WarningYellow = Color(0xFFF59E0B)
val ErrorGrey = Color(0xFF707481)
val BrandRed = Color(0xFFBA002C)
val SurfaceDark = Color(0xFF141923)

private data class Capture(val timestamp: Long, val rgb: Map<FaceRegion, RegionRgb>)

@Composable
fun ScanScreen(onCloseClicked: () -> Unit = {}) {
    var attempt by remember { mutableStateOf(0) }
    key(attempt) {
        ScanAttempt(onCloseClicked, onRetry = { attempt++ })
    }
}

@Composable
private fun ScanAttempt(onClose: () -> Unit, onRetry: () -> Unit) {
    var quality by remember { mutableStateOf(ScanQualityState.FACE_LOST) }
    var progress by remember { mutableStateOf(0f) }
    var reading by remember { mutableStateOf<PulseReading?>(null) }
    var result by remember { mutableStateOf<PulseResult?>(null) }
    var showCancel by remember { mutableStateOf(false) }
    val captures = remember { Channel<Capture>(64, BufferOverflow.DROP_OLDEST) }
    val preview = LocalInspectionMode.current

    if (!preview) {
        LaunchedEffect(Unit) {
            withContext(Dispatchers.Default) {
                val scan = PulseScan()
                val started = SystemClock.elapsedRealtime()
                while (true) {
                    repeat(64) {
                        val frame = captures.tryReceive().getOrNull() ?: return@repeat
                        scan.add(frame.timestamp, frame.rgb)
                    }
                    val summary = scan.result(((SystemClock.elapsedRealtime() - started) / 1000).toInt())
                    withContext(Dispatchers.Main) {
                        progress = scan.progress
                        reading = scan.live
                        result = summary
                    }
                    if (summary != null) break
                    delay(100)
                }
            }
        }
    }

    val summary = result
    BackHandler { if (summary == null) showCancel = true else onClose() }
    
    val colorState by animateColorAsState(
        targetValue = when (quality) {
            ScanQualityState.OPTIMAL -> SignalGreen
            ScanQualityState.FACE_LOST, ScanQualityState.PERMISSION_DENIED, ScanQualityState.CAMERA_ERROR -> ErrorGrey
            else -> WarningYellow
        },
        animationSpec = tween(durationMillis = 300),
        label = "ring_color"
    )

    val dark = ThemeState.isDarkTheme ?: isSystemInDarkTheme()
    Column(
        modifier = Modifier.fillMaxSize()
            .background(if (dark) NavyBg else Color(0xFFF8FAFC))
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopNavBar(activeColor = colorState, onCancel = { if (summary == null) showCancel = true else onClose() })
        if (summary != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .background(
                        if (dark) Color(0xFF131826) else Color.White,
                        RoundedCornerShape(20.dp)
                    )
                    .border(
                        1.dp,
                        if (dark) Color(0xFF1E2538) else Color(0xFFE2E8F0),
                        androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "SCAN COMPLETE",
                        color = BrandRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        summary.bpm?.let { "$it BPM" } ?: "Signal too weak",
                        style = MaterialTheme.typography.displayMedium,
                        color = if (dark) Color.White else Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Duration: ${summary.elapsedSeconds}s • Quality: ${summary.quality.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }}",
                        color = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        fontSize = 13.sp
                    )
                    if (summary.bpm == null) {
                        Text(
                            "Rescan in even lighting and keep your head still.",
                            color = WarningYellow,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = onClose,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandRed,
                            contentColor = Color.White
                        )
                    ) {
                        Text("View Results", fontWeight = FontWeight.Bold)
                    }
                    
                    if (summary.bpm == null || summary.quality != ScanQualityState.OPTIMAL.name) {
                        TextButton(
                            onClick = onRetry,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Try Again", color = if (dark) Color.White else Color(0xFF0F172A))
                        }
                    }
                }
            }
        } else {
            CameraProgressRing(
                activeColor = colorState,
                progress = progress,
                onStateChange = { state ->
                    quality = state
                    if (state != ScanQualityState.OPTIMAL) captures.trySend(Capture(0L, emptyMap()))
                },
                onCapture = { frame -> captures.trySend(Capture(frame.timestampNs, frame.rgb)) }
            )
            StatusSection(state = quality)
            val live = reading.takeIf { quality == ScanQualityState.OPTIMAL }
            
            TelemetryPill(
                bpm = live?.bpm?.toString() ?: "--",
                hrv = "--",
                quality = quality,
                isDark = dark
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Canvas(Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(90.dp)) {
                drawLine(colorState.copy(alpha = 0.25f), Offset(0f, center.y), Offset(size.width, center.y))
                live?.waveform?.takeIf { it.size > 1 }?.let { waveform ->
                    val path = Path()
                    waveform.forEachIndexed { index, value ->
                        val x = size.width * index / waveform.lastIndex
                        val y = center.y - value * size.height * 0.45f
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, colorState, style = Stroke(2.dp.toPx()))
                }
            }
        }
    }
    if (showCancel && summary == null) {
        AlertDialog(
            onDismissRequest = { showCancel = false },
            title = { Text("End scan?") },
            confirmButton = { TextButton(onClick = onClose) { Text("End scan") } },
            dismissButton = {
                TextButton(onClick = { showCancel = false }) { Text("Keep scanning") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScanScreen() {
    CardioVeyraTheme { ScanScreen() }
}
