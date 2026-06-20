package com.example.brainrottracker.service

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.brainrottracker.data.local.AppDatabase
import com.example.brainrottracker.data.repository.UsageRepository
import com.example.brainrottracker.service.engines.InstagramEngine
import com.example.brainrottracker.service.engines.YouTubeEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BrainrotTrackerService : AccessibilityService() {

    // Scope for background database operations
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var repository: UsageRepository

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

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                Log.d("BrainrotTracker", "Focus: $packageName")
                if (packageName == "com.google.android.youtube") {
                    youtubeEngine.resetDumpFlag()
                }
            }
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                if (packageName == "com.instagram.android") {
                    val root = rootInActiveWindow ?: return
                    instagramEngine.detectSwipe(root) {
                        saveSwipe("Instagram")
                    }
                    root.recycle()
                }
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
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
        repository = UsageRepository(database.usageDao())

        // Initialize engines
        youtubeEngine = YouTubeEngine(resources)
        instagramEngine = InstagramEngine(resources)

        Log.d("BrainrotTracker", "Service Connected and Engines Ready!")
    }
}
