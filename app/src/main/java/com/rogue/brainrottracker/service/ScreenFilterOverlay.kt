package com.rogue.brainrottracker.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.util.Log
import android.view.View
import android.view.WindowManager

/**
 * Draws a full-screen desaturation scrim over the currently focused tracked app using an
 * AccessibilityService overlay window (TYPE_ACCESSIBILITY_OVERLAY needs no extra permission
 * beyond the accessibility service itself). Touch passes straight through to the app below.
 */
class ScreenFilterOverlay(private val service: AccessibilityService) {

    private val windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: View? = null
    private var intensity: Float = 0f

    private inner class FilterView(context: Context) : View(context) {
        private val paint = Paint()

        fun setIntensity(value: Float) {
            val saturation = (1f - value).coerceIn(0f, 1f)
            val matrix = ColorMatrix().apply { setSaturation(saturation) }
            paint.colorFilter = ColorMatrixColorFilter(matrix)
            paint.color = Color.argb((value * 60).toInt().coerceIn(0, 60), 0, 0, 0)
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawColor(paint.color)
        }
    }

    fun show() {
        if (overlayView != null) return
        try {
            val view = FilterView(service).also { it.setIntensity(intensity) }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )
            windowManager.addView(view, params)
            overlayView = view
        } catch (e: Exception) {
            Log.e("BrainrotTracker", "Failed to add grayscale overlay", e)
        }
    }

    fun updateIntensity(value: Float) {
        intensity = value.coerceIn(0f, 1f)
        (overlayView as? FilterView)?.setIntensity(intensity)
    }

    fun hide() {
        val view = overlayView ?: return
        try {
            windowManager.removeView(view)
        } catch (e: Exception) {
            Log.e("BrainrotTracker", "Failed to remove grayscale overlay", e)
        }
        overlayView = null
    }
}
