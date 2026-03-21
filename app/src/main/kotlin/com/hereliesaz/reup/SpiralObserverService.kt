// hereliesaz/reup/ReUp-c714b8692ef249c9d91ed57a33a63f43f5c8c59d/app/src/main/kotlin/com/hereliesaz/reup/SpiralObserverService.kt

package com.hereliesaz.reup

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.ui.graphics.toArgb
import com.hereliesaz.reup.SpiralConfig as Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.task.text.nlclassifier.NLClassifier

/**
 * The fortified Observer.
 * Main thread exhalation achieved. Semantic coordinates acquired.
 * Rendering pipeline unclogged.
 * Cartesian disconnect resolved via geometric relativity.
 * Now tracking the shifting sands of motion with a surgical 4f stroke.
 */
class SpiralObserverService : AccessibilityService() {

    private val TAG = "SpiralObserver"
    private var windowManager: WindowManager? = null
    private var overlayView: HighlightView? = null
    private lateinit var dbHelper: SpiralDatabaseHelper
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    
    // The machine's actual cortex
    private var classifier: NLClassifier? = null

    // Spatial Memory
    private var cachedTargetNode: AccessibilityNodeInfo? = null
    private var cachedTargetText: String = ""
    private var cachedColor: Int? = null

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

        val eventType = event.eventType

        // Fast-path for spatial tracking. Do not awaken the neural network just because the user scrolled.
        if (eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED || 
            eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            
            cachedTargetNode?.let { node ->
                if (node.refresh()) {
                    updateVisualIntervention(node, cachedTargetText, cachedColor!!)
                } else {
                    clearCachedIntervention()
                }
            }
            event.source?.recycle()
            return
        }

        if (eventType != AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val source = event.source ?: return
        
        val eventText = event.text.joinToString(" ").lowercase()
        val sourceText = source.text?.toString()?.lowercase() ?: ""
        val fullText = sourceText.ifBlank { eventText }.trim()

        if (fullText.isBlank()) {
            clearCachedIntervention()
            source.recycle()
            return
        }

        val prefs = getSharedPreferences(Config.PREFS_NAME, Context.MODE_PRIVATE)
        val activeMask = prefs.getInt(Config.KEY_FILTER_MASK, Config.DEFAULT_MASK)
        val sensitivity = prefs.getFloat(Config.KEY_SENSITIVITY, Config.DEFAULT_SENSITIVITY)

        serviceScope.launch {
            var interventionTriggered = false
            var detectedColor: Int? = null
            var targetText = fullText 

            classifier?.let { cortex ->
                val results = cortex.classify(fullText)
                val despairScore = results.find { it.label.equals("Negative", ignoreCase = true) }?.score ?: 0f
                
                if (despairScore >= sensitivity) {
                    if (Config.isEnabled(activeMask, Config.FOCUS_SELF) && Config.isEnabled(activeMask, Config.TYPE_DESPAIR)) {
                        interventionTriggered = true
                        dbHelper.logDistortion(fullText.take(50), Config.FOCUS_SELF, Config.TYPE_DESPAIR)
                        detectedColor = calculateColorForSeverity(despairScore)
                    }
                }
            }

            if (!interventionTriggered) {
                for ((trigger, vectors) in spiralLexicon) {
                    if (fullText.contains(trigger)) {
                        val (focus, type, severity) = vectors
                        if (Config.isEnabled(activeMask, focus) && Config.isEnabled(activeMask, type)) {
                            interventionTriggered = true
                            targetText = trigger
                            dbHelper.logDistortion(trigger, focus, type)
                            detectedColor = calculateColorForSeverity(severity)
                            break
                        }
                    }
                }
            }

            withContext(Dispatchers.Main) {
                if (interventionTriggered && detectedColor != null) {
                    clearCachedIntervention()
                    
                    // Cache the source node instead of recycling it. The machine needs it to track motion.
                    cachedTargetNode = source
                    cachedTargetText = targetText
                    cachedColor = detectedColor
                    
                    updateVisualIntervention(source, targetText, detectedColor!!)
                } else {
                    clearCachedIntervention()
                    source.recycle()
                }
            }
        }
    }

    private fun updateVisualIntervention(node: AccessibilityNodeInfo, targetText: String, color: Int) {
        val bounds = Rect()
        var precisionAchieved = false

        val nodeText = node.text?.toString()?.lowercase() ?: ""
        val startIndex = nodeText.indexOf(targetText)
        if (startIndex >= 0 && targetText.isNotEmpty()) {
            val args = Bundle().apply {
                putInt(AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_START_INDEX, startIndex)
                putInt(AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_LENGTH, targetText.length)
            }
            
            val success = node.refreshWithExtraData(AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY, args)
            
            if (success && node.extras != null) {
                val parcelables = node.extras.getParcelableArray(AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY)
                if (parcelables != null && parcelables.isNotEmpty()) {
                    val rectFs = parcelables.filterIsInstance<android.graphics.RectF>()
                    if (rectFs.isNotEmpty()) {
                        val combinedRect = android.graphics.RectF(rectFs.first())
                        for (i in 1 until rectFs.size) {
                            combinedRect.union(rectFs[i])
                        }
                        combinedRect.roundOut(bounds)
                        precisionAchieved = true
                    }
                }
            }
        }

        if (!precisionAchieved) {
            node.getBoundsInScreen(bounds)
        }
        
        if (bounds.isEmpty) {
            overlayView?.clearInterference()
        } else {
            overlayView?.highlightText(bounds, color)
        }
    }

    private fun clearCachedIntervention() {
        cachedTargetNode?.recycle()
        cachedTargetNode = null
        cachedTargetText = ""
        cachedColor = null
        overlayView?.clearInterference()
    }

    private fun calculateColorForSeverity(severity: Float): Int {
        val baseColor = when {
            severity >= Config.SEVERITY_VISCERAL_THRESHOLD -> Config.SEVERITY_VISCERAL_COLOR
            severity >= Config.SEVERITY_MODERATE_THRESHOLD -> Config.SEVERITY_MODERATE_COLOR
            else -> Config.SEVERITY_MILD_COLOR
        }
        return baseColor.copy(alpha = 0.8f).toArgb()
    }

    override fun onInterrupt() { clearCachedIntervention() }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        clearCachedIntervention()
        overlayView?.let { windowManager?.removeView(it) }
        dbHelper.close()
    }

    private inner class HighlightView(context: Context) : View(context) {
        
        init {
            setWillNotDraw(false)
        }
        
        private val paint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f // The requested razor
            isAntiAlias = true
        }
        private var targetBounds: Rect? = null
        private var isInterfering = false

        fun highlightText(bounds: Rect, color: Int) {
            isInterfering = true
            
            val screenOffset = IntArray(2)
            getLocationOnScreen(screenOffset)
            
            targetBounds = Rect(bounds).apply {
                offset(-screenOffset[0], -screenOffset[1])
            }
            
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
