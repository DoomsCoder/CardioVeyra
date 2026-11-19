package com.example.cardioveyra.ui.screens.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardioveyra.ui.screens.details.ScanDetailsScreen
import com.example.cardioveyra.ui.theme.ThemeState
import kotlinx.coroutines.launch

data class StatColors(
    val bgColor: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val primaryText: Color,
    val slate: Color,
    val lightSlate: Color,
    val teal: Color,
    val crimson: Color,
    val gold: Color,
    val blue: Color
)

@Composable
fun StatisticsScreen(
    onBack: () -> Unit = {}
) {
    val isDarkTheme = ThemeState.isDarkTheme ?: isSystemInDarkTheme()
    
    val colors = StatColors(
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
    )

    var selectedScanId by remember { mutableStateOf<Long?>(null) }
    val tabs = listOf("Overview", "Heart Rate", "HRV", "Stress")
    
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = colors.bgColor,
        topBar = {
            Column(modifier = Modifier.background(colors.bgColor).statusBarsPadding()) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "CARDIOVEYRA ANALYTICS",
                            color = colors.crimson,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Your Insights",
                            color = colors.primaryText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.cardBg)
                            .border(if (isDarkTheme) 0.dp else 1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("7 Days", color = colors.lightSlate, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Dropdown", tint = colors.lightSlate, modifier = Modifier.size(16.dp))
                    }
                }

                // Tab Menu
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(tabs) { index, title ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) colors.crimson else Color.Transparent)
                                .clickable { 
                                    coroutineScope.launch { pagerState.animateScrollToPage(index) } 
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) Color.White else colors.slate,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (page) {
                        0 -> OverviewTab(colors) { scanId -> selectedScanId = scanId }
                        1 -> HeartRateTab(colors)
                        2 -> HrvTab(colors)
                        3 -> StressTab(colors)
                    }
                    Spacer(modifier = Modifier.height(80.dp)) // Space for bottom nav
                }
            }
        }
    }
    
    // Bottom Sheet for Details
    if (selectedScanId != null) {
        ScanDetailsScreen(
            scanId = selectedScanId!!,
            isDarkTheme = isDarkTheme,
            colors = colors,
            onBack = { selectedScanId = null }
        )
    }
}

// ============================================================================
// TAB CONTENT
// ============================================================================

@Composable
fun OverviewTab(colors: StatColors, onNavigateToDetails: (Long) -> Unit) {
    // Trend Chart
    Card(
        modifier = Modifier.fillMaxWidth().border(if (colors.cardBorder == Color.Transparent) 0.dp else 1.dp, colors.cardBorder, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("CARDIOVASCULAR BASELINE", color = colors.slate, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("7-Day Heart & Rhythm\nTrend", color = colors.primaryText, fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp)
                Box(
                    modifier = Modifier.background(colors.crimson.copy(alpha=0.15f), RoundedCornerShape(12.dp)).padding(horizontal=10.dp, vertical=6.dp)
                ) {
                    Text("Normal\nRange", color = colors.crimson, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("BPM Range (60 - 90)", color = colors.slate, fontSize = 12.sp)
                Text("● Sun: 72 BPM", color = colors.crimson, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            MockAreaChart(
                color = colors.crimson,
                colors = colors,
                data = listOf(62f, 68f, 75f, 60f, 78f, 64f, 72f),
                yLabels = listOf("90", "80", "70", "60"),
                minVal = 60f,
                maxVal = 90f
            )
        }
    }

    // POINT 3: 2x2 Grid for Overview Summaries
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniStatCard(colors, modifier = Modifier.weight(1f), title = "Avg Heart Rate", value = "72", unit = "BPM", subtitle = "↘ Optimal Resting", subtitleColor = colors.teal)
            MiniStatCard(colors, modifier = Modifier.weight(1f), title = "Avg HRV", value = "45", unit = "ms", subtitle = "↗ Balanced", subtitleColor = colors.teal)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniStatCard(colors, modifier = Modifier.weight(1f), title = "Avg Stress", value = "32", unit = "/ 100", subtitle = "Low Stress", subtitleColor = colors.blue)
            MiniStatCard(colors, modifier = Modifier.weight(1f), title = "Avg Signal Quality", value = "96", unit = "%", subtitle = "High Integrity", subtitleColor = colors.slate)
        }
    }

    // Recent Scans
    Card(
        modifier = Modifier.fillMaxWidth().border(if (colors.cardBorder == Color.Transparent) 0.dp else 1.dp, colors.cardBorder, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Recent Scans", color = colors.primaryText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Tap any scan for detailed analysis", color = colors.slate, fontSize = 11.sp)
                }
                Text("See All", color = colors.crimson, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))

            ScanHistoryRow(colors, scanId = 1L, title = "Today, 10:30 AM", metrics = "72 BPM • 48 ms HRV", status = "Optimal", statusColor = colors.teal, onClick = onNavigateToDetails)
            Spacer(modifier = Modifier.height(12.dp))
            ScanHistoryRow(colors, scanId = 2L, title = "Today, 07:15 AM", metrics = "68 BPM • 52 ms HRV", status = "Optimal", statusColor = colors.teal, onClick = onNavigateToDetails)
            Spacer(modifier = Modifier.height(12.dp))
            ScanHistoryRow(colors, scanId = 3L, title = "Yesterday, 09:45 PM", metrics = "78 BPM • 41 ms HRV", status = "Balanced", statusColor = colors.blue, onClick = onNavigateToDetails)
            Spacer(modifier = Modifier.height(12.dp))
            ScanHistoryRow(colors, scanId = 4L, title = "Yesterday, 02:20 PM", metrics = "84 BPM • 36 ms HRV", status = "Fatigued", statusColor = colors.gold, onClick = onNavigateToDetails)
        }
    }
}

