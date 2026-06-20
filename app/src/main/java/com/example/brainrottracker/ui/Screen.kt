package com.example.brainrottracker.ui

sealed class Screen {
    object Dashboard : Screen()
    object WeeklyUsage : Screen()
}
