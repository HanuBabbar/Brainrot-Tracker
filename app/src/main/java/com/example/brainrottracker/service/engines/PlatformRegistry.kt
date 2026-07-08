package com.example.brainrottracker.service.engines

import android.content.res.Resources

/**
 * Single source of truth for all tracked platforms.
 *
 * To add a new platform:
 *   1. Add its package name to [allPackageNames] (mirrors accessibility_service_config.xml).
 *   2. Add a [TrackedPlatform] entry in [build].
 *   3. That's it — the service, DAO, and UI automatically pick it up.
 */
object PlatformRegistry {

    /**
     * Package names of all tracked apps.
     * Must match the `android:packageNames` list in accessibility_service_config.xml.
     */
    val allPackageNames: List<String> = listOf(
        "com.instagram.android",
        "com.google.android.youtube",
        "com.zhiliaoapp.musically"   // TikTok
    )

    /**
     * Builds the runtime map of [TrackedPlatform]s keyed by package name.
     * Called once in [BrainrotTrackerService.onServiceConnected].
     */
    fun build(resources: Resources): Map<String, TrackedPlatform> = linkedMapOf(
        "com.instagram.android" to TrackedPlatform(
            packageName = "com.instagram.android",
            displayName = "Instagram",
            engine      = InstagramEngine(resources)
        ),
        "com.google.android.youtube" to TrackedPlatform(
            packageName = "com.google.android.youtube",
            displayName = "YouTube",
            engine      = YouTubeEngine(resources)
        ),
        "com.zhiliaoapp.musically" to TrackedPlatform(
            packageName = "com.zhiliaoapp.musically",
            displayName = "TikTok",
            engine      = TikTokEngine(resources)
        )
    )
}
