package com.example.brainrottracker.service

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.brainrottracker.data.local.AppDatabase
import com.example.brainrottracker.data.preferences.UserSettings
import com.example.brainrottracker.data.repository.UsageRepository
import com.example.brainrottracker.service.engines.InstagramEngine
import com.example.brainrottracker.service.engines.YouTubeEngine
import com.example.brainrottracker.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BrainrotTrackerService : AccessibilityService() {

    // Scope for background database operations
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var repository: UsageRepository
    private lateinit var userSettings: UserSettings
    
    // Performance throttling
    private var lastTreeCheckTime = 0L
    private var currentIntervalMs = 750L // Default Medium

    // Decoupled engines
    private lateinit var youtubeEngine: YouTubeEngine
    private lateinit var instagramEngine: InstagramEngine

    companion object {
        fun isServiceEnabled(context: Context): Boolean {
            val expectedComponentName = ComponentName(context, BrainrotTrackerService::class.java)
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            return enabledServices.contains(expectedComponentName.flattenToString())
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val packageName = event.packageName?.toString() ?: return

        if (packageName != "com.instagram.android" && packageName != "com.google.android.youtube") return

        val now = System.currentTimeMillis()

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                Log.d("BrainrotTracker", "Focus: $packageName")
                if (packageName == "com.google.android.youtube") {
                    youtubeEngine.resetDumpFlag()
                }
            }
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                // Scrolls are high-signal, we don't throttle them as much as content changes
                if (packageName == "com.instagram.android") {
                    val root = rootInActiveWindow ?: return
                    instagramEngine.detectSwipe(root) {
                        saveSwipe("Instagram")
                    }
                    root.recycle()
                }
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                // Throttle heavy tree scans based on user preference
                if (now - lastTreeCheckTime < currentIntervalMs) return
                lastTreeCheckTime = now

                val root = rootInActiveWindow ?: return
                if (packageName == "com.google.android.youtube") {
                    youtubeEngine.detectSwipe(root) {
                        saveSwipe("YouTube")
                    }
                } else if (packageName == "com.instagram.android") {
                    instagramEngine.detectSwipe(root) {
                        saveSwipe("Instagram")
                    }
                }
                root.recycle()
            }
        }
    }

    private fun saveSwipe(platform: String) {
        serviceScope.launch {
            repository.incrementUsage(platform)
        }
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()

        // Setup database and repository
        val database = AppDatabase.getDatabase(this)
        userSettings = UserSettings(this)
        val notificationHelper = NotificationHelper(this)
        repository = UsageRepository(database.usageDao(), userSettings, notificationHelper)

        // Initialize engines
        youtubeEngine = YouTubeEngine(resources)
        instagramEngine = InstagramEngine(resources)

        // Observe CPU mode changes
        serviceScope.launch {
            userSettings.cpuMode.collect { mode ->
                currentIntervalMs = mode.intervalMs
                Log.d("BrainrotTracker", "CPU Mode updated: ${mode.name} (${mode.intervalMs}ms)")
            }
        }

        Log.d("BrainrotTracker", "Service Connected and Engines Ready!")
    }
}
