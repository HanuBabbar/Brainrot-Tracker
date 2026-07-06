package com.example.brainrottracker.service.engines

import android.view.accessibility.AccessibilityNodeInfo

interface PlatformEngine {
    /**
     * Attempts to detect a swipe on the given screen root.
     * Calls [onSwipe] if a new piece of content is detected.
     */
    fun detectSwipe(root: AccessibilityNodeInfo, onSwipe: () -> Unit)
}
