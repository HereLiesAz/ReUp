package com.hereliesaz.reup

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.hereliesaz.reup.SpiralConfig as Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The fortified Observer.
 * Now with high-visibility interference and multi-vector text detection.
 */
class SpiralObserverService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var overlayView: HighlightView? = null
    private lateinit var dbHelper: SpiralDatabaseHelper
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private val spiralLexicon = mapOf(
        "pointless" to Triple(Config.FOCUS_WORLD, Config.TYPE_DESPAIR, 0.35f),
        "hate" to Triple(Config.FOCUS_OTHERS, Config.TYPE_ANGER, 0.55f),
        "worthless" to Triple(Config.FOCUS_SELF, Config.TYPE_WORTHLESS, 0.65f),
        "failure" to Triple(Config.FOCUS_SELF, Config.TYPE_WORTHLESS, 0.85f),
        "give up" to Triple(Config.FOCUS_SELF, Config.TYPE_DESPAIR, 0.95f)
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        overlayView = HighlightView(this)
        dbHelper = SpiralDatabaseHelper(this)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        try {
            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            // Permission for 'Display over other apps' might be required on some ROMs
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // We watch for text changes and window shifts
        if (event.eventType != AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val source = event.source
        // Combine event text and source text to avoid missing the evidence
        val eventText = event.text.joinToString(" ").lowercase()
        val sourceText = source?.text?.toString()?.lowercase() ?: ""
        val fullText = "$eventText $sourceText"

        val prefs = getSharedPreferences(Config.PREFS_NAME, Context.MODE_PRIVATE)
        val activeMask = prefs.getInt(Config.KEY_FILTER_MASK, Config.DEFAULT_MASK)

        var detectedColor: Int? = null
        val bounds = Rect()

        for ((trigger, vectors) in spiralLexicon) {
            if (fullText.contains(trigger)) {
                val (focus, type, severity) = vectors

                if (Config.isEnabled(activeMask, focus) && Config.isEnabled(activeMask, type)) {
                    serviceScope.launch { dbHelper.logDistortion(trigger, focus, type) }

                    // If we have a source node, get its physical location
                    source?.getBoundsInScreen(bounds)

                    // Bump Alpha to 0xCC (80%) for undeniable visibility
                    val baseColor = when {
                        severity >= Config.SEVERITY_VISCERAL_THRESHOLD -> Config.SEVERITY_VISCERAL_COLOR
                        severity >= Config.SEVERITY_MODERATE_THRESHOLD -> Config.SEVERITY_MODERATE_COLOR
                        else -> Config.SEVERITY_MILD_COLOR
                    }
                    detectedColor = baseColor.copy(alpha = 0.8f).value.toInt()
                    break
                }
            }
        }

        if (detectedColor != null && !bounds.isEmpty) {
            overlayView?.highlightText(bounds, detectedColor)
        } else {
            overlayView?.clearInterference()
        }

        source?.recycle()
    }

    override fun onInterrupt() { overlayView?.clearInterference() }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let { windowManager?.removeView(it) }
        dbHelper.close()
    }

    private inner class HighlightView(context: Context) : View(context) {
        private val paint = Paint().apply { style = Paint.Style.FILL }
        private var targetBounds: Rect? = null
        private var isInterfering = false

        fun highlightText(bounds: Rect, color: Int) {
            isInterfering = true
            targetBounds = Rect(bounds)
            paint.color = color
            invalidate()
        }

        fun clearInterference() {
            isInterfering = false
            targetBounds = null
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (isInterfering) {
                targetBounds?.let { canvas.drawRect(it, paint) }
            }
        }
    }
}