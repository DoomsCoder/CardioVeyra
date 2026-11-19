package com.example.cardioveyra.ui.screens.about

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.isSystemInDarkTheme
import com.example.cardioveyra.ui.theme.ThemeState

data class AboutColors(
    val bgColor: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val crimson: Color,
    val teal: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val iconBg: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackClick: () -> Unit = {}
) {
    BackHandler {
        onBackClick()
    }
    
    val isDarkTheme = ThemeState.isDarkTheme ?: isSystemInDarkTheme()
    val colors = AboutColors(
        bgColor = if (isDarkTheme) Color(0xFF0B0F19) else Color(0xFFF8FAFC),
        cardBg = if (isDarkTheme) Color(0xFF131826) else Color(0xFFFFFFFF),
        cardBorder = if (isDarkTheme) Color(0xFF1E2538) else Color(0xFFE2E8F0),
        crimson = Color(0xFFBA002C),
        teal = if (isDarkTheme) Color(0xFF2DD4BF) else Color(0xFF0D9488),
        primaryText = if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF0F172A),
        secondaryText = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
        iconBg = if (isDarkTheme) Color(0xFF1E2538) else Color(0xFFE2E8F0)
    )

    Scaffold(
        containerColor = colors.bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "About CardioVeyra",
                        color = colors.primaryText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.primaryText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.bgColor,
                    scrolledContainerColor = colors.bgColor
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Section 1: Hero Header
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.crimson.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Heart Pulse",
                        tint = colors.crimson,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "CardioVeyra",
                        color = colors.primaryText,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(colors.iconBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "v1.0.0",
                            color = colors.secondaryText,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Check your heart rate and HRV using just your phone camera.",
                    color = colors.secondaryText,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 320.dp),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Section 2: WHY USE CARDIOVEYRA
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "WHY USE CARDIOVEYRA",
                        color = colors.crimson,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                    )

                    FeatureCard(
                        colors = colors,
                        icon = Icons.Outlined.PhoneAndroid,
                        iconTint = colors.secondaryText,
                        iconBg = colors.iconBg,
                        title = "No Wearable Needed",
                        description = "Skip the smartwatch or chest strap. Your front camera reads subtle light changes in your skin to estimate your pulse in 60 seconds."
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FeatureCard(
                        colors = colors,
                        icon = Icons.Outlined.Shield,
                        iconTint = colors.teal,
                        iconBg = colors.teal.copy(alpha = 0.15f),
                        title = "Stays On Your Phone",
                        description = "All processing happens locally in temporary memory. Your camera feed is never recorded, saved, or sent to any server."
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FeatureCard(
                        colors = colors,
                        icon = Icons.AutoMirrored.Outlined.ShowChart,
                        iconTint = colors.crimson,
                        iconBg = colors.crimson.copy(alpha = 0.15f),
                        title = "Know Your Baseline",
                        description = "Track your resting heart rate, HRV, and stress balance over time to spot when your body is recovered or needs rest."
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Section 3: HOW IT WORKS
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "HOW IT WORKS",
                        color = colors.crimson,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StepItem(
                                colors = colors,
                                icon = Icons.Outlined.WbSunny,
                                iconTint = Color(0xFFD4AF37),
                                title = "1. Face Light",
                                subtitle = "Even, soft illumination",
                                modifier = Modifier.weight(1f)
                            )
                            StepItem(
                                colors = colors,
                                icon = Icons.Outlined.Timer,
                                iconTint = colors.teal,
                                title = "2. Hold Still 60s",
                                subtitle = "Keep camera at eye level",
                                modifier = Modifier.weight(1f)
                            )
                            StepItem(
                                colors = colors,
                                icon = Icons.AutoMirrored.Outlined.TrendingUp,
                                iconTint = colors.crimson,
                                title = "3. View Trends",
                                subtitle = "HRV, pulse & recovery",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(64.dp))

                // Section 4: Footer Compliance Note
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Info",
                        tint = colors.secondaryText,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Built for everyday wellness tracking. CardioVeyra is not a medical device and does not diagnose health conditions.",
                        color = colors.secondaryText,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.widthIn(max = 280.dp)
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun FeatureCard(
    colors: AboutColors,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    color = colors.primaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = colors.secondaryText,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun StepItem(
    colors: AboutColors,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            color = colors.primaryText,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            color = colors.secondaryText,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0F19)
@Composable
fun AboutScreenPreview() {
    MaterialTheme {
        AboutScreen()
    }
}