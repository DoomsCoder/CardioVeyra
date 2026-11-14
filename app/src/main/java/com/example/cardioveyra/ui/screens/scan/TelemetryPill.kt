package com.example.cardioveyra.ui.screens.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardioveyra.ui.theme.ThemeState

@Composable
fun TelemetryPill(state: ScanQualityState) {
    // Read theme state
    val systemIsDark = isSystemInDarkTheme()
    val isDark = ThemeState.isDarkTheme ?: systemIsDark
    
    val surfaceColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)
    val dividerColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)
    val textColor = if (isDark) Color.White else Color.Black
    val labelColor = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f)

    Row(
        modifier = Modifier
            .background(surfaceColor, RoundedCornerShape(50))
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Pulse Data
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Favorite, contentDescription = null, tint = BrandRed, modifier = Modifier.size(20.dp))
            Text("Pulse", color = labelColor, fontSize = 14.sp)
            Text(if (state == ScanQualityState.FACE_LOST) "--" else "72", color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("BPM", color = labelColor, fontSize = 10.sp)
        }

        // Divider
        Box(modifier = Modifier.width(1.dp).height(16.dp).background(dividerColor))

        // Quality Data
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.Speed, contentDescription = null, tint = SignalGreen, modifier = Modifier.size(20.dp))
            Text("Quality", color = labelColor, fontSize = 14.sp)
            Text(
                text = when (state) {
                    ScanQualityState.OPTIMAL -> "High"
                    ScanQualityState.MOTION_WARNING, ScanQualityState.POOR_LIGHTING -> "Low"
                    ScanQualityState.FACE_LOST -> "None"
                },
                color = when (state) {
                    ScanQualityState.OPTIMAL -> SignalGreen
                    ScanQualityState.MOTION_WARNING, ScanQualityState.POOR_LIGHTING -> WarningYellow
                    ScanQualityState.FACE_LOST -> ErrorGrey
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}