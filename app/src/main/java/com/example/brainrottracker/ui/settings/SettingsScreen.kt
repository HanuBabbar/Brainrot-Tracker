package com.example.brainrottracker.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brainrottracker.R
import com.example.brainrottracker.data.preferences.CPUMode
import com.example.brainrottracker.ui.AppViewModel
import com.example.brainrottracker.ui.components.BrainRotToggle
import com.example.brainrottracker.ui.components.PageHeader
import com.example.brainrottracker.ui.components.SectionLabel
import com.example.brainrottracker.ui.theme.*

@Composable
fun SettingsScreen(appViewModel: AppViewModel) {
    val notifications by remember { mutableStateOf(true) }
    val haptic by remember { mutableStateOf(true) }
    val shameCounter by remember { mutableStateOf(false) }
    val soundEffects by remember { mutableStateOf(false) }
    val brainRotBot by remember { mutableStateOf(true) }

    // Persisted settings from ViewModel
    val darkMode by appViewModel.darkMode.collectAsState()
    val dailyLimit by appViewModel.dailyLimit.collectAsState()
    val breakReminder by appViewModel.breakReminder.collectAsState()
    val cpuMode by appViewModel.cpuMode.collectAsState()

    // Local UI state for toggles controlling expansion
    var dailyLimitEnabled by remember { mutableStateOf(true) }
    var bioBreakEnabled by remember { mutableStateOf(true) }

    // Dialog states
    var showLimitDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 24.dp)
    ) {
        PageHeader(overline = "Brain Rot", title = "Settings")

        // Profile Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardDark)
                .border(1.dp, BorderPurple, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x268B5CF6))
                    .border(1.dp, BorderPurpleStrong, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.brain_icon),
                    contentDescription = "Brain mascot",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("brain rot user", fontWeight = FontWeight.Bold, color = ForegroundLight)
                Text("Level 4 Scroller · 640 XP", fontSize = 10.sp, color = MutedForeground)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Purple950)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(Brush.horizontalGradient(listOf(PrimaryPurple, LightPurple)))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionLabel("Preferences")
        SettingGroup {
            // Usage Alerts
            SettingRow(
                icon = Icons.Default.Notifications,
                iconBg = Color(0x664C1D95),
                label = "Usage Alerts",
                desc = "Warn when approaching limit",
                checked = notifications,
                onCheckedChange = {},
                showDivider = true
            )

            // Daily Limit toggle + adjuster
            SettingRow(
                icon = Icons.Default.TrackChanges,
                iconBg = Color(0x664C1D95),
                label = "Daily Limit",
                desc = "Currently: $dailyLimit reels/day",
                checked = dailyLimitEnabled,
                onCheckedChange = { dailyLimitEnabled = it },
                showDivider = dailyLimitEnabled
            )
            if (dailyLimitEnabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Decrease button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x338B5CF6))
                            .border(1.dp, BorderPurpleStrong, RoundedCornerShape(10.dp))
                            .clickable {
                                if (dailyLimit > 10) appViewModel.setDailyLimit(dailyLimit - 10)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LightPurple)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x1A8B5CF6))
                            .border(1.dp, BorderPurple, RoundedCornerShape(10.dp))
                            .clickable { showLimitDialog = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$dailyLimit reels",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForegroundLight,
                            textAlign = TextAlign.Center
                        )
                    }
                    // Increase button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x338B5CF6))
                            .border(1.dp, BorderPurpleStrong, RoundedCornerShape(10.dp))
                            .clickable { appViewModel.setDailyLimit(dailyLimit + 10) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LightPurple)
                    }
                }
                // Divider before next row
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(1.dp)
                        .background(Color(0x668B5CF6).copy(alpha = 0.25f))
                )
            }

            // Break Reminder toggle + adjuster
            SettingRow(
                icon = Icons.Default.Timer,
                iconBg = Color(0x664C1D95),
                label = "Break Reminder",
                desc = "Every $breakReminder min",
                checked = bioBreakEnabled,
                onCheckedChange = { bioBreakEnabled = it },
                showDivider = bioBreakEnabled
            )
            if (bioBreakEnabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x338B5CF6))
                            .border(1.dp, BorderPurpleStrong, RoundedCornerShape(10.dp))
                            .clickable {
                                if (breakReminder > 5) appViewModel.setBreakReminder(breakReminder - 5)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LightPurple)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x1A8B5CF6))
                            .border(1.dp, BorderPurple, RoundedCornerShape(10.dp))
                            .clickable { showReminderDialog = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$breakReminder min",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForegroundLight,
                            textAlign = TextAlign.Center
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x338B5CF6))
                            .border(1.dp, BorderPurpleStrong, RoundedCornerShape(10.dp))
                            .clickable { appViewModel.setBreakReminder(breakReminder + 5) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LightPurple)
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(1.dp)
                        .background(Color(0x668B5CF6).copy(alpha = 0.25f))
                )
            }

            SettingRow(
                icon = Icons.Default.Bolt,
                iconBg = Color(0x664C1D95),
                label = "Haptic Warning",
                desc = "Buzz when over limit",
                checked = haptic,
                onCheckedChange = {},
                showDivider = true
            )
            SettingRow(
                icon = Icons.Default.DarkMode,
                iconBg = Color(0x664C1D95),
                label = "Dark Mode",
                desc = if (darkMode) "Currently: Dark" else "Currently: Light",
                checked = darkMode,
                onCheckedChange = { appViewModel.setDarkMode(it) },
                showDivider = true
            )

            // CPU Performance Mode Custom Selector
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x664C1D95)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = NavActive, modifier = Modifier.size(15.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("CPU Mode", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ForegroundLight)
                        Text("Lower modes save battery but may miss swipes", fontSize = 10.sp, color = MutedForeground)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CPUMode.entries.forEach { mode ->
                        val selected = cpuMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) Color(0x338B5CF6) else Color(0x1A8B5CF6))
                                .border(1.dp, if (selected) PrimaryPurple else BorderPurple, RoundedCornerShape(10.dp))
                                .clickable { appViewModel.setCPUMode(mode) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.name,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) LightPurple else ForegroundLight
                            )
                        }
                    }
                }
            }
        }

        SectionLabel("Fun Stuff")
        SettingGroup {
            SettingRow(
                icon = Icons.Default.Bolt,
                iconBg = Color(0x4D701A75),
                label = "Public Shame Mode",
                desc = "Show daily count on lock screen",
                checked = shameCounter,
                onCheckedChange = {},
                accent = FuchsiaAccent,
                showDivider = true
            )
            SettingRow(
                icon = Icons.Default.VolumeUp,
                iconBg = Color(0x4D701A75),
                label = "Guilt Sound",
                desc = "Plays a sound when over limit",
                checked = soundEffects,
                onCheckedChange = {},
                accent = FuchsiaAccent,
                showDivider = true
            )
            SettingRow(
                icon = Icons.Default.SmartToy,
                iconBg = Color(0x4D701A75),
                label = "Intervention Bot",
                desc = "AI checks in to help you quit",
                checked = brainRotBot,
                onCheckedChange = {},
                accent = FuchsiaAccent,
                showDivider = false
            )
        }

        // Danger Zone
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x1A450A0A))
                .border(1.dp, Color(0x80450A0A), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                "DANGER ZONE",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = Color(0x99EF4444),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0x4DB91C1C), RoundedCornerShape(12.dp))
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Reset All Stats",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xB3F87171)
                )
            }
        }
    }

    // Daily Limit custom input dialog
    if (showLimitDialog) {
        var inputText by remember { mutableStateOf(dailyLimit.toString()) }
        AlertDialog(
            onDismissRequest = { showLimitDialog = false },
            containerColor = CardDark,
            titleContentColor = ForegroundLight,
            textContentColor = MutedForeground,
            title = { Text("Set Daily Limit", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter a number between 10 and 1000 reels:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it.filter { c -> c.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        suffix = { Text("reels", color = MutedForeground, fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = BorderPurpleStrong,
                            focusedTextColor = ForegroundLight,
                            unfocusedTextColor = ForegroundLight,
                            cursorColor = PrimaryPurple
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val v = inputText.toIntOrNull()
                    if (v != null && v in 10..1000) {
                        appViewModel.setDailyLimit(v)
                    }
                    showLimitDialog = false
                }) {
                    Text("Set", color = LightPurple, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLimitDialog = false }) {
                    Text("Cancel", color = MutedForeground)
                }
            }
        )
    }

    // Break Reminder custom input dialog
    if (showReminderDialog) {
        var inputText by remember { mutableStateOf(breakReminder.toString()) }
        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            containerColor = CardDark,
            titleContentColor = ForegroundLight,
            textContentColor = MutedForeground,
            title = { Text("Set Break Reminder", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter interval in minutes (5–120):", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it.filter { c -> c.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        suffix = { Text("min", color = MutedForeground, fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = BorderPurpleStrong,
                            focusedTextColor = ForegroundLight,
                            unfocusedTextColor = ForegroundLight,
                            cursorColor = PrimaryPurple
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val v = inputText.toIntOrNull()
                    if (v != null && v in 5..120) {
                        appViewModel.setBreakReminder(v)
                    }
                    showReminderDialog = false
                }) {
                    Text("Set", color = LightPurple, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReminderDialog = false }) {
                    Text("Cancel", color = MutedForeground)
                }
            }
        )
    }
}

@Composable
private fun SettingGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .border(1.dp, BorderPurple, RoundedCornerShape(16.dp))
            .padding(vertical = 4.dp),
        content = content
    )
    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    iconBg: Color,
    label: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accent: Color = PrimaryPurple,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = if (accent == FuchsiaAccent) FuchsiaAccent else NavActive, modifier = Modifier.size(15.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ForegroundLight)
                    Text(desc, fontSize = 10.sp, color = MutedForeground)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            BrainRotToggle(checked = checked, onCheckedChange = onCheckedChange, accent = accent)
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(1.dp)
                    .background(Color(0x668B5CF6).copy(alpha = 0.25f))
            )
        }
    }
}
