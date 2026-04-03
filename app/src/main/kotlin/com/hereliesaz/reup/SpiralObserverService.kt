// app/src/main/kotlin/com/hereliesaz/reup/SpiralObserverService.kt

package com.hereliesaz.reup

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.graphics.toArgb
import com.hereliesaz.reup.SpiralConfig as Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.task.text.nlclassifier.NLClassifier
import java.io.File

/**
 * The fortified Observer.
 * Main thread exhalation achieved. Semantic coordinates acquired.
 * Cartesian disconnect resolved via geometric relativity.
 * Recursive node excavation implemented.
 * IPC traffic jam resolved via temporal debouncing (400ms).
 * ACTIVE INTERVENTION PROTOCOL (Text Replacement) INITIATED.
 * PASSIVE AFFIRMATION LOOP INITIATED.
 * CUSTOM TRIGGERS MIGRATED TO TXT FILE.
 */
class SpiralObserverService : AccessibilityService() {

    private val TAG = "SpiralObserver"
    private var windowManager: WindowManager? = null
    private var overlayView: HighlightView? = null
    private lateinit var dbHelper: SpiralDatabaseHelper
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    // The machine's actual cortex
    private var classifier: NLClassifier? = null

    // Spatial Memory & Restraint
    private var cachedTargetNode: AccessibilityNodeInfo? = null
    private var cachedTargetText: String = ""
    private var cachedColor: Int? = null
    private var interrogationJob: Job? = null

    // Active Intervention Views
    private var touchTargetView: View? = null
    private var suggestionsPopupView: View? = null

    // Affirmation Tracking State
    private var currentPackageName: CharSequence? = null
    private var ignoreCount = 0
    private var affirmationJob: Job? = null
    private val affirmations = listOf(
        "You are stronger than this moment.",
        "Take a deep breath. You've got this.",
        "Be kind to yourself.",
        "Every step forward counts.",
        "You are worthy of grace.",
        "Progress, not perfection.",
        "It's okay to feel this way, but don't stay here."
    )

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

        dbHelper = SpiralDatabaseHelper.getInstance(this)

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

        val packageName = event.packageName
        if (packageName != null && packageName != currentPackageName) {
            currentPackageName = packageName
            resetAffirmationState()
        }

        val source = event.source ?: return

        if (!source.isEditable) {
            source.recycle()
            return
        }

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

        // --- NEW: Read Custom Triggers directly from the .txt file ---
        val triggersFile = File(this.filesDir, Config.FILE_CUSTOM_PHRASES)
        val customPhrases = if (triggersFile.exists()) triggersFile.readLines().filter { it.isNotBlank() } else emptyList()

        interrogationJob?.cancel()
        val safeSource = AccessibilityNodeInfo.obtain(source)
        source.recycle()

