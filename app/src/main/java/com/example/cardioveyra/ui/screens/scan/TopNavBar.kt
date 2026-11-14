package com.example.cardioveyra.ui.screens.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
fun TopNavBar(activeColor: Color, onCancel: () -> Unit) {
    
    // Fix: Properly determine if we are in dark mode to style the buttons
    val systemIsDark = isSystemInDarkTheme()
    val isDark = ThemeState.isDarkTheme ?: systemIsDark
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Close Button
        IconButton(
            onClick = onCancel,
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f),
                    CircleShape
                )
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Cancel",
                tint = if (isDark) Color.White else Color.Black.copy(alpha = 0.7f)
            )
        }

        // Diagnostic Badge
        Row(
            modifier = Modifier
                .background(
                    if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f),
                    RoundedCornerShape(50)
                )
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(activeColor, CircleShape)
            )
            Text(
                text = "CARDIOVEYRA OPTICAL",
                color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}




