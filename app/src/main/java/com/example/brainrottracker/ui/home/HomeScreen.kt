package com.example.brainrottracker.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brainrottracker.R
import com.example.brainrottracker.ui.AppViewModel
import com.example.brainrottracker.ui.components.BrainRotCard
import com.example.brainrottracker.ui.dashboard.DashboardViewModel
import com.example.brainrottracker.ui.theme.*
import com.example.brainrottracker.util.formatDurationMs

@Composable
fun HomeScreen(
    viewModel: DashboardViewModel,
    dailyLimit: Int,
    appViewModel: AppViewModel
) {
    val stats by viewModel.todayStats.collectAsState()
    val timeWastedMs by appViewModel.timeWastedMs.collectAsState()
    val currentStreak by appViewModel.currentStreak.collectAsState()

    val instagramCount = stats["Instagram"] ?: 0
    val youtubeCount = stats["YouTube"] ?: 0
    val totalReels = instagramCount + youtubeCount
    val overLimit = totalReels > dailyLimit
    val countColor = if (overLimit) ErrorRed else SuccessGreen
    val timeText = formatDurationMs(timeWastedMs)
    val progress = (totalReels.toFloat() / dailyLimit).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.brain_icon),
                contentDescription = "Brain Rot mascot",
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 4.dp),
                contentScale = ContentScale.Fit
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "brain",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = ForegroundLight
                )
                Text(
                    text = "rot",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = LightPurple
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Welcome back!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ForegroundLight
            )
            Text(
                text = "Track your usage. Break the habit.\nTake back your time.",
                fontSize = 12.sp,
                color = MutedForeground,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BrainRotCard(modifier = Modifier.weight(1f)) {
                Text("Reels Today", fontSize = 10.sp, color = MutedForeground, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$totalReels",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = countColor,
                    lineHeight = 36.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (overLimit) "Over daily limit" else "Under daily limit",
                    fontSize = 10.sp,
                    color = if (overLimit) ErrorRed else SuccessGreen
                )
            }
            BrainRotCard(modifier = Modifier.weight(1f)) {
                Text("Time Wasted", fontSize = 10.sp, color = MutedForeground, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeText,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = ForegroundLight,
                    lineHeight = 36.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Today on reels", fontSize = 10.sp, color = MutedForeground)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        BrainRotCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Daily Limit Used", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForegroundLight)
                Text(
                    text = "$totalReels / $dailyLimit reels",
                    fontSize = 11.sp,
                    color = if (overLimit) ErrorRed else MutedForeground,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Purple950.copy(alpha = 0.8f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.horizontalGradient(
                                if (overLimit) listOf(ErrorRedDark, ErrorRed)
                                else listOf(PrimaryPurple, AccentPurple)
                            )
                        )
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (overLimit) "${totalReels - dailyLimit} reels over your limit — put it down!"
                else "${dailyLimit - totalReels} reels left today — keep it up!",
                fontSize = 10.sp,
                color = if (overLimit) ErrorRed else SuccessGreen
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickStat(Icons.Default.Schedule, "Time Wasted", timeText, Modifier.weight(1f))
            QuickStat(Icons.Default.LocalFireDepartment, "Clean Streak", "${currentStreak}d", Modifier.weight(1f))
            QuickStat(Icons.AutoMirrored.Filled.TrendingUp, "vs Yesterday", "+12%", Modifier.weight(1f))
            QuickStat(Icons.Default.LockOpen, "Goals Met", "2/7", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0x996D28D9), Color(0x4D8B5CF6))
                    )
                )
                .border(1.dp, BorderPurpleStrong, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x4D7C3AED)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = LightPurple, modifier = Modifier.size(15.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (overLimit) "You're over your limit today." else "You're doing great today.",
                    fontSize = 11.sp,
                    color = ForegroundLight.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp
                )
                Text(
                    text = if (overLimit) "Close Instagram. Do something real." else "Keep the momentum going!",
                    fontSize = 11.sp,
                    color = LightPurple,
                    lineHeight = 16.sp
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MutedForeground, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun QuickStat(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .border(1.dp, BorderPurple, RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = NavActive, modifier = Modifier.size(16.dp))
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForegroundLight)
        Text(label, fontSize = 8.sp, color = MutedForeground, lineHeight = 10.sp)
    }
}
