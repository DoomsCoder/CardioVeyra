package com.example.cardioveyra.ui.screens.scan

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardioveyra.ui.theme.ThemeState


@Composable
fun StatusSection(state: ScanQualityState) {
    
    // Read theme state
    val systemIsDark = isSystemInDarkTheme()
    val isDark = ThemeState.isDarkTheme ?: systemIsDark
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 32.dp)
    ) {
        AnimatedContent(targetState = state, label = "Headline") { targetState ->
            Text(
                text = when (targetState) {
                    ScanQualityState.OPTIMAL -> "Extracting pulse..."
                    ScanQualityState.MOTION_WARNING -> "Hold still"
                    ScanQualityState.POOR_LIGHTING -> "Too dark"
                    ScanQualityState.FACE_LOST -> "Face lost"
                },
                color = if (isDark) Color.White else Color.Black,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Hold perfectly still. 15 seconds remaining.",
            color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic Warning Badge
        AnimatedVisibility(
            visible = state != ScanQualityState.OPTIMAL,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .background(WarningYellow.copy(alpha = 0.15f), RoundedCornerShape(50))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = WarningYellow, modifier = Modifier.size(16.dp))
                Text(
                    text = when (state) {
                        ScanQualityState.MOTION_WARNING -> "Motion detected"
                        ScanQualityState.POOR_LIGHTING -> "Find a brighter space"
                        else -> "Poor lighting or ROI lost"
                    },
                    color = WarningYellow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}