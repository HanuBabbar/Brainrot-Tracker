package com.rogue.brainrottracker.ui

sealed class Screen {
    object Dashboard : Screen()
    object WeeklyUsage : Screen()
    object Settings : Screen()
    object Profile : Screen()
    object Login : Screen()
    data class Friends(val initialCode: String = "BRT-") : Screen()
    object Leaderboard : Screen()
}
