package com.example.cardioveyra.ui.screens.scan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import com.example.cardioveyra.analyzer.FaceFrame
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
internal fun CameraProgressRing(
    activeColor: Color,
    progress: Float,
    onStateChange: (ScanQualityState) -> Unit,
    onCapture: (FaceFrame) -> Unit
) {
    Box(modifier = Modifier.size(320.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 6.dp.toPx()
            drawCircle(
                color = activeColor.copy(alpha = 0.25f),
                radius = (size.minDimension - strokeWidth) / 2,
                style = Stroke(strokeWidth)
            )
            drawArc(
                color = activeColor,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2),
                size = androidx.compose.ui.geometry.Size(size.width - strokeWidth, size.height - strokeWidth),
                style = Stroke(strokeWidth)
            )
        }

        Box(
            modifier = Modifier
                .size(290.dp)
                .clip(CircleShape)
                .background(SurfaceDark)
        ) {
            CameraXLivePreview(
                modifier = Modifier.fillMaxSize(),
                onStateChanged = onStateChange,
                onCapture = onCapture
            )
        }
    }
}
