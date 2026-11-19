package com.example.cardioveyra.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardioveyra.ui.screens.about.AboutScreen
import com.example.cardioveyra.ui.theme.ThemeState

// --- Theme Tokens ---
data class SettingsColors(
    val bg: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val teal: Color,
    val crimson: Color = Color(0xFFBA002C),
    val softRed: Color = Color(0xFFF87171)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    // 1. Determine Theme
    val systemDark = isSystemInDarkTheme()
    val isDarkTheme = ThemeState.isDarkTheme ?: systemDark
    
    val colors = SettingsColors(
        bg = if (isDarkTheme) Color(0xFF0B0F19) else Color(0xFFF8FAFC),
        cardBg = if (isDarkTheme) Color(0xFF131826) else Color(0xFFFFFFFF),
        cardBorder = if (isDarkTheme) Color(0xFF1E2538) else Color(0xFFE2E8F0),
        textPrimary = if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF0F172A),
        textSecondary = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
        teal = if (isDarkTheme) Color(0xFF2DD4BF) else Color(0xFF0D9488)
    )

    // 2. State Variables
    // Theming logic connects to global state
    var selectedThemeIndex by rememberSaveable { 
        mutableIntStateOf(
            when (ThemeState.isDarkTheme) {
                true -> 0
                false -> 1
                null -> 2
            }
        ) 
    }
    
    var showScanGuide by rememberSaveable { mutableStateOf(true) }
    var hapticFeedback by rememberSaveable { mutableStateOf(true) }
    var selectedDurationIndex by rememberSaveable { mutableIntStateOf(0) }
    var strictSignalCheck by rememberSaveable { mutableStateOf(true) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showDisclaimer by rememberSaveable { mutableStateOf(false) }
    var showAboutScreen by rememberSaveable { mutableStateOf(false) }

    if (showAboutScreen) {
        AboutScreen(onBackClick = { showAboutScreen = false })
    } else {
        // 3. Main Scaffold Layout
        Scaffold(
            containerColor = colors.bg,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.bg)
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "PREFERENCES",
                        color = colors.crimson,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Settings",
                        color = colors.textPrimary,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                
                // SECTION 1: APPEARANCE & EXPERIENCE
                SettingsSection(title = "APPEARANCE & EXPERIENCE", colors = colors) {
                    // Theme Selector
                    SettingsCustomRow(
                        colors = colors,
                        icon = Icons.Default.DarkMode,
                        iconTint = colors.teal,
                        title = "Interface Theme",
                        subtitle = "Current: " + listOf("Dark Mode", "Light Mode", "System Default")[selectedThemeIndex]
                    ) {
                        SegmentedPillToggle(
                            colors = colors,
                            options = listOf("Dark", "Light", "System"),
                            selectedIndex = selectedThemeIndex,
                            onOptionSelected = { index -> 
                                selectedThemeIndex = index 
                                ThemeState.isDarkTheme = when(index) {
                                    0 -> true
                                    1 -> false
                                    else -> null
                                }
                            }
                        )
                    }
                    HorizontalDivider(color = colors.cardBorder)
                    // Show Scan Guide
                    SettingsSwitchRow(
                        colors = colors,
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        iconTint = colors.teal,
                        title = "Show Scan Guide",
                        subtitle = "Show 3–step instructions before each scan",
                        checked = showScanGuide,
                        onCheckedChange = { showScanGuide = it }
                    )
                    HorizontalDivider(color = colors.cardBorder)
                    // Haptic Feedback
                    SettingsSwitchRow(
                        colors = colors,
                        icon = Icons.Default.Vibration,
                        iconTint = colors.teal,
                        title = "Haptic Feedback",
                        subtitle = "Vibrate on scan start and completion",
                        checked = hapticFeedback,
                        onCheckedChange = { hapticFeedback = it }
                    )
                }

                // SECTION 2: SCAN PREFERENCES
                SettingsSection(title = "SCAN PREFERENCES", colors = colors) {
                    // Scan Duration
                    SettingsCustomRow(
                        colors = colors,
                        icon = Icons.Default.Timer,
                        iconTint = colors.teal,
                        title = "Scan Duration",
                        subtitle = "Full HRV requires 60 seconds optical data"
                    ) {
                        SegmentedPillToggleTwoRows(
                            colors = colors,
                            option1Title = "60s (Full HRV)", option1Sub = "Recommended",
                            option2Title = "30s (Quick HR)", option2Sub = "BPM Only",
                            selectedIndex = selectedDurationIndex,
                            onOptionSelected = { selectedDurationIndex = it }
                        )
                    }
                    HorizontalDivider(color = colors.cardBorder)
                    // Strict Signal Check
                    SettingsSwitchRow(
                        colors = colors,
                        icon = Icons.Default.Timeline,
                        iconTint = colors.teal,
                        title = "Strict Signal Check",
                        subtitle = "Auto-pause if motion or lighting drops quality",
                        checked = strictSignalCheck,
                        onCheckedChange = { strictSignalCheck = it }
                    )
                }

                // SECTION 3: DATA & PRIVACY
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "DATA & PRIVACY",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    // Privacy Banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.teal.copy(alpha = 0.1f))
                            .border(1.dp, colors.teal.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = "Privacy", tint = colors.teal)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("100% On-Device Processing", color = colors.teal, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Camera frames stay in volatile memory and are never saved, recorded, or uploaded.", color = colors.textSecondary, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }

                    SettingsGroupCard(colors) {
                        SettingsNavigationRow(
                            colors = colors,
                            icon = Icons.Default.Download,
                            iconTint = colors.teal,
                            title = "Export Scan History",
                            subtitle = "Export as CSV or PDF report",
                            onClick = { /* Export action */ }
                        )
                        HorizontalDivider(color = colors.cardBorder)
                        SettingsNavigationRow(
                            colors = colors,
                            icon = Icons.Default.DeleteOutline,
                            iconTint = colors.softRed,
                            title = "Clear Scan History",
                            subtitle = "Delete all cached pulse & HRV records",
                            titleColor = colors.softRed,
                            iconBgTint = colors.softRed.copy(alpha = 0.1f),
                            onClick = { showDeleteDialog = true }
                        )
                    }
                }

                // SECTION 4: ABOUT & COMPLIANCE
                SettingsSection(title = "ABOUT & COMPLIANCE", colors = colors) {
                    SettingsNavigationRow(
                        colors = colors,
                        icon = Icons.Default.Info,
                        iconTint = colors.teal,
                        title = "About CardioVeyra",
                        subtitle = "App version, mission, and how it works",
                        onClick = { showAboutScreen = true }
                    )
                    HorizontalDivider(color = colors.cardBorder)
                    SettingsNavigationRow(
                        colors = colors,
                        icon = Icons.Default.HealthAndSafety,
                        iconTint = colors.teal,
                        title = "Wellness Disclaimer",
                        subtitle = "General wellness & non-diagnostic notice",
                        onClick = { showDisclaimer = true }
                    )
                    HorizontalDivider(color = colors.cardBorder)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(colors.teal.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Memory, contentDescription = "Engine", tint = colors.teal, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text(text = "CardioVeyra v1.0.0", color = colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "On-Device TFLite rPPG Engine", color = colors.textSecondary, fontSize = 13.sp)
                            }
                        }
                        // Build Badge
                        Box(
                            modifier = Modifier
                                .border(1.dp, colors.teal.copy(alpha=0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Build 104", color = colors.teal, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp)) // Nav bar clearance
            }
        }

        // Dialogs & Modals
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                containerColor = colors.cardBg,
                titleContentColor = colors.textPrimary,
                textContentColor = colors.textSecondary,
                title = { Text("Clear Scan History?", fontWeight = FontWeight.Bold) },
                text = { Text("This will permanently delete all pulse and HRV data cached on this device. This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Delete", color = colors.softRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel", color = colors.textPrimary)
                    }
                }
            )
        }

        if (showDisclaimer) {
            ModalBottomSheet(
                onDismissRequest = { showDisclaimer = false },
                containerColor = colors.bg,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Default.WarningAmber, contentDescription = "Disclaimer", tint = Color(0xFFD4AF37), modifier = Modifier.size(32.dp))
                    Text("Wellness Disclaimer", color = colors.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "CardioVeyra is intended for general wellness and fitness tracking. It is not a medical device. " +
                        "The signals extracted via optical photoplethysmography (rPPG) are not intended for use in the diagnosis of disease or other conditions, " +
                        "or in the cure, mitigation, treatment, or prevention of disease.",
                        color = colors.textSecondary, lineHeight = 20.sp
                    )
                    Button(
                        onClick = { showDisclaimer = false },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.crimson,
                            contentColor = Color.White
                        )
                    ) {
                        Text("I Understand")
                    }
                }
            }
        }
    }
}

