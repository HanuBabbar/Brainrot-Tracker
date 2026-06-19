package com.example.brainrottracker.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val stats by viewModel.todayStats.collectAsState()
    
    val instagramCount = stats["Instagram"] ?: 0
    val youtubeCount = stats["YouTube"] ?: 0

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = "Today's Consumption",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            StatCard(
                platform = "Instagram Reels",
                count = instagramCount,
                color = MaterialTheme.colorScheme.primaryContainer
            )

            StatCard(
                platform = "YouTube Shorts",
                count = youtubeCount,
                color = MaterialTheme.colorScheme.secondaryContainer
            )
            
            Text(
                text = "Total: ${instagramCount + youtubeCount}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun StatCard(platform: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = platform, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "$count",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black
            )
        }
    }
}
