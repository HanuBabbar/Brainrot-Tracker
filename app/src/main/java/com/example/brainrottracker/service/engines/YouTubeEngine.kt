package com.example.brainrottracker.service.engines

import android.content.res.Resources
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

class YouTubeEngine(private val resources: Resources) : PlatformEngine {

    private var lastShortTitle: String? = null
    private var lastDumpedPackage = ""

    private val YOUTUBE_JUNK_TITLES = setOf(
        "shorts", "home", "subscriptions", "you", "create",
        "likes", "comments", "share", "remix", "all", "gaming",
        "hyped", "podcasts", "music", "mixes", "live"
    )

    override fun detectSwipe(root: AccessibilityNodeInfo, onSwipe: () -> Unit) {
        // Tree dump logic preserved
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
                onSwipe()
            }
            lastShortTitle = title
        }
    }

    override fun onWindowFocused() {
        // Reset the tree-dump flag so we log the tree once per new Shorts session
        lastDumpedPackage = ""
    }

    private fun isYoutubeShortsPlayer(root: AccessibilityNodeInfo): Boolean {
        val metrics = resources.displayMetrics
        val screenW = metrics.widthPixels
        val screenH = metrics.heightPixels

        val recyclers = mutableListOf<AccessibilityNodeInfo>()
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
            b.width() > b.height() * 2.0f && b.width() > screenW * 0.7f && b.height() > 80
        }

        recyclers.forEach { it.recycle() }

        return hasShortsRecycler && !hasHorizontalFeed
    }

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
            child.recycle()
        }
    }

    private fun findNodesByClass(node: AccessibilityNodeInfo, className: String, result: MutableList<AccessibilityNodeInfo>) {
        if (node.className?.toString() == className) {
            result.add(AccessibilityNodeInfo.obtain(node))
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            findNodesByClass(child, className, result)
            child.recycle()
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
}
