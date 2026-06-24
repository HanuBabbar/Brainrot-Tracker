package com.example.brainrottracker.ui.streak

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brainrottracker.R
import com.example.brainrottracker.ui.AppViewModel
import com.example.brainrottracker.ui.components.GradientButton
import com.example.brainrottracker.ui.components.PageHeader
import com.example.brainrottracker.ui.theme.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun StreakScreen(appViewModel: AppViewModel) {
    val context = LocalContext.current
    val currentStreak by appViewModel.currentStreak.collectAsState()
    val bestStreak by appViewModel.bestStreak.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 24.dp)
    ) {
        PageHeader(overline = "Clean Days", title = "Your Streak")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF1A0A2E), Color(0xFF0F0520), Color(0xFF1A0A0A))
                    )
                )
                .border(1.dp, Color(0x40F97316), RoundedCornerShape(24.dp))
        ) {
            Image(
                painter = painterResource(R.drawable.brain_fire),
                contentDescription = "Brain on fire",
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .height(190.dp)
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Fit
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            "Days Under Limit",
                            fontSize = 11.sp,
                            color = Color(0x99FDBA74),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$currentStreak",
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            lineHeight = 72.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Best ever", fontSize = 10.sp, color = MutedForeground)
                        Text("${bestStreak}d", fontSize = 24.sp, fontWeight = FontWeight.Black, color = ForegroundLight)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(7) { index ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .then(
                                    if (index < 6) {
                                        Modifier.background(
                                            Brush.horizontalGradient(listOf(OrangeAccent, ErrorRed))
                                        )
                                    } else {
                                        Modifier
                                            .background(Color(0x1FF97316))
                                            .border(1.dp, Color(0x33F97316), RoundedCornerShape(4.dp))
                                    }
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("June 7 — June 20, 2026", fontSize = 10.sp, color = MutedForeground)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StreakStatCard("47", "days", "Total Active", Modifier.weight(1f))
            StreakStatCard("2", "times", "Breaks Taken", Modifier.weight(1f))
            StreakStatCard("21", "days", "This Month", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0x66581C87), Color(0x4D310A65))
                    )
                )
                .border(1.dp, BorderPurple, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text("Keep it going!", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ForegroundLight)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Every day you stay under your limit is a win. Your brain is recovering — don't break the chain.",
                fontSize = 11.sp,
                color = MutedForeground,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        GradientButton(
            text = "Share Streak",
            onClick = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "🧠🔥 I'm on a ${currentStreak}-day clean streak using the Brainrot Tracker app! " +
                        "I've been keeping my daily scrolling under control. " +
                        "Join me in fighting the algorithm!"
                    )
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Streak"))
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StreakStatCard(value: String, unit: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .border(1.dp, BorderPurple, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ForegroundLight)
        Text(unit, fontSize = 9.sp, color = Color(0xCCFB923C), fontWeight = FontWeight.Medium)
        Text(label, fontSize = 9.sp, color = MutedForeground, modifier = Modifier.padding(top = 2.dp))
    }
}
