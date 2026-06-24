package com.example.brainrottracker.ui.insight

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brainrottracker.R
import com.example.brainrottracker.ui.components.BrainRotCard
import com.example.brainrottracker.ui.components.GradientButton
import com.example.brainrottracker.ui.dashboard.DashboardViewModel
import com.example.brainrottracker.ui.theme.*

private val insights = listOf(
    "Every reel you skip\nis 30 seconds\ngiven back to yourself.",
    "The algorithm is designed\nto keep you watching.\nYou are designed to do more.",
    "What would you build\nwith the 2 hours\nyou spend on reels today?",
    "You don't miss the reels\nyou never watched.\nPut the phone down.",
    "Your attention is the product.\nStop giving it away for free.",
)

@Composable
fun InsightScreen(viewModel: DashboardViewModel) {
    val stats by viewModel.todayStats.collectAsState()
    val totalReels = (stats["Instagram"] ?: 0) + (stats["YouTube"] ?: 0)
    var insightIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x338B5CF6))
                    .border(1.dp, BorderPurpleStrong, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = NavActive, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("WISDOM", fontSize = 11.sp, color = MutedForeground, letterSpacing = 1.sp)
                Text("Daily Insight", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ForegroundLight, lineHeight = 20.sp)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF0E0B24), Color(0xFF150D30), Color(0xFF0A0A1A))
                    )
                )
                .border(1.dp, BorderPurpleStrong, RoundedCornerShape(24.dp))
        ) {
            Image(
                painter = painterResource(R.drawable.brain_couch),
                contentDescription = "Brain on couch",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 20.dp)) {
                Text("\"", fontSize = 13.sp, color = MutedForeground, fontStyle = FontStyle.Italic)
                Text(
                    text = insights[insightIndex],
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForegroundLight,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    insights.indices.forEach { i ->
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (i == insightIndex) 20.dp else 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (i == insightIndex) NavActive else Color(0x408B5CF6)
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        GradientButton(
            text = "Another Insight",
            onClick = { insightIndex = (insightIndex + 1) % insights.size },
            colors = listOf(Color(0xFF6D28D9), AccentPurple)
        )

        Spacer(modifier = Modifier.height(20.dp))

        BrainRotCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                "TODAY'S DEEP STATS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = MutedForeground,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            val deepStats = listOf(
                Triple(Icons.Default.Schedule, "Time lost to reels today", formatTimeLost(totalReels)),
                Triple(Icons.Default.FitnessCenter, "Could have gone for a run", if (totalReels > 100) "yes" else "maybe"),
                Triple(Icons.Default.Bolt, "Dopamine hits received", "$totalReels"),
                Triple(Icons.Default.Memory, "Deep work sessions done", "0"),
            )
            deepStats.forEachIndexed { index, (icon, label, value) ->
                DeepStatRow(icon, label, value, showDivider = index < deepStats.lastIndex)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0x59581C87), Color(0x40310A65))
                    )
                )
                .border(1.dp, Color(0x2E8B5CF6), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text("Did You Know?", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LightPurple, modifier = Modifier.padding(bottom = 8.dp))
            Text(
                "Instagram reels are engineered by a team of PhDs to maximize the time you spend watching. You are not weak — the system is rigged. But you can still win.",
                fontSize = 11.sp,
                color = ForegroundLight.copy(alpha = 0.6f),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun DeepStatRow(icon: ImageVector, label: String, value: String, showDivider: Boolean) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MutedForeground, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(label, fontSize = 11.sp, color = ForegroundLight.copy(alpha = 0.7f))
            }
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LightPurple)
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0x668B5CF6).copy(alpha = 0.25f))
            )
        }
    }
}

private fun formatTimeLost(reels: Int): String {
    val totalMinutes = reels * 30 / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "0m"
    }
}
