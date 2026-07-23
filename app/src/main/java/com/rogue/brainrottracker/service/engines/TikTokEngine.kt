package com.rogue.brainrottracker.service.engines

import android.content.res.Resources
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Detects swipes in TikTok's "For You" feed.
 *
 * Detection strategy: TikTok's For You feed is a full-screen vertical ViewPager.
 * Each video has action buttons (like/comment/share/follow) in the right column.
 * We check that those right-column buttons are present (confirming we're in the feed,
 * not the Discover tab or profile), then read the username/caption from the bottom-left
 * to detect when the video changes.
 */
class TikTokEngine(private val resources: Resources) : PlatformEngine {

    private var lastVideoAuthor: String? = null

    override fun detectSwipe(root: AccessibilityNodeInfo, onSwipe: () -> Unit) {
        if (!isTikTokFeedPlayer(root)) {
            lastVideoAuthor = null
            return
        }

        val currentAuthor = getBottomLeftText(root)
        if (currentAuthor != null && currentAuthor != lastVideoAuthor) {
            if (lastVideoAuthor != null) {
                Log.d("BrainrotTracker", "🎵 SWIPED TikTok video by: $currentAuthor")
                onSwipe()
            }
            lastVideoAuthor = currentAuthor
        }
    }

    /**
     * Returns true when the visible screen looks like TikTok's For You feed.
     * Heuristic: 3+ clickable nodes in the right action-button column.
     */
    private fun isTikTokFeedPlayer(root: AccessibilityNodeInfo): Boolean {
        val metrics = resources.displayMetrics
        val screenH = metrics.heightPixels
        val screenW = metrics.widthPixels

        val rightColumnNodes = mutableListOf<AccessibilityNodeInfo>()
        findClickableNodesInRegion(
            root,
            left  = (screenW * 0.75f).toInt(),
            top   = (screenH * 0.30f).toInt(),
            right = screenW,
            bottom = (screenH * 0.85f).toInt(),
            result = rightColumnNodes
        )
        val actionButtonCount = rightColumnNodes.size
        rightColumnNodes.forEach { it.recycle() }

        return actionButtonCount >= 3
    }

    /**
     * Reads the most prominent text from the bottom-left area, where TikTok
     * shows the username/caption (@username, song name, or caption text).
     */
    private fun getBottomLeftText(root: AccessibilityNodeInfo): String? {
        val screenH = resources.displayMetrics.heightPixels
        val screenW = resources.displayMetrics.widthPixels

        val candidates = mutableListOf<String>()
        collectTextInRegion(
            root,
            left   = 0,
            top    = (screenH * 0.55).toInt(),
            right  = (screenW * 0.75).toInt(),
            bottom = (screenH * 0.92).toInt(),
            results = candidates
        )

        return candidates
            .filter { text ->
                text.length > 2
                && !text.all { c -> c.isDigit() || c == ',' || c == '.' || c == 'K' || c == 'M' || c == ' ' }
            }
            .maxByOrNull { it.length }
    }
}
