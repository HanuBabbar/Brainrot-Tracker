package com.example.brainrottracker.data.preferences

enum class CPUMode(val intervalMs: Long) {
    HIGH(500L),
    MEDIUM(750L),
    LOW(1200L)
}