// ============================================================================
// REUSABLE COMPONENTS
// ============================================================================

@Composable
fun SettingsSection(title: String, colors: SettingsColors, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            color = colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
        SettingsGroupCard(colors = colors, content = content)
    }
}

@Composable
fun SettingsGroupCard(colors: SettingsColors, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsCustomRow(
    colors: SettingsColors,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    customContent: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(text = title, color = colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, color = colors.textSecondary, fontSize = 13.sp)
            }
        }
        customContent()
    }
}

@Composable
fun SettingsSwitchRow(
    colors: SettingsColors,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(text = title, color = colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, color = colors.textSecondary, fontSize = 13.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.crimson,
                uncheckedThumbColor = colors.textSecondary,
                uncheckedTrackColor = colors.bg,
                uncheckedBorderColor = colors.cardBorder
            )
        )
    }
}

@Composable
fun SettingsNavigationRow(
    colors: SettingsColors,
    icon: ImageVector,
    iconTint: Color,
    iconBgTint: Color = iconTint.copy(alpha = 0.12f),
    title: String,
    titleColor: Color = colors.textPrimary,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBgTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(text = title, color = titleColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, color = colors.textSecondary, fontSize = 13.sp)
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Navigate", tint = colors.textSecondary)
    }
}

@Composable
fun SegmentedPillToggle(
    colors: SettingsColors,
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.bg)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, title ->
            val isSelected = selectedIndex == index
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) colors.crimson else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // Removes ripple for cleaner pill feel
                    ) { onOptionSelected(index) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = if (isSelected) Color.White else colors.textSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun SegmentedPillToggleTwoRows(
    colors: SettingsColors,
    option1Title: String, option1Sub: String,
    option2Title: String, option2Sub: String,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.bg)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val options = listOf(Pair(option1Title, option1Sub), Pair(option2Title, option2Sub))
        
        options.forEachIndexed { index, pair ->
            val isSelected = selectedIndex == index
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) colors.crimson else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onOptionSelected(index) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = pair.first,
                    color = if (isSelected) Color.White else colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = pair.second,
                    color = if (isSelected) Color.White.copy(alpha=0.8f) else colors.textSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(name = "Dark Mode", showBackground = true)
@Composable
fun SettingsScreenDarkPreview() {
    ThemeState.isDarkTheme = true
    SettingsScreen()
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun SettingsScreenLightPreview() {
    ThemeState.isDarkTheme = false
    SettingsScreen()
}