        interrogationJob = serviceScope.launch {
            try {
                delay(1000) // Debounce breath

                var interventionTriggered = false
                var detectedColor: Int? = null
                var targetText = fullText

                // 1. NEURAL NETWORK EVALUATION (Using the locked Sweet Spot)
                classifier?.let { cortex ->
                    val results = cortex.classify(fullText)
                    val despairScore = results.find { it.label.equals("Negative", ignoreCase = true) }?.score ?: 0f

                    if (despairScore >= Config.THRESHOLD_CORTEX) {
                        val activeFocus = when {
                            Config.isEnabled(activeMask, Config.FOCUS_SELF) -> Config.FOCUS_SELF
                            Config.isEnabled(activeMask, Config.FOCUS_OTHERS) -> Config.FOCUS_OTHERS
                            Config.isEnabled(activeMask, Config.FOCUS_WORLD) -> Config.FOCUS_WORLD
                            else -> 0
                        }
                        val activeType = when {
                            Config.isEnabled(activeMask, Config.TYPE_DESPAIR) -> Config.TYPE_DESPAIR
                            Config.isEnabled(activeMask, Config.TYPE_WORTHLESS) -> Config.TYPE_WORTHLESS
                            Config.isEnabled(activeMask, Config.TYPE_ANGER) -> Config.TYPE_ANGER
                            else -> 0
                        }

                        if (activeFocus != 0 || activeType != 0) {
                            interventionTriggered = true
                            dbHelper.logDistortion(fullText.take(50), activeFocus, activeType)
                            detectedColor = calculateColorForSeverity(despairScore)
                        }
                    }
                }

                // 2. HARDCODED LEXICON EVALUATION
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

                // 3. CUSTOM TXT LEXICON EVALUATION
                if (!interventionTriggered && customPhrases.isNotEmpty()) {
                    for (phrase in customPhrases) {
                        if (fullText.contains(phrase, ignoreCase = true)) {
                            interventionTriggered = true
                            targetText = phrase
                            dbHelper.logDistortion(phrase, Config.FOCUS_SELF, Config.TYPE_DESPAIR)
                            detectedColor = calculateColorForSeverity(0.6f)
                            break
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    if (interventionTriggered && detectedColor != null) {
                        clearCachedIntervention()

                        if (!safeSource.refresh()) {
                            clearCachedIntervention()
                            return@withContext
                        }

                        val preciseNode = findDeepestNodeWithText(safeSource, targetText) ?: AccessibilityNodeInfo.obtain(safeSource)

                        if (!preciseNode.refresh()) {
                            clearCachedIntervention()
                            return@withContext
                        }

                        cachedTargetNode = preciseNode
                        cachedTargetText = targetText
                        cachedColor = detectedColor

                        updateVisualIntervention(preciseNode, targetText, detectedColor!!)
                    } else {
                        clearCachedIntervention()
                    }
                }
            } finally {
                safeSource.recycle()
            }
        }
    }

    private fun findDeepestNodeWithText(root: AccessibilityNodeInfo?, target: String): AccessibilityNodeInfo? {
        if (root == null || target.isBlank()) return null
        var match: AccessibilityNodeInfo? = null
        val nodeText = root.text?.toString()?.lowercase() ?: root.contentDescription?.toString()?.lowercase() ?: ""

        if (nodeText.contains(target) || (target.length > 5 && target.contains(nodeText) && nodeText.isNotBlank())) {
            match = AccessibilityNodeInfo.obtain(root)
        }
        for (i in 0 until root.childCount) {
            val child = root.getChild(i)
            val childMatch = findDeepestNodeWithText(child, target)
            if (childMatch != null) {
                match?.recycle()
                match = childMatch
            }
            child?.recycle()
        }
        return match
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
            clearCachedIntervention()
        } else {
            overlayView?.highlightText(bounds, color)
            showTouchTarget(bounds)
        }
    }

    // --- ACTIVE INTERVENTION & AFFIRMATION PROTOCOL ---

    private fun showTouchTarget(bounds: Rect) {
        removeTouchTarget()

        touchTargetView = View(this).apply {
            setOnClickListener { showSuggestionsPopup(bounds) }
        }

        val params = WindowManager.LayoutParams(
            bounds.width(),
            bounds.height(),
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = bounds.left
            y = bounds.top
        }

        try { windowManager?.addView(touchTargetView, params) }
        catch (e: Exception) { Log.e(TAG, "Failed to project touch target.") }
    }

    private fun showSuggestionsPopup(bounds: Rect) {
        removeTouchTarget()
        removeSuggestionsPopup()

        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            background = GradientDrawable().apply {
                setColor(AndroidColor.parseColor("#F21A1A24")) // Opaque SkyIndigo
                cornerRadius = 24f
                setStroke(4, AndroidColor.parseColor("#FDBE31")) // RayGold Border
            }
        }

        layout.addView(TextView(context).apply {
            text = "REPHRASE PROTOCOL"
            setTextColor(AndroidColor.parseColor("#FDBE31"))
            textSize = 12f
            setPadding(0, 0, 0, 24)
            gravity = Gravity.CENTER
        })

