package com.example.cardioveyra.ui.onboarding

import com.example.cardioveyra.R
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardioveyra.ui.theme.ThemeState
import kotlinx.coroutines.launch

data class OnboardingPage(
    val imageRes: Int,
    val titlePrefix: String,
    val titleHighlight: String,
    val description: String
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit = {}
) {
    val isDarkTheme = ThemeState.isDarkTheme ?: isSystemInDarkTheme()
    
    val backgroundColor = if (isDarkTheme) Color(0xFF0B0F19) else Color(0xFFF7F8FA)
    val brandAccent = if (isDarkTheme) Color(0xFFFF5A5F) else Color(0xFFBA002C)
    val titleTextColor = if (isDarkTheme) Color.White else Color(0xFF1C2025)
    val descriptionColor = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color(0xFF6B7280)
    val indicatorInactiveColor = if (isDarkTheme) Color.DarkGray else Color(0xFFD1D5DB)
    val skipButtonColor = if (isDarkTheme) Color.Gray else Color(0xFF6B7280)

    val pages = listOf(
        OnboardingPage(
            imageRes = if (isDarkTheme) R.drawable.forehead else R.drawable.forehead_light,
            titlePrefix = "Clear Your ",
            titleHighlight = "Forehead",
            description = "Please move your hair or glasses away from your forehead for an accurate scan."
        ),
        OnboardingPage(
            imageRes = if (isDarkTheme) R.drawable.lighting else R.drawable.lighitng_light,
            titlePrefix = "Find ",
            titleHighlight = "Even Lighting",
            description = "Face a steady light source so your face is evenly lit for an accurate scan."
        ),
        OnboardingPage(
            imageRes = if (isDarkTheme) R.drawable.hold_perfectly_still else R.drawable.hold_perfectly_still_light,
            titlePrefix = "Hold Perfectly ",
            titleHighlight = "Still",
            description = "Rest your phone on a table or hold it at eye level without moving for 60 seconds."
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = backgroundColor,
        contentWindowInsets = WindowInsets.systemBars
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Step Indicator (Top Right)
            val stepIndicatorBg = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)
            val stepIndicatorText = if (isDarkTheme) Color.White else Color.DarkGray
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
                    .background(stepIndicatorBg, CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "STEP ${pagerState.currentPage + 1} OF ${pages.size}",
                    color = stepIndicatorText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                val page = pages[pageIndex]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Image with Ambient Glow
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(340.dp) // Slightly larger container for glow
                    ) {
                        // Ambient Glow effect (only really visible in dark mode, or very subtle in light)
                        Box(
                            modifier = Modifier
                                .size(280.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            if (isDarkTheme) brandAccent.copy(alpha = 0.15f) else Color.Transparent,
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .blur(48.dp)
                        )

                        // Center Image
                        Image(
                            painter = painterResource(id = page.imageRes),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(280.dp)
                                .clip(RoundedCornerShape(32.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // Title
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = titleTextColor)) {
                                append(page.titlePrefix)
                            }
                            withStyle(style = SpanStyle(color = brandAccent)) {
                                append(page.titleHighlight)
                            }
                        },
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    Text(
                        text = page.description,
                        color = descriptionColor,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }

            // Bottom Navigation Bar
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip Button
                TextButton(
                    onClick = onFinish,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Skip",
                        color = skipButtonColor,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Custom Pager Indicator
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { iteration ->
                        val isSelected = pagerState.currentPage == iteration
                        val color by animateColorAsState(
                            targetValue = if (isSelected) brandAccent else indicatorInactiveColor,
                            animationSpec = tween(300), 
                            label = "indicatorColor"
                        )
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 8.dp,
                            animationSpec = tween(300), 
                            label = "indicatorWidth"
                        )

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(width)
                                .background(color, CircleShape)
                        )
                    }
                }

                // Next / Get Started Button
                val isLastPage = pagerState.currentPage == pages.size - 1
                TextButton(
                    onClick = {
                        if (isLastPage) {
                            onFinish()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isLastPage) "Get Started" else "Next",
                            color = brandAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        if (!isLastPage) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next",
                                tint = brandAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
