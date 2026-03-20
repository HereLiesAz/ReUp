// hereliesaz/reup/ReUp-9db2805a9ede9350d55e55d72acf9c1535bb70f4/app/src/main/kotlin/com/hereliesaz/reup/SpiralObserverService.kt

package com.hereliesaz.reup

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.hereliesaz.reup.SpiralConfig as Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.task.text.nlclassifier.NLClassifier

/**
 * The fortified Observer.
 * Now with functional LiteRT neural inference and real-time probability evaluation.
 */
class SpiralObserverService : AccessibilityService() {

    private val TAG = "SpiralObserver"
    private var windowManager: WindowManager? = null
    private var overlayView: HighlightView? = null
    private lateinit var dbHelper: SpiralDatabaseHelper
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    
    // The machine's actual cortex
    private var classifier: NLClassifier? = null

    // Fallback/Override lexicon for high-certainty specific vectors
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

        // Initialize the neural network from the downloaded cortex
        try {
            classifier = NLClassifier.createFromFile(this, "sentiment_classifier.tflite")
            Log.i(TAG, "Cortex online. Neural inference active.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize cortex: ${e.message}")
        }

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
            Log.e(TAG, "Overlay authorization failed at runtime: ${e.message}")
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType != AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val source = event.source
        val eventText = event.text.joinToString(" ").lowercase()
        val sourceText = source?.text?.toString()?.lowercase() ?: ""
        val fullText = "$eventText $sourceText".trim()

        if (fullText.isBlank()) return

        val prefs = getSharedPreferences(Config.PREFS_NAME, Context.MODE_PRIVATE)
        val activeMask = prefs.getInt(Config.KEY_FILTER_MASK, Config.DEFAULT_MASK)
        val sensitivity = prefs.getFloat(Config.KEY_SENSITIVITY, Config.DEFAULT_SENSITIVITY)

        var interventionTriggered = false
        var detectedColor: Int? = null
        val bounds = Rect()

        // Phase 1: Neural Interrogation (Probability of Despair)
        classifier?.let { cortex ->
            val results = cortex.classify(fullText)
            // Most TFLite sentiment models use "Negative" and "Positive" labels
            val despairScore = results.find { it.label.equals("Negative", ignoreCase = true) }?.score ?: 0f
            
            if (despairScore >= sensitivity) {
                Log.i(TAG, "NEURAL DETECTION: Despair probability ($despairScore) exceeds paranoia threshold.")
                interventionTriggered = true
                
                // For neural detections, we default to internal despair if no lexicon match exists
                serviceScope.launch { dbHelper.logDistortion(fullText.take(50), Config.FOCUS_SELF, Config.TYPE_DESPAIR) }
                detectedColor = calculateColorForSeverity(despairScore)
            }
        }

        // Phase 2: Lexicon Verification (High-certainty override and vector mapping)
        for ((trigger, vectors) in spiralLexicon) {
            if (fullText.contains(trigger)) {
                val (focus, type, severity) = vectors
                if (Config.isEnabled(activeMask, focus) && Config.isEnabled(activeMask, type)) {
                    Log.i(TAG, "LEXICON MATCH: '$trigger' confirmed. Severity: $severity")
                    interventionTriggered = true
                    serviceScope.launch { dbHelper.logDistortion(trigger, focus, type) }
                    detectedColor = calculateColorForSeverity(severity)
                    break
                }
            }
        }

        if (interventionTriggered && source != null) {
            source.getBoundsInScreen(bounds)
            detectedColor?.let { overlayView?.highlightText(bounds, it) }
        } else {
            overlayView?.clearInterference()
        }

        source?.recycle()
    }

    private fun calculateColorForSeverity(severity: Float): Int {
        val baseColor = when {
            severity >= Config.SEVERITY_VISCERAL_THRESHOLD -> Config.SEVERITY_VISCERAL_COLOR
            severity >= Config.SEVERITY_MODERATE_THRESHOLD -> Config.SEVERITY_MODERATE_COLOR
            else -> Config.SEVERITY_MILD_COLOR
        }
        return baseColor.copy(alpha = 0.8f).value.toInt()
    }

    override fun onInterrupt() { overlayView?.clearInterference() }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let { windowManager?.removeView(it) }
        dbHelper.close()
    }

    private inner class HighlightView(context: Context) : View(context) {
        private val paint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 12f // Slightly thicker for visceral impact
            isAntiAlias = true
        }
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
}            val despairScore = results.find { it.label.equals("Negative", ignoreCase = true) }?.score ?: 0f
            
            if (despairScore >= sensitivity) {
                Log.i(TAG, "NEURAL DETECTION: Despair probability ($despairScore) exceeds paranoia threshold.")
                interventionTriggered = true
                
                // For neural detections, we default to internal despair if no lexicon match exists
                serviceScope.launch { dbHelper.logDistortion(fullText.take(50), Config.FOCUS_SELF, Config.TYPE_DESPAIR) }
                detectedColor = calculateColorForSeverity(despairScore)
            }
        }

        // Phase 2: Lexicon Verification (High-certainty override and vector mapping)
        for ((trigger, vectors) in spiralLexicon) {
            if (fullText.contains(trigger)) {
                val (focus, type, severity) = vectors
                if (Config.isEnabled(activeMask, focus) && Config.isEnabled(activeMask, type)) {
                    Log.i(TAG, "LEXICON MATCH: '$trigger' confirmed. Severity: $severity")
                    interventionTriggered = true
                    serviceScope.launch { dbHelper.logDistortion(trigger, focus, type) }
                    detectedColor = calculateColorForSeverity(severity)
                    break
                }
            }
        }

        if (interventionTriggered && source != null) {
            source.getBoundsInScreen(bounds)
            detectedColor?.let { overlayView?.highlightText(bounds, it) }
        } else {
            overlayView?.clearInterference()
        }

        source?.recycle()
    }

    private fun calculateColorForSeverity(severity: Float): Int {
        val baseColor = when {
            severity >= Config.SEVERITY_VISCERAL_THRESHOLD -> Config.SEVERITY_VISCERAL_COLOR
            severity >= Config.SEVERITY_MODERATE_THRESHOLD -> Config.SEVERITY_MODERATE_COLOR
            else -> Config.SEVERITY_MILD_COLOR
        }
        return baseColor.copy(alpha = 0.8f).value.toInt()
    }

    override fun onInterrupt() { overlayView?.clearInterference() }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let { windowManager?.removeView(it) }
        dbHelper.close()
    }

    private inner class HighlightView(context: Context) : View(context) {
        private val paint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 12f // Slightly thicker for visceral impact
            isAntiAlias = true
        }
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
