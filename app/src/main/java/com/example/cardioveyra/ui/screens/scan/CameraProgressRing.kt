package com.example.cardioveyra.ui.screens.scan

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CameraProgressRing(
    progress: Float,
    activeColor: Color,
    state: ScanQualityState,
    onStateChange: (ScanQualityState) -> Unit
) {
    // Pulse animation for the outer ring when Optimal
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_scale"
    )

    val currentScale = if (state == ScanQualityState.OPTIMAL) scale else 1f

    Box(
        modifier = Modifier
            .size(320.dp), // Removed .clickable since ML kit handles it automatically now
        contentAlignment = Alignment.Center
    ) {
        // Outer Progress Arc (Conic Gradient)
        Canvas(modifier = Modifier
            .fillMaxSize()
            .rotate(-90f) // Start at 12 o'clock
        ) {
            val strokeWidth = 6.dp.toPx()
            val sweepAngle = 360f * progress

            // Background Track
            drawArc(
                color = Color(0xFF1E293B),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )

            // Active Progress
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(activeColor.copy(alpha = 0.2f), activeColor),
                    center = Offset(size.width / 2, size.height / 2)
                ),
                startAngle = 0f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Inner Camera Viewport Box (Replace this Box with CameraX PreviewView later)
        Box(
            modifier = Modifier
                .size(290.dp) // Slightly smaller than outer ring
                .clip(CircleShape)
                .background(SurfaceDark),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder Icon representing user's face
            CameraXLivePreview(
                modifier = Modifier.fillMaxSize(),
                onStateChanged = { newState ->
                    // Pass the real ML Kit state UP to the ScanScreen via the callback
                    onStateChange(newState)
                }
            )

            // Centering Locked overlay
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    modifier = Modifier
                        .padding(bottom = 40.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).background(activeColor, CircleShape))
                    Text(
                        text = when (state) {
                            ScanQualityState.OPTIMAL -> "CENTERING LOCKED"
                            ScanQualityState.MOTION_WARNING -> "STABILIZING..."
                            ScanQualityState.POOR_LIGHTING -> "INCREASE LIGHTING"
                            ScanQualityState.FACE_LOST -> "SEARCHING..."
                        },
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}