package com.example.cardioveyra.ui.screens.scan

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cardioveyra.ui.theme.CardioVeyraTheme
import com.example.cardioveyra.ui.theme.ThemeState

// Theme Colors from your design
val NavyBg = Color(0xFF0B0F19)
val SignalGreen = Color(0xFF10B981)
val WarningYellow = Color(0xFFF59E0B)
val ErrorGrey = Color(0xFF707481)
val BrandRed = Color(0xFFBA002C)
val SurfaceDark = Color(0xFF141923)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onCloseClicked: () -> Unit = {}
) {
    // State to simulate rPPG analyzer output
    var scanState by remember { mutableStateOf(ScanQualityState.OPTIMAL) }
    var progress by remember { mutableFloatStateOf(0.45f) } // 45% filled

    var showCancelDialog by remember {
        mutableStateOf(false)
    }

    BackHandler(enabled = true) {
        showCancelDialog = true
    }

    // Dynamic color based on state
    val activeColor by animateColorAsState(
        targetValue = when (scanState) {
            ScanQualityState.OPTIMAL -> SignalGreen
            ScanQualityState.MOTION_WARNING -> WarningYellow
            ScanQualityState.POOR_LIGHTING -> WarningYellow // Map poor lighting to yellow
            ScanQualityState.FACE_LOST -> ErrorGrey
        },
        animationSpec = tween(500),
        label = "RingColor"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Fix: Replaced hardcoded NavyBg with a theme-aware background
            .background(if (ThemeState.isDarkTheme == true || (ThemeState.isDarkTheme == null && isSystemInDarkTheme())) NavyBg else Color(0xFFF8F9FA))
            // Removed .statusBarsPadding() so the TopNavBar sits flush against the top edge 
            // inside the edge-to-edge window. The TopNavBar should handle its own safe area padding.
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Top Navigation Bar
        // Pass the lambda that opens the dialog to the TopNavBar, NOT onCloseClicked directly
        TopNavBar(
            activeColor = activeColor, 
            onCancel = { showCancelDialog = true }
        )

        Spacer(modifier = Modifier.weight(0.5f))

        // 2. Centerpiece: Progress Ring & Camera Viewport
        CameraProgressRing(
            progress = progress,
            activeColor = activeColor,
            state = scanState,
            onStateChange = { newState ->
                // The ML Kit analyzer will constantly update this state!
                scanState = newState
            }
        )

        Spacer(modifier = Modifier.weight(0.5f))

        // 3. Real-Time Status Text
        StatusSection(state = scanState)

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Telemetry Pill
        TelemetryPill(state = scanState)

        Spacer(modifier = Modifier.weight(1f))

        // 5. Bottom ECG Waveform
        AnimatedEcgWave(
            activeColor = activeColor,
            isPaused = scanState != ScanQualityState.OPTIMAL
        )
    }

    if(showCancelDialog) {
        AlertDialog(
            onDismissRequest = {
                showCancelDialog = false
            },
            title = { Text("Cancel Scan?") },
            text = { Text("If you leave now, your current scan progress will be lost.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        // User confirmed they want to leave. Trigger the navigation callback!
                        onCloseClicked()
                    }
                ) {
                    Text("End Scan", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCancelDialog = false }
                ) {
                    Text("Keep Scanning")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScanScreen() {
    CardioVeyraTheme {
        ScanScreen()
    }
}
