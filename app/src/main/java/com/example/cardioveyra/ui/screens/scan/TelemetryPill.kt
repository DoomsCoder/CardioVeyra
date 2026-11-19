package com.example.cardioveyra.ui.screens.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TelemetryPill(
    bpm: String,
    hrv: String,
    quality: ScanQualityState,
    isDark: Boolean
) {
    val pillBg = if (isDark) Color(0xFF131826) else Color(0xFFFFFFFF)
    val pillBorder = if (isDark) Color(0xFF1E2538) else Color(0xFFE2E8F0)
    val primaryText = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val secondaryText = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    
    val qualityColor = when (quality) {
        ScanQualityState.OPTIMAL -> SignalGreen
        ScanQualityState.FACE_LOST, ScanQualityState.PERMISSION_DENIED, ScanQualityState.CAMERA_ERROR -> ErrorGrey
        else -> WarningYellow
    }
    
    val qualityText = when (quality) {
        ScanQualityState.OPTIMAL -> "High"
        ScanQualityState.FACE_LOST -> "Lost"
        else -> "Low"
    }

    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(pillBg)
            .border(1.dp, pillBorder, RoundedCornerShape(32.dp))
            .padding(vertical = 12.dp, horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // BPM Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Favorite, contentDescription = "Pulse", tint = BrandRed, modifier = Modifier.size(16.dp))
            Text("Pulse", color = secondaryText, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(bpm, color = primaryText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(" BPM", color = secondaryText, fontSize = 10.sp, modifier = Modifier.padding(bottom = 2.dp, start = 2.dp))
            }
        }
        
        // Divider
        Box(modifier = Modifier.width(1.dp).height(24.dp).background(pillBorder))
        
        // Quality Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Speed, contentDescription = "Quality", tint = qualityColor, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Quality ", color = secondaryText, fontSize = 14.sp)
            Text(qualityText, color = qualityColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
