package com.example.brainrottracker.service

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class BrainrotTrackerService : AccessibilityService() {

    private var lastShortTitle: String? = null
    private var lastReelAuthor: String? = null
    private var lastTreeCheckTime = 0L
    private val TREE_CHECK_INTERVAL_MS = 500L
    private var lastDumpedPackage = ""

    private val YOUTUBE_JUNK_TITLES = setOf(
        "shorts", "home", "subscriptions", "you", "create",
        "likes", "comments", "share", "remix", "all", "gaming",
        "hyped", "podcasts", "music", "mixes", "live"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val packageName = event.packageName?.toString() ?: return
        val now = System.currentTimeMillis()

        // We only care about Instagram and YouTube
        if (packageName != "com.instagram.android" && packageName != "com.google.android.youtube") return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                Log.d("BrainrotTracker", "Focus: $packageName")
                // Reset dump flag on every window state change so we
                // get a fresh dump when navigating to Shorts tab
                lastDumpedPackage = ""
            }
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                // Don't throttle scrolls as aggressively for Instagram, they are the signal
                val root = rootInActiveWindow ?: return
                if (packageName == "com.instagram.android") detectInstagramReels(root)
                root.recycle()
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                // Throttle content changes to save CPU
                if (now - lastTreeCheckTime < TREE_CHECK_INTERVAL_MS) return
                lastTreeCheckTime = now

                val root = rootInActiveWindow ?: return
                if (packageName == "com.google.android.youtube") {
                    detectYouTubeShorts(root)
                } else if (packageName == "com.instagram.android") {
                    detectInstagramReels(root)
                }
                root.recycle()
            }
        }
    }

    // --- YouTube Shorts Logic ---

    private fun detectYouTubeShorts(root: AccessibilityNodeInfo) {
        // Dump the tree when navigating to YouTube or after a window change
        if (lastDumpedPackage != "youtube_shorts_attempt") {
            Log.d("BrainrotTree", "=== TREE DUMP START ===")
            dumpTree(root)
            Log.d("BrainrotTree", "=== TREE DUMP END ===")
            lastDumpedPackage = "youtube_shorts_attempt"
        }

        if (!isYoutubeShortsPlayer(root)) {
            lastShortTitle = null
            return
        }

        val title = getBottomLeftText(root)
        if (title != null && title != lastShortTitle) {
            if (lastShortTitle != null) {
                Log.d("BrainrotTracker", "🔥 SWIPED YouTube Short: $title")
            }
            lastShortTitle = title
        }
    }

    private fun isYoutubeShortsPlayer(root: AccessibilityNodeInfo): Boolean {
        val metrics = resources.displayMetrics
        val screenW = metrics.widthPixels
        val screenH = metrics.heightPixels

        val recyclers = mutableListOf<AccessibilityNodeInfo>()
        // YouTube uses the OLD support library name in many builds
        findNodesByClass(root, "android.support.v7.widget.RecyclerView", recyclers)
        findNodesByClass(root, "androidx.recyclerview.widget.RecyclerView", recyclers)

        Log.d("BrainrotYT", "RecyclerView count: ${recyclers.size}")
        
        val hasShortsRecycler = recyclers.any { node ->
            val b = Rect()
            node.getBoundsInScreen(b)
            Log.d("BrainrotYT", "  RV: ${b.width()}x${b.height()} scrollable=${node.isScrollable}")
            val isFullWidth = b.width() >= screenW * 0.90f
            val isFullHeight = b.height() >= screenH * 0.75f
            val isTallerThanWide = b.height() > b.width()
            isFullWidth && isFullHeight && isTallerThanWide
        }

        val hasHorizontalFeed = recyclers.any { node ->
            val b = Rect()
            node.getBoundsInScreen(b)
            // Tightened check for horizontal shelf
            b.width() > b.height() * 2.0f && b.width() > screenW * 0.7f && b.height() > 80
        }

        recyclers.forEach { it.recycle() }

        return hasShortsRecycler && !hasHorizontalFeed
    }

    // --- Instagram Reels Logic ---

    private fun detectInstagramReels(root: AccessibilityNodeInfo) {
        if (!isInstagramReelsPlayer(root)) {
            lastReelAuthor = null
            return
        }

        val currentAuthor = getBottomLeftText(root)
        if (currentAuthor != null && currentAuthor != lastReelAuthor) {
            if (lastReelAuthor != null) {
                Log.d("BrainrotTracker", "🎬 SWIPED Instagram Reel by: $currentAuthor")
            }
            lastReelAuthor = currentAuthor
        }
    }

    private fun isInstagramReelsPlayer(root: AccessibilityNodeInfo): Boolean {
        val metrics = resources.displayMetrics
        val screenH = metrics.heightPixels
        val screenW = metrics.widthPixels

        // Fingerprint Reels by the action button cluster on the right side
        val clickableNodes = mutableListOf<AccessibilityNodeInfo>()
        findClickableNodesInRegion(
            root,
            left = (screenW * 0.75f).toInt(),
            top = (screenH * 0.40f).toInt(),
            right = screenW,
            bottom = (screenH * 0.90f).toInt(),
            result = clickableNodes
        )
        val actionButtonCount = clickableNodes.size
        clickableNodes.forEach { it.recycle() }

        if (actionButtonCount < 3) return false

        // Guard: Check for horizontal RecyclerView (Explore/Home feed)
        val recyclers = mutableListOf<AccessibilityNodeInfo>()
        findNodesByClass(root, "android.support.v7.widget.RecyclerView", recyclers)
        findNodesByClass(root, "androidx.recyclerview.widget.RecyclerView", recyclers)
        val hasHorizontalRecycler = recyclers.any { node ->
            val b = Rect()
            node.getBoundsInScreen(b)
            b.width() > b.height() * 2.0f && b.width() > screenW * 0.5f
        }
        recyclers.forEach { it.recycle() }

        return !hasHorizontalRecycler
    }

    // --- Helpers ---

    private fun getBottomLeftText(root: AccessibilityNodeInfo): String? {
        val screenHeight = resources.displayMetrics.heightPixels
        val screenWidth = resources.displayMetrics.widthPixels

        val candidates = mutableListOf<String>()
        collectTextInRegion(
            root,
            left = 0,
            top = (screenHeight * 0.55).toInt(),
            right = (screenWidth * 0.75).toInt(),
            bottom = (screenHeight * 0.92).toInt(),
            results = candidates
        )

        return candidates
            .filter { text ->
                text.length > 5
                && !text.all { c -> c.isDigit() || c == ',' || c == '.' || c == 'K' || c == 'M' || c == ' ' }
                && text.trim().lowercase() !in YOUTUBE_JUNK_TITLES
                && !text.trim().lowercase().endsWith("likes")
                && !text.trim().lowercase().endsWith("views")
                && !text.trim().lowercase().endsWith("comments")
            }
            .maxByOrNull { it.length }
    }

    private fun collectTextInRegion(node: AccessibilityNodeInfo, left: Int, top: Int, right: Int, bottom: Int, results: MutableList<String>) {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        if (!Rect.intersects(bounds, Rect(left, top, right, bottom))) return

        val text = node.text?.toString()
        if (!text.isNullOrBlank() && text.length > 3) {
            results.add(text)
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectTextInRegion(child, left, top, right, bottom, results)
            child.recycle() // Recycle child after recursion
        }
    }

    private fun findClickableNodesInRegion(node: AccessibilityNodeInfo, left: Int, top: Int, right: Int, bottom: Int, result: MutableList<AccessibilityNodeInfo>) {
        val b = Rect()
        node.getBoundsInScreen(b)
        if (!Rect.intersects(b, Rect(left, top, right, bottom))) return

        if (node.isClickable && b.centerX() in left..right && b.centerY() in top..bottom) {
            result.add(AccessibilityNodeInfo.obtain(node))
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            findClickableNodesInRegion(child, left, top, right, bottom, result)
            child.recycle() // Recycle child after recursion
        }
    }

    private fun findNodesByClass(node: AccessibilityNodeInfo, className: String, result: MutableList<AccessibilityNodeInfo>) {
        if (node.className?.toString() == className) {
            result.add(AccessibilityNodeInfo.obtain(node))
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            findNodesByClass(child, className, result)
            child.recycle() // Recycle child after recursion
        }
    }

    private fun dumpTree(node: AccessibilityNodeInfo, depth: Int = 0) {
        val b = Rect()
        node.getBoundsInScreen(b)
        Log.d("BrainrotTree", "${"  ".repeat(depth)}[${node.className}] " +
                "text='${node.text}' " +
                "desc='${node.contentDescription}' " +
                "scrollable=${node.isScrollable} " +
                "clickable=${node.isClickable} " +
                "bounds=$b"
        )
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            dumpTree(child, depth + 1)
            child.recycle()
        }
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        Log.d("BrainrotTracker", "Service Connected and ready!")
    }
}