package com.example.brainrottracker.util

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.Calendar

/**
 * Returns how many milliseconds the given package was in the foreground today.
 * Requires PACKAGE_USAGE_STATS permission granted by the user in device Settings.
 */
fun getTodayForegroundTimeMs(context: Context, packageName: String): Long {
    return try {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val stats = usm.queryAndAggregateUsageStats(
            cal.timeInMillis,
            System.currentTimeMillis()
        )
        stats[packageName]?.totalTimeInForeground ?: 0L
    } catch (e: Exception) {
        0L
    }
}

/**
 * Returns the total foreground time of tracked apps (Instagram, YouTube, TikTok) in the last 24 hours.
 */
fun getTotalTimeWastedMs(context: Context): Long {
    return try {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (24 * 60 * 60 * 1000)
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        
        val trackedPackages = setOf("com.instagram.android", "com.google.android.youtube", "com.zhiliaoapp.musically")
        var totalMs = 0L
        if (stats != null) {
            for (usageStats in stats) {
                if (usageStats.packageName in trackedPackages) {
                    totalMs += usageStats.totalTimeInForeground
                }
            }
        }
        totalMs
    } catch (e: Exception) {
        0L
    }
}

/**
 * Calculates the longest continuous foreground session duration for tracked apps in the last 24 hours.
 */
fun getLongestSessionMs(context: Context): Long {
    return try {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (24 * 60 * 60 * 1000)
        val events = usm.queryEvents(startTime, endTime)
        
        val trackedPackages = setOf("com.instagram.android", "com.google.android.youtube", "com.zhiliaoapp.musically")
        val activeStartTimes = mutableMapOf<String, Long>()
        var maxSessionDuration = 0L
        
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName
            if (pkg in trackedPackages) {
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    activeStartTimes[pkg] = event.timeStamp
                } else if (event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                    val start = activeStartTimes[pkg]
                    if (start != null) {
                        val duration = event.timeStamp - start
                        if (duration > maxSessionDuration) {
                            maxSessionDuration = duration
                        }
                        activeStartTimes.remove(pkg)
                    }
                }
            }
        }
        
        // Also check if any package is currently in the foreground
        val now = System.currentTimeMillis()
        for ((pkg, start) in activeStartTimes) {
            val duration = now - start
            if (duration > maxSessionDuration) {
                maxSessionDuration = duration
            }
        }
        
        maxSessionDuration
    } catch (e: Exception) {
        0L
    }
}

/**
 * Returns true if the app has been granted the PACKAGE_USAGE_STATS permission.
 * Without this, all UsageStats queries return empty results.
 */
fun hasUsageStatsPermission(context: Context): Boolean {
    return try {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        // If we can read any stats at all, permission is granted
        val stats = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            now - 86_400_000L,
            now
        )
        stats != null && stats.isNotEmpty()
    } catch (e: Exception) {
        false
    }
}

/**
 * Converts a duration in milliseconds to a human-readable string like "1h 20m" or "45m".
 */
fun formatDurationMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0               -> "${hours}h"
        minutes > 0             -> "${minutes}m"
        ms > 0                  -> "<1m"
        else                    -> "0m"
    }
}