@Composable
fun HeartRateTab(colors: StatColors) {
    Card(
        modifier = Modifier.fillMaxWidth().border(if (colors.cardBorder == Color.Transparent) 0.dp else 1.dp, colors.cardBorder, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("HEART RATE TREND", color = colors.slate, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("7-Day BPM Trend", color = colors.primaryText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.background(colors.crimson.copy(alpha=0.15f), RoundedCornerShape(12.dp)).padding(horizontal=10.dp, vertical=6.dp)) {
                    Text("● Live Curve", color = colors.crimson, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Calculated from optical facial photoplethysmography resting scans.", color = colors.slate, fontSize = 12.sp, lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            MockLineChart(
                color = colors.crimson,
                colors = colors,
                data = listOf(62f, 68f, 75f, 60f, 78f, 64f, 71f),
                yLabels = listOf("90", "80", "70", "60"),
                minVal = 60f,
                maxVal = 90f
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().border(if (colors.cardBorder == Color.Transparent) 0.dp else 1.dp, colors.cardBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Resting HR Baseline", color = colors.slate, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("71", color = colors.primaryText, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                Text(" BPM", color = colors.slate, fontSize = 16.sp, modifier = Modifier.padding(bottom = 6.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("↘ ↓ 2 BPM vs last week", color = colors.teal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("30-Day Rolling Avg: 73 BPM", color = colors.slate, fontSize = 11.sp)
            }
        }
    }

    // POINT 4: 2-Column Row for Heart Rate
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MiniStatCard(colors, modifier = Modifier.weight(1f), title = "7-Day Lowest", value = "62", unit = "BPM", subtitle = "Mon", subtitleColor = colors.slate)
        MiniStatCard(colors, modifier = Modifier.weight(1f), title = "7-Day Highest", value = "84", unit = "BPM", subtitle = "Thu", subtitleColor = colors.slate)
    }

    InsightCard(colors, text = "Your resting heart rate dropped by 2 BPM this week, showing steady cardiovascular consistency.")
}

@Composable
fun HrvTab(colors: StatColors) {
    Card(
        modifier = Modifier.fillMaxWidth().border(if (colors.cardBorder == Color.Transparent) 0.dp else 1.dp, colors.cardBorder, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("PARASYMPATHETIC RESERVE", color = colors.teal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("7-Day HRV Trend", color = colors.primaryText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.background(colors.teal.copy(alpha=0.15f), RoundedCornerShape(12.dp)).padding(horizontal=10.dp, vertical=6.dp)) {
                    Text("RMSSD Index", color = colors.teal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Higher HRV indicates greater autonomic resilience and prompt recovery.", color = colors.slate, fontSize = 12.sp, lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            MockAreaChart(
                color = colors.teal,
                colors = colors,
                data = listOf(35f, 38f, 36f, 42f, 48f, 52f, 48f),
                yLabels = listOf("60", "50", "40", "30"),
                minVal = 30f,
                maxVal = 60f
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().border(if (colors.cardBorder == Color.Transparent) 0.dp else 1.dp, colors.cardBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Autonomic Nervous Balance", color = colors.slate, fontSize = 14.sp)
                Box(modifier = Modifier.border(1.dp, colors.teal, RoundedCornerShape(12.dp)).padding(horizontal=10.dp, vertical=4.dp)) {
                    Text("Balanced", color = colors.teal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("48", color = colors.primaryText, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                Text(" ms", color = colors.slate, fontSize = 16.sp, modifier = Modifier.padding(bottom = 6.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Target Range: 42 - 65 ms", color = colors.slate, fontSize = 12.sp)
                Text("+6 ms vs 30-day baseline", color = colors.teal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // POINT 5: 2-Column Row for HRV
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MiniStatCard(colors, modifier = Modifier.weight(1f), title = "7-Day Low", value = "36", unit = "ms", subtitle = "Mon", subtitleColor = colors.slate)
        MiniStatCard(colors, modifier = Modifier.weight(1f), title = "7-Day Peak", value = "52", unit = "ms", subtitle = "Sat", subtitleColor = colors.slate)
    }

    InsightCard(colors, text = "Your RMSSD values remain within your optimal window, indicating solid day-to-day recovery.")
}

@Composable
fun StressTab(colors: StatColors) {
    Card(
        modifier = Modifier.fillMaxWidth().border(if (colors.cardBorder == Color.Transparent) 0.dp else 1.dp, colors.cardBorder, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("AUTONOMIC SYMPATHETIC RATIO", color = colors.slate, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("HRV vs Stress Score", color = colors.primaryText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.background(colors.slate.copy(alpha=0.1f), RoundedCornerShape(12.dp)).padding(horizontal=10.dp, vertical=6.dp)) {
                    Text("Inverse Matrix", color = colors.slate, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Notice how HRV surges while stress score drops during recovery periods.", color = colors.slate, fontSize = 12.sp, lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colors.teal))
                    Text("HRV (ms)", color = colors.slate, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colors.crimson))
                    Text("Stress Score", color = colors.slate, fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            MockComposedChart(
                line1Color = colors.teal, line1Data = listOf(35f, 38f, 36f, 42f, 48f, 52f, 48f),
                line2Color = colors.crimson, line2Data = listOf(80f, 75f, 78f, 60f, 45f, 30f, 38f),
                colors = colors,
                yLabels = listOf("100", "75", "50", "25"),
                minVal = 25f,
                maxVal = 100f
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().border(if (colors.cardBorder == Color.Transparent) 0.dp else 1.dp, colors.cardBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Autonomic Tone", color = colors.primaryText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.background(colors.teal.copy(alpha=0.15f), RoundedCornerShape(12.dp)).padding(horizontal=10.dp, vertical=4.dp)) {
                    Text("Vagal Dominant", color = colors.teal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Progress bar
            Row(modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(7.dp))) {
                Box(modifier = Modifier.weight(0.68f).fillMaxHeight().background(colors.teal))
                Box(modifier = Modifier.weight(0.32f).fillMaxHeight().background(colors.crimson))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colors.teal))
                    Text("Parasympathetic (Rest) ", color = colors.slate, fontSize = 11.sp)
                    Text("68%", color = colors.primaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colors.crimson))
                    Text("Sympathetic ", color = colors.slate, fontSize = 11.sp)
                    Text("32%", color = colors.primaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    InsightCard(colors, text = "Elevated HRV during morning scans indicates effective nervous system recovery despite midday cognitive demand.")
}

// ============================================================================
// COMPONENTS
// ============================================================================

@Composable
fun MiniStatCard(colors: StatColors, title: String, value: String, unit: String, subtitle: String, subtitleColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.border(if (colors.cardBorder == Color.Transparent) 0.dp else 1.dp, colors.cardBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = colors.slate, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = colors.primaryText, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(" $unit", color = colors.slate, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(subtitle, color = subtitleColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun InsightCard(colors: StatColors, text: String) {
    Card(
        modifier = Modifier.fillMaxWidth().border(if (colors.cardBorder == Color.Transparent) 0.dp else 1.dp, colors.cardBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, colors.gold.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .background(colors.gold.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Info, contentDescription = "Insight", tint = colors.gold, modifier = Modifier.size(20.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("WELLNESS INSIGHT", color = colors.gold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text(
                    text = text,
                    color = colors.slate, fontSize = 13.sp, lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun ScanHistoryRow(
    colors: StatColors,
    scanId: Long,
    title: String,
    metrics: String,
    status: String,
    statusColor: Color,
    onClick: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(scanId) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(colors.slate.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Favorite, contentDescription = "Heart", tint = colors.crimson, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = colors.primaryText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Details →", color = colors.slate, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(metrics, color = colors.slate, fontSize = 12.sp)
        }
        Box(
            modifier = Modifier
                .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(status, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ============================================================================
// CHARTS
// ============================================================================

val XAxisDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@Composable
fun MockAreaChart(color: Color, colors: StatColors, data: List<Float>, yLabels: List<String>, minVal: Float, maxVal: Float) {
    Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        Canvas(modifier = Modifier.fillMaxSize().padding(start = 30.dp, bottom = 20.dp, top = 10.dp)) {
            val width = size.width
            val height = size.height
            val range = (maxVal - minVal).coerceAtLeast(1f)
            val stepX = width / (data.size - 1).coerceAtLeast(1)

            // Draw Y-Axis Grid Lines
            val stepY = height / (yLabels.size - 1).coerceAtLeast(1)
            for (i in yLabels.indices) {
                drawLine(
                    color = colors.slate.copy(alpha = 0.2f),
                    start = Offset(0f, i * stepY),
                    end = Offset(width, i * stepY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }

            // Generate Path Points
            val path = Path()
            val fillPath = Path()
            val points = data.mapIndexed { index, value ->
                val normY = 1f - ((value - minVal) / range)
                val y = height * 0.15f + normY * height * 0.63f
                Offset(index * stepX, y)
            }

            // Draw Cubic Bezier Curve
            path.moveTo(points[0].x, points[0].y)
            fillPath.moveTo(points[0].x, height)
            fillPath.lineTo(points[0].x, points[0].y)

            for (i in 1 until points.size) {
                val p0 = points[i - 1]
                val p1 = points[i]
                val cpX = p0.x + (p1.x - p0.x) / 2f
                path.cubicTo(cpX, p0.y, cpX, p1.y, p1.x, p1.y)
                fillPath.cubicTo(cpX, p0.y, cpX, p1.y, p1.x, p1.y)
            }

            fillPath.lineTo(points.last().x, height)
            fillPath.close()

            // Fill Gradient
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(colors = listOf(color.copy(alpha = 0.3f), Color.Transparent))
            )

            // Stroke
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Data Dots
            points.forEach { p ->
                drawCircle(color = colors.cardBg, radius = 4.dp.toPx(), center = p)
                drawCircle(color = color, radius = 2.5.dp.toPx(), center = p)
            }
        }

        // Y-Axis Labels
        Column(modifier = Modifier.align(Alignment.TopStart).fillMaxHeight().padding(bottom = 20.dp, top = 2.dp), verticalArrangement = Arrangement.SpaceBetween) {
            yLabels.forEach { label ->
                Text(label, color = colors.slate, fontSize = 10.sp)
            }
        }

        // X-Axis Labels
        Row(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(start = 30.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            XAxisDays.forEach { day ->
                val isSun = day == "Sun"
                Text(day, color = if (isSun) colors.primaryText else colors.slate, fontSize = 10.sp, fontWeight = if (isSun) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable
fun MockLineChart(color: Color, colors: StatColors, data: List<Float>, yLabels: List<String>, minVal: Float, maxVal: Float) {
    Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        Canvas(modifier = Modifier.fillMaxSize().padding(start = 30.dp, bottom = 20.dp, top = 10.dp)) {
            val width = size.width
            val height = size.height
            val range = (maxVal - minVal).coerceAtLeast(1f)
            val stepX = width / (data.size - 1).coerceAtLeast(1)

            // Draw Y-Axis Grid Lines
            val stepY = height / (yLabels.size - 1).coerceAtLeast(1)
            for (i in yLabels.indices) {
                drawLine(
                    color = colors.slate.copy(alpha = 0.2f),
                    start = Offset(0f, i * stepY),
                    end = Offset(width, i * stepY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }

            // Generate Path Points
            val path = Path()
            val points = data.mapIndexed { index, value ->
                val normY = 1f - ((value - minVal) / range)
                val y = height * 0.15f + normY * height * 0.63f
                Offset(index * stepX, y)
            }

            // Draw Cubic Bezier Curve
            path.moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                val p0 = points[i - 1]
                val p1 = points[i]
                val cpX = p0.x + (p1.x - p0.x) / 2f
                path.cubicTo(cpX, p0.y, cpX, p1.y, p1.x, p1.y)
            }

            // Stroke
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Data Dots
            points.forEach { p ->
                drawCircle(color = colors.cardBg, radius = 4.dp.toPx(), center = p)
                drawCircle(color = color, radius = 2.5.dp.toPx(), center = p)
            }
        }

        // Y-Axis Labels
        Column(modifier = Modifier.align(Alignment.TopStart).fillMaxHeight().padding(bottom = 20.dp, top = 2.dp), verticalArrangement = Arrangement.SpaceBetween) {
            yLabels.forEach { label ->
                Text(label, color = colors.slate, fontSize = 10.sp)
            }
        }

        // X-Axis Labels
        Row(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(start = 30.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            XAxisDays.forEach { day ->
                val isSun = day == "Sun"
                Text(day, color = if (isSun) colors.primaryText else colors.slate, fontSize = 10.sp, fontWeight = if (isSun) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable
fun MockComposedChart(line1Color: Color, line1Data: List<Float>, line2Color: Color, line2Data: List<Float>, colors: StatColors, yLabels: List<String>, minVal: Float, maxVal: Float) {
    Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        Canvas(modifier = Modifier.fillMaxSize().padding(start = 30.dp, bottom = 20.dp, top = 10.dp)) {
            val width = size.width
            val height = size.height
            val range = (maxVal - minVal).coerceAtLeast(1f)
            val stepX = width / (line1Data.size - 1).coerceAtLeast(1)

            // Draw Y-Axis Grid Lines
            val stepY = height / (yLabels.size - 1).coerceAtLeast(1)
            for (i in yLabels.indices) {
                drawLine(
                    color = colors.slate.copy(alpha = 0.2f),
                    start = Offset(0f, i * stepY),
                    end = Offset(width, i * stepY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }

            fun drawLineData(data: List<Float>, color: Color) {
                val path = Path()
                val points = data.mapIndexed { index, value ->
                    val normY = 1f - ((value - minVal) / range)
                    val y = height * 0.15f + normY * height * 0.63f
                    Offset(index * stepX, y)
                }

                path.moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    val p0 = points[i - 1]
                    val p1 = points[i]
                    val cpX = p0.x + (p1.x - p0.x) / 2f
                    path.cubicTo(cpX, p0.y, cpX, p1.y, p1.x, p1.y)
                }

                drawPath(path = path, color = color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

                points.forEach { p ->
                    drawCircle(color = colors.cardBg, radius = 3.5.dp.toPx(), center = p)
                    drawCircle(color = color, radius = 2.dp.toPx(), center = p)
                }
            }

            drawLineData(line2Data, line2Color) // Draw stress first (behind)
            drawLineData(line1Data, line1Color) // Draw HRV on top
        }

        // Y-Axis Labels
        Column(modifier = Modifier.align(Alignment.TopStart).fillMaxHeight().padding(bottom = 20.dp, top = 2.dp), verticalArrangement = Arrangement.SpaceBetween) {
            yLabels.forEach { label ->
                Text(label, color = colors.slate, fontSize = 10.sp)
            }
        }

        // X-Axis Labels
        Row(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(start = 30.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            XAxisDays.forEach { day ->
                val isSun = day == "Sun"
                Text(day, color = if (isSun) colors.primaryText else colors.slate, fontSize = 10.sp, fontWeight = if (isSun) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Preview
@Composable
fun StatisticsScreenPreview() {
    StatisticsScreen()
}
