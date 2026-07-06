package com.example.brainrottracker.service.engines

import android.content.res.Resources
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

class InstagramEngine(private val resources: Resources) : PlatformEngine {

    private var lastReelAuthor: String? = null

    override fun detectSwipe(root: AccessibilityNodeInfo, onSwipe: () -> Unit) {
        if (!isInstagramReelsPlayer(root)) {
            lastReelAuthor = null
            return
        }

        val currentAuthor = getBottomLeftText(root)
        if (currentAuthor != null && currentAuthor != lastReelAuthor) {
            if (lastReelAuthor != null) {
                Log.d("BrainrotTracker", "🎬 SWIPED Instagram Reel by: $currentAuthor")
                onSwipe()
            }
            lastReelAuthor = currentAuthor
        }
    }

    private fun isInstagramReelsPlayer(root: AccessibilityNodeInfo): Boolean {
        val metrics = resources.displayMetrics
        val screenH = metrics.heightPixels
        val screenW = metrics.widthPixels

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
}
