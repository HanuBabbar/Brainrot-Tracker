package com.rogue.brainrottracker.service.engines

/**
 * Represents a single tracked social-media platform.
 *
 * @param packageName  Android package name used to filter accessibility events.
 * @param displayName  Human-readable name stored in the database and shown in the UI.
 * @param engine       The detection engine for this platform.
 */
data class TrackedPlatform(
    val packageName: String,
    val displayName: String,
    val engine: PlatformEngine
)
