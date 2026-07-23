package com.rogue.brainrottracker.ui

sealed class Screen {
    object Dashboard : Screen()
    object WeeklyUsage : Screen()
    object Settings : Screen()
    object Profile : Screen()
    object Login : Screen()
    object Friends : Screen()
    object Leaderboard : Screen()
}
