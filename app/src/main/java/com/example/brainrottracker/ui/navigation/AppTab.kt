package com.example.brainrottracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Default.Home),
    Stats("Stats", Icons.Default.BarChart),
    Streak("Streak", Icons.Default.LocalFireDepartment),
    Insight("Insight", Icons.Default.Lightbulb),
    Settings("Settings", Icons.Default.Settings),
}
