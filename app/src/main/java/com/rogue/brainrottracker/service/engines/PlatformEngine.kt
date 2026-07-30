package com.rogue.brainrottracker.service.engines

import android.view.accessibility.AccessibilityNodeInfo

interface PlatformEngine {
    /**
     * Attempts to detect a swipe/new-content event on the given screen root.
     * Calls [onSwipe] if a new piece of content is detected.
     */
    fun detectSwipe(root: AccessibilityNodeInfo, onSwipe: () -> Unit)

    /**
     * Called when this platform's app window comes to the foreground.
     * Default implementation is a no-op; override when the engine needs
     * to reset internal state on focus (e.g. clearing a dump flag).
     */
    fun onWindowFocused() {}
}
