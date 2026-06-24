package com.example.brainrottracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.brainrottracker.ui.components.BrainRotBottomBar
import com.example.brainrottracker.ui.dashboard.DashboardViewModel
import com.example.brainrottracker.ui.dashboard.WeeklyUsageViewModel
import com.example.brainrottracker.ui.home.HomeScreen
import com.example.brainrottracker.ui.insight.InsightScreen
import com.example.brainrottracker.ui.navigation.AppTab
import com.example.brainrottracker.ui.settings.SettingsScreen
import com.example.brainrottracker.ui.stats.StatsScreen
import com.example.brainrottracker.ui.streak.StreakScreen
import com.example.brainrottracker.ui.theme.*

@Composable
fun MainShell(
    appViewModel: AppViewModel,
    dashboardViewModel: DashboardViewModel,
    weeklyUsageViewModel: WeeklyUsageViewModel
) {
    var selectedTab by remember { mutableStateOf(AppTab.Home) }
    val dailyLimit by appViewModel.dailyLimit.collectAsState()

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            BrainRotBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                AppTab.Home -> HomeScreen(dashboardViewModel, dailyLimit, appViewModel)
                AppTab.Stats -> StatsScreen(weeklyUsageViewModel, appViewModel)
                AppTab.Streak -> StreakScreen(appViewModel)
                AppTab.Insight -> InsightScreen(dashboardViewModel)
                AppTab.Settings -> SettingsScreen(appViewModel)
            }
        }
    }
}
