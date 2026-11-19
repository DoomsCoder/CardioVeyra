package com.example.cardioveyra.ui.screens.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.cardioveyra.ui.screens.statistics.StatColors
import com.example.cardioveyra.ui.theme.ThemeState

data class MockScan(
    val title: String,
    val bpm: String,
    val hrv: String,
    val stress: String,
    val status: String,
    val statusColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanDetailsScreen(
    scanId: Long,
    isDarkTheme: Boolean = ThemeState.isDarkTheme ?: isSystemInDarkTheme(),
    colors: StatColors = StatColors(
        bgColor = if (isDarkTheme) Color(0xFF0B0F19) else Color(0xFFF8FAFC),
        cardBg = if (isDarkTheme) Color(0xFF131826) else Color(0xFFFFFFFF),
        cardBorder = if (isDarkTheme) Color.Transparent else Color(0xFFE2E8F0),
        primaryText = if (isDarkTheme) Color.White else Color(0xFF0F172A),
        slate = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
        lightSlate = if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF94A3B8),
        teal = if (isDarkTheme) Color(0xFF2DD4BF) else Color(0xFF0D9488),
        crimson = Color(0xFFBA002C),
        gold = Color(0xFFD4AF37),
        blue = Color(0xFF4FC3F7)
    ),
    onBack: () -> Unit
) {
    val mockData = mapOf(
        1L to MockScan("Today, 10:30 AM", "72", "48", "24", "Optimal Recovery", colors.teal),
        2L to MockScan("Today, 07:15 AM", "68", "52", "18", "Optimal Recovery", colors.teal),
        3L to MockScan("Yesterday, 09:45 PM", "78", "41", "45", "Balanced", colors.blue),
        4L to MockScan("Yesterday, 02:20 PM", "84", "36", "65", "Fatigued", colors.gold)
    )
    val scan = mockData[scanId] ?: mockData[1L]!!

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onBack,
        sheetState = sheetState,
        containerColor = colors.bgColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.slate
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(scan.statusColor.copy(alpha = 0.15f))
                            .border(1.dp, scan.statusColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(scan.statusColor))
                        Text(
                            text = scan.status,
                            color = scan.statusColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "BIOMETRIC SCAN DETAILS",
                        color = colors.crimson,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = scan.title,
                        color = colors.primaryText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                // Metrics Grid (2x2)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard(
                        modifier = Modifier.weight(1f).border(if (colors.cardBorder == Color.Transparent) 0.dp else 1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                        title = "Heart Rate",
                        value = scan.bpm,
                        unit = "BPM",
                        subText = "Regular Resting Rate",
                        subTextColor = colors.teal,
                        cardColor = colors.cardBg,
                        textColor = colors.primaryText,
                        slateColor = colors.slate
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f).border(if (colors.cardBorder == Color.Transparent) 0.dp else 1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                        title = "HRV (rMSSD)",
                        value = scan.hrv,
                        unit = "ms",
                        subText = "+4 ms vs baseline",
                        subTextColor = colors.teal,
                        cardColor = colors.cardBg,
                        textColor = colors.primaryText,
                        slateColor = colors.slate
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard(
                        modifier = Modifier.weight(1f).border(if (colors.cardBorder == Color.Transparent) 0.dp else 1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                        title = "Stress Score",
                        value = scan.stress,
                        unit = "/ 100",
                        subText = "Low Stress • Calm",
                        subTextColor = colors.blue,
                        cardColor = colors.cardBg,
                        textColor = colors.primaryText,
                        slateColor = colors.slate
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f).border(if (colors.cardBorder == Color.Transparent) 0.dp else 1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                        title = "Quality Index",
                        value = "98",
                        unit = "%",
                        subText = "Signal Integrity: High",
                        subTextColor = colors.slate,
                        cardColor = colors.cardBg,
                        textColor = colors.primaryText,
                        slateColor = colors.slate
                    )
                }
                
                // Beat-to-Beat RR Tachogram
                Card(
                modifier = Modifier.fillMaxWidth().border(if (colors.cardBorder == Color.Transparent) 0.dp else 1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text("Beat-to-Beat RR Tachogram", color = colors.primaryText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("60s Recording • 74 Cardiac Cycles", color = colors.slate, fontSize = 11.sp)
                        }
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.teal.copy(alpha = 0.15f))
                                .border(1.dp, colors.teal.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mean RR: 833 ms", color = colors.teal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                        val rrData = listOf(820f, 850f, 790f, 880f, 810f, 860f, 780f, 890f, 840f, 870f)
                        val maxRR = 900f
                        val minRR = 750f
                        
                        Canvas(modifier = Modifier.fillMaxSize().padding(start = 30.dp, bottom = 20.dp, top = 10.dp)) {
                            val w = size.width
                            val h = size.height
                            val stepX = w / (rrData.size - 1)
                            
                            // Grid Lines
                            val yLabels = listOf(900f, 850f, 800f, 750f)
                            yLabels.forEach { label ->
                                val normY = 1f - ((label - minRR) / (maxRR - minRR))
                                val y = normY * h
                                drawLine(
                                    color = colors.slate.copy(alpha = 0.3f),
                                    start = Offset(0f, y),
                                    end = Offset(w, y),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                                )
                            }
                            
                            // Path
                            val path = Path()
                            val fillPath = Path()
                            
                            val points = rrData.mapIndexed { index, value ->
                                val normY = 1f - ((value - minRR) / (maxRR - minRR))
                                Offset(index * stepX, normY * h)
                            }
                            
                            path.moveTo(points[0].x, points[0].y)
                            fillPath.moveTo(points[0].x, h)
                            fillPath.lineTo(points[0].x, points[0].y)
                            
                            for (i in 1 until points.size) {
                                val p0 = points[i - 1]
                                val p1 = points[i]
                                val cpX = p0.x + (p1.x - p0.x) / 2f
                                path.cubicTo(cpX, p0.y, cpX, p1.y, p1.x, p1.y)
                                fillPath.cubicTo(cpX, p0.y, cpX, p1.y, p1.x, p1.y)
                            }
                            
                            fillPath.lineTo(points.last().x, h)
                            fillPath.close()
                            
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(colors.teal.copy(alpha = 0.25f), Color.Transparent)
                                )
                            )
                            
                            drawPath(
                                path = path,
                                color = colors.teal,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                            
                            // Points (Peaks and valleys)
                            for (i in points.indices) {
                                val p = points[i]
                                val isPeak = rrData[i] >= 850f
                                val isValley = rrData[i] <= 810f
                                
                                val dotColor = if (isValley) colors.crimson else if (isPeak) colors.teal else Color.Transparent
                                
                                if (dotColor != Color.Transparent) {
                                    drawCircle(color = colors.cardBg, radius = 5.dp.toPx(), center = p)
                                    drawCircle(color = dotColor, radius = 3.dp.toPx(), center = p)
                                }
                            }
                        }
                        
                        // Y-axis labels
                        Column(modifier = Modifier.align(Alignment.TopStart).fillMaxHeight().padding(bottom = 20.dp, top = 2.dp), verticalArrangement = Arrangement.SpaceBetween) {
                            Text("900", color = colors.slate, fontSize = 10.sp)
                            Text("850", color = colors.slate, fontSize = 10.sp)
                            Text("800", color = colors.slate, fontSize = 10.sp)
                            Text("750", color = colors.slate, fontSize = 10.sp)
                        }
                        
                        // X-axis labels
                        Row(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(start = 30.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Beat #1", color = colors.slate, fontSize = 10.sp)
                            Text("Beat #37 (30s)", color = colors.slate, fontSize = 10.sp)
                            Text("Beat #74 (60s)", color = colors.slate, fontSize = 10.sp)
                        }
                    }
                }
            }
            
            // PPG Waveform
            Card(
                modifier = Modifier.fillMaxWidth().border(if (colors.cardBorder == Color.Transparent) 0.dp else 1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(colors.crimson))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("10-Sec Photoplethysmogram (PPG)", color = colors.primaryText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Text("Waveform", color = colors.slate, fontSize = 11.sp, modifier = Modifier.padding(start = 12.dp))
                        }
                        
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.teal.copy(alpha = 0.1f))
                                .border(1.dp, colors.teal.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Signal SNR: 28.4 dB", color = colors.teal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 20.dp, top = 10.dp)) {
                            val path = Path()
                            val fillPath = Path()
                            val w = size.width
                            val h = size.height
                            val cycles = 15 // Increased number of cycles to look like the image
                            val cycleWidth = w / cycles
                            
                            // Draw horizontal background grid lines
                            val gridLines = 4
                            val stepY = h / gridLines
                            for (i in 0..gridLines) {
                                drawLine(
                                    color = colors.slate.copy(alpha = 0.2f),
                                    start = Offset(0f, i * stepY),
                                    end = Offset(w, i * stepY),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                            
                            path.moveTo(0f, h * 0.7f)
                            fillPath.moveTo(0f, h)
                            fillPath.lineTo(0f, h * 0.7f)
                            
                            for (i in 0 until cycles) {
                                val startX = i * cycleWidth
                                val endX = startX + cycleWidth
                                val peakX = startX + cycleWidth * 0.2f
                                val dipX = startX + cycleWidth * 0.4f
                                val notchX = startX + cycleWidth * 0.65f
                                
                                // Baseline to systolic peak
                                path.cubicTo(
                                    startX + cycleWidth * 0.05f, h * 0.7f,
                                    peakX - cycleWidth * 0.05f, h * 0.1f,
                                    peakX, h * 0.1f
                                )
                                fillPath.cubicTo(startX + cycleWidth * 0.05f, h * 0.7f, peakX - cycleWidth * 0.05f, h * 0.1f, peakX, h * 0.1f)
                                
                                // Systolic peak to dip
                                path.cubicTo(
                                    peakX + cycleWidth * 0.05f, h * 0.1f,
                                    dipX - cycleWidth * 0.05f, h * 0.85f,
                                    dipX, h * 0.85f
                                )
                                fillPath.cubicTo(peakX + cycleWidth * 0.05f, h * 0.1f, dipX - cycleWidth * 0.05f, h * 0.85f, dipX, h * 0.85f)
                                
                                // Dip to dicrotic notch
                                path.cubicTo(
                                    dipX + cycleWidth * 0.1f, h * 0.85f,
                                    notchX - cycleWidth * 0.1f, h * 0.5f,
                                    notchX, h * 0.5f
                                )
                                fillPath.cubicTo(dipX + cycleWidth * 0.1f, h * 0.85f, notchX - cycleWidth * 0.1f, h * 0.5f, notchX, h * 0.5f)
                                
                                // Dicrotic notch to baseline
                                path.cubicTo(
                                    notchX + cycleWidth * 0.1f, h * 0.5f,
                                    endX - cycleWidth * 0.1f, h * 0.7f,
                                    endX, h * 0.7f
                                )
                                fillPath.cubicTo(notchX + cycleWidth * 0.1f, h * 0.5f, endX - cycleWidth * 0.1f, h * 0.7f, endX, h * 0.7f)
                            }
                            
                            fillPath.lineTo(w, h)
                            fillPath.close()
                            
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(colors.crimson.copy(alpha = 0.15f), Color.Transparent)
                                )
                            )
                            
                            drawPath(
                                path = path,
                                color = colors.crimson,
                                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                        
                        // Bottom row of slate time labels
                        Row(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("0.0s", color = colors.slate, fontSize = 10.sp)
                            Text("2.5s", color = colors.slate, fontSize = 10.sp)
                            Text("5.0s", color = colors.slate, fontSize = 10.sp)
                            Text("7.5s", color = colors.slate, fontSize = 10.sp)
                            Text("10.0s", color = colors.slate, fontSize = 10.sp)
                        }
                    }
                }
            }
            
            // Autonomic Balance
            Card(
                modifier = Modifier.fillMaxWidth().border(if (colors.cardBorder == Color.Transparent) 0.dp else 1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Autonomic Balance", color = colors.primaryText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("70% Parasympathetic Tone", color = colors.teal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp))) {
                        Box(modifier = Modifier.weight(0.7f).fillMaxHeight().background(colors.teal))
                        Box(modifier = Modifier.weight(0.3f).fillMaxHeight().background(colors.crimson))
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colors.teal))
                            Text("Rest / Digest (70%)", color = colors.slate, fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colors.crimson))
                            Text("Sympathetic (30%)", color = colors.slate, fontSize = 11.sp)
                        }
                    }
                }
            }
            
            // Scan Summary
            Card(
                modifier = Modifier.fillMaxWidth().border(if (colors.cardBorder == Color.Transparent) 0.dp else 1.dp, colors.cardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .border(1.dp, colors.gold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .background(colors.gold.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Insight",
                            tint = colors.gold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "SCAN SUMMARY",
                            color = colors.gold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Consistent inter-beat intervals and low sympathetic activation indicate strong recovery post-rest.",
                            color = colors.slate,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            } // End scrollable Column
            
            // Bottom Pinned Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bgColor)
                    .navigationBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Export PDF */ },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.slate
                    ),
                    border = BorderStroke(1.dp, colors.slate)
                ) {
                    Text("Export PDF", fontWeight = FontWeight.SemiBold)
                }
                
                Button(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.crimson,
                        contentColor = Color.White
                    )
                ) {
                    Text("Done", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String,
    subText: String,
    subTextColor: Color,
    cardColor: Color,
    textColor: Color,
    slateColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, color = slateColor, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(value, color = textColor, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(unit, color = slateColor, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
            }
            Text(subText, color = subTextColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Preview
@Composable
fun ScanDetailsScreenPreview() {
    ScanDetailsScreen(scanId = 1L, onBack = {})
}
