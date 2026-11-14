package com.example.cardioveyra.ui.screens.scan

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.cardioveyra.ui.theme.ThemeState

@Composable
fun AnimatedEcgWave(activeColor: Color, isPaused: Boolean) {
    // Read theme state
    val systemIsDark = isSystemInDarkTheme()
    val isDark = ThemeState.isDarkTheme ?: systemIsDark
    
    val baseLineColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.2f)
    val infiniteTransition = rememberInfiniteTransition(label = "ecg_scroll")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f, // Width of one full path segment
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ecg_offset"
    )

    // Freeze animation if state is not optimal
    val currentOffset = if (isPaused) 0f else offsetX

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        val path = Path()
        val baseY = size.height / 2
        val width = 1000f

        // Creating a repeating ECG pattern
        for (i in 0..2) {
            val startX = (i * width) - currentOffset
            path.moveTo(startX, baseY)
            path.lineTo(startX + 120, baseY) // Flat line
            path.lineTo(startX + 135, baseY - 5) // P wave start
            path.lineTo(startX + 145, baseY + 5) // P wave down
            path.lineTo(startX + 160, baseY) // PR segment
            path.lineTo(startX + 180, baseY) // Q dip
            path.lineTo(startX + 190, baseY - 30) // R peak (up)
            path.lineTo(startX + 200, baseY + 40) // S dip (down)
            path.lineTo(startX + 215, baseY) // ST segment
            path.lineTo(startX + 240, baseY) // T wave start
            path.lineTo(startX + 250, baseY - 10) // T wave peak
            path.lineTo(startX + 275, baseY) // Flat
            path.lineTo(startX + width, baseY) // Connect to next
        }

        drawPath(
            path = path,
            color = activeColor.copy(alpha = 0.4f),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
        )
        
        // Static Baseline (Background)
        drawLine(
            color = baseLineColor,
            start = Offset(0f, baseY),
            end = Offset(size.width, baseY),
            strokeWidth = 2.dp.toPx()
        )
    }
}