        val suggestions = getAlternativesFor(cachedTargetText)
        suggestions.forEach { suggestion ->
            val btn = Button(context).apply {
                text = suggestion
                setTextColor(AndroidColor.WHITE)
                isAllCaps = false
                background = GradientDrawable().apply {
                    setColor(AndroidColor.parseColor("#99C93F2B")) // Visceral Red
                    cornerRadius = 16f
                }
                setOnClickListener { applySuggestion(suggestion) }
            }
            layout.addView(btn)
            (btn.layoutParams as LinearLayout.LayoutParams).setMargins(0, 0, 0, 16)
        }

        layout.addView(Button(context).apply {
            text = "IGNORE"
            setTextColor(AndroidColor.GRAY)
            setBackgroundColor(AndroidColor.TRANSPARENT)
            setOnClickListener { handleIgnore() }
        })

        suggestionsPopupView = layout

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = bounds.left
            y = bounds.bottom + 20
        }

        try { windowManager?.addView(suggestionsPopupView, params) }
        catch (e: Exception) { Log.e(TAG, "Failed to project intervention popup.") }
    }

    private fun getAlternativesFor(text: String): List<String> {
        return when (text.lowercase()) {
            "pointless" -> listOf("challenging", "a learning step", "meaningful")
            "hate" -> listOf("struggle with", "dislike", "am navigating")
            "worthless" -> listOf("valuable", "still growing", "worthy")
            "failure" -> listOf("setback", "stepping stone", "lesson")
            "give up" -> listOf("take a break", "keep trying", "ask for help")
            else -> listOf("This is hard but I can manage", "I need a moment", "I'm working through this")
        }
    }

    private fun applySuggestion(suggestion: String) {
        cachedTargetNode?.let { node ->
            val currentText = node.text?.toString() ?: ""
            val newText = currentText.replace(cachedTargetText, suggestion, ignoreCase = true)

            val args = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, newText)
            }
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        }

        resetAffirmationState()
        clearCachedIntervention()
    }

    // --- Affirmation Logic ---

    private fun handleIgnore() {
        ignoreCount++
        clearCachedIntervention()

        if (ignoreCount >= 2 && (affirmationJob == null || affirmationJob?.isActive != true)) {
            startAffirmationLoop()
        }
    }

    private fun startAffirmationLoop() {
        affirmationJob = serviceScope.launch(Dispatchers.Main) {
            while (true) {
                delay(20000)
                Toast.makeText(this@SpiralObserverService, affirmations.random(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetAffirmationState() {
        ignoreCount = 0
        affirmationJob?.cancel()
        affirmationJob = null
    }

    // --- UI Cleanup ---

    private fun removeTouchTarget() {
        touchTargetView?.let { windowManager?.removeView(it) }
        touchTargetView = null
    }

    private fun removeSuggestionsPopup() {
        suggestionsPopupView?.let { windowManager?.removeView(it) }
        suggestionsPopupView = null
    }

    private fun clearCachedIntervention() {
        cachedTargetNode?.recycle()
        cachedTargetNode = null
        cachedTargetText = ""
        cachedColor = null
        overlayView?.clearInterference()
        removeTouchTarget()
        removeSuggestionsPopup()
    }

    private fun calculateColorForSeverity(severity: Float): Int {
        val baseColor = when {
            severity >= Config.SEVERITY_VISCERAL_THRESHOLD -> Config.SEVERITY_VISCERAL_COLOR
            severity >= Config.SEVERITY_MODERATE_THRESHOLD -> Config.SEVERITY_MODERATE_COLOR
            else -> Config.SEVERITY_MILD_COLOR
        }
        return baseColor.toArgb()
    }

    override fun onInterrupt() { clearCachedIntervention() }

    override fun onDestroy() {
        super.onDestroy()
        resetAffirmationState()
        serviceScope.cancel()
        clearCachedIntervention()
        overlayView?.let { windowManager?.removeView(it) }
        dbHelper.close()
    }

    private inner class HighlightView(context: Context) : View(context) {
        init { setWillNotDraw(false) }

        private val paint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
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