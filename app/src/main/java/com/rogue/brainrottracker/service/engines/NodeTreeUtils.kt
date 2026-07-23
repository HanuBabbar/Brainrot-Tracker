package com.rogue.brainrottracker.service.engines

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Shared accessibility tree-walking utilities used by all platform engines.
 * Extracting these prevents code duplication across InstagramEngine, YouTubeEngine,
 * TikTokEngine, and any future engines.
 */

/** Recursively collects text from nodes whose bounds overlap the given region. */
internal fun collectTextInRegion(
    node: AccessibilityNodeInfo,
    left: Int, top: Int, right: Int, bottom: Int,
    results: MutableList<String>
) {
    val bounds = Rect()
    node.getBoundsInScreen(bounds)
    if (!Rect.intersects(bounds, Rect(left, top, right, bottom))) return

    val text = node.text?.toString()
    if (!text.isNullOrBlank() && text.length > 2) results.add(text)

    for (i in 0 until node.childCount) {
        val child = node.getChild(i) ?: continue
        collectTextInRegion(child, left, top, right, bottom, results)
        child.recycle()
    }
}

/** Recursively finds clickable nodes whose centre point falls inside the given region. */
internal fun findClickableNodesInRegion(
    node: AccessibilityNodeInfo,
    left: Int, top: Int, right: Int, bottom: Int,
    result: MutableList<AccessibilityNodeInfo>
) {
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

/** Recursively finds nodes matching the given class name. */
internal fun findNodesByClass(
    node: AccessibilityNodeInfo,
    className: String,
    result: MutableList<AccessibilityNodeInfo>
) {
    if (node.className?.toString() == className) {
        result.add(AccessibilityNodeInfo.obtain(node))
    }
    for (i in 0 until node.childCount) {
        val child = node.getChild(i) ?: continue
        findNodesByClass(child, className, result)
        child.recycle()
    }
}
