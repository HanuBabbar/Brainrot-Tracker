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
import com.example.brainrottracker.service.engines.PlatformRegistry
import com.example.brainrottracker.service.engines.TrackedPlatform
import com.example.brainrottracker.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat

class BrainrotTrackerService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var repository: UsageRepository
    private lateinit var userSettings: UserSettings

    private val disableReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_DISABLE_SERVICE) {
                Log.d("BrainrotTracker", "Received disable intent. Disabling self.")
                disableSelf()
            }
        }
    }

    // Performance throttling for heavy TYPE_WINDOW_CONTENT_CHANGED scans
    private var lastTreeCheckTime = 0L
    private var currentIntervalMs = 750L // Default Medium

    /**
     * All tracked platforms keyed by package name.
     * Built from [PlatformRegistry] — no platform-specific logic lives here.
     */
    private lateinit var platforms: Map<String, TrackedPlatform>

    companion object {
        const val ACTION_DISABLE_SERVICE = "com.example.brainrottracker.ACTION_DISABLE_SERVICE"

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

        // Ignore events from apps we don't track — O(1) map lookup, no if/else chain
        val platform = platforms[packageName] ?: return

        val now = System.currentTimeMillis()

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                Log.d("BrainrotTracker", "Focus: ${platform.displayName}")
                platform.engine.onWindowFocused()
            }

            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                // High-signal scroll events — not throttled
                val root = rootInActiveWindow ?: return
                platform.engine.detectSwipe(root) { saveSwipe(platform.displayName) }
                root.recycle()
            }

            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                // Throttle heavy tree scans based on user CPU preference
                if (now - lastTreeCheckTime < currentIntervalMs) return
                lastTreeCheckTime = now

                val root = rootInActiveWindow ?: return
                platform.engine.detectSwipe(root) { saveSwipe(platform.displayName) }
                root.recycle()
            }
        }
    }

    private fun saveSwipe(platformDisplayName: String) {
        serviceScope.launch {
            repository.incrementUsage(platformDisplayName)
        }
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()

        val filter = IntentFilter(ACTION_DISABLE_SERVICE)
        ContextCompat.registerReceiver(this, disableReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)

        val database = AppDatabase.getDatabase(this)
        userSettings = UserSettings(this)
        val notificationHelper = NotificationHelper(this)
        repository = UsageRepository(database.usageDao(), userSettings, notificationHelper)

        // Build the engine map from the registry — adding a platform only touches PlatformRegistry
        platforms = PlatformRegistry.build(resources)

        serviceScope.launch {
            userSettings.cpuMode.collect { mode ->
                currentIntervalMs = mode.intervalMs
                Log.d("BrainrotTracker", "CPU Mode: ${mode.name} (${mode.intervalMs}ms)")
            }
        }

        Log.d("BrainrotTracker", "Service ready — tracking: ${platforms.values.map { it.displayName }}")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(disableReceiver)
    }
}
