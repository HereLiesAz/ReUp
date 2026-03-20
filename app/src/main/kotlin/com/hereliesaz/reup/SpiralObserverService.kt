package com.hereliesaz.reup

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.hereliesaz.reup.ui.theme.TranslucentHemorrhage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.task.text.nlclassifier.NLClassifier

class SpiralObserverService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var overlayView: HighlightView? = null
    private lateinit var dbHelper: SpiralDatabaseHelper
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    
    private var classifier: NLClassifier? = null
    private val lastLoggedSentences = mutableMapOf<String, Long>()

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
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        try {
            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            // Awaiting authorization to hallucinate.
        }

        try {
            val options = NLClassifier.NLClassifierOptions.builder().build()
            classifier = NLClassifier.createFromFileAndOptions(this, "sentiment_classifier.tflite", options)
        } catch (e: Exception) {
            // The machine lacks its brain. It will remain dormant until the model is provided.
        }
    }

    @Suppress("DEPRECATION")
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) return
        if (classifier == null) return

        val source = event.source ?: return
        val currentText = source.text?.toString() ?: ""

        val rectsToDraw = mutableListOf<RectF>()
        val currentTime = System.currentTimeMillis()
        
        // Interrogate SharedPreferences for your current tolerance for misery.
        val sharedPrefs = getSharedPreferences("ReUpPrefs", Context.MODE_PRIVATE)
        val sensitivityThreshold = sharedPrefs.getFloat("sensitivity", 0.7f)

        val sentences = currentText.split(Regex("(?<=[.!?])\\s+|\\n+"))

        var currentIndexOffset = 0

        for (rawSentence in sentences) {
            val sentence = rawSentence.trim()
            if (sentence.length < 3) {
                currentIndexOffset += rawSentence.length
                continue
            }

            val results = classifier?.classify(sentence) ?: emptyList()
            
            val negativeScore = results.find { it.label.equals("Negative", ignoreCase = true) }?.score ?: 0f

            val startIndex = currentText.indexOf(sentence, currentIndexOffset)
            
            if (startIndex >= 0 && negativeScore > sensitivityThreshold) {
                val lastLogged = lastLoggedSentences[sentence] ?: 0L
                if (currentTime - lastLogged > 10000) {
                    serviceScope.launch {
                        dbHelper.logDistortion(sentence.take(50))
                    }
                    lastLoggedSentences[sentence] = currentTime
                }

                val args = Bundle().apply {
                    putInt(AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_START_INDEX, startIndex)
                    putInt(AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_LENGTH, sentence.length)
                }

                val hasData = source.refreshWithExtraData(
                    AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY,
                    args
                )

                if (hasData) {
                    val parcelables = args.getParcelableArray(AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY)
                    parcelables?.forEach { parcel ->
                        if (parcel is RectF) {
                            rectsToDraw.add(parcel)
                        }
                    }
                }
            }
            currentIndexOffset = (startIndex.takeIf { it >= 0 } ?: currentIndexOffset) + sentence.length
        }

        overlayView?.updateRects(rectsToDraw)
        source.recycle()
    }

    override fun onInterrupt() {
        overlayView?.updateRects(emptyList())
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                // Already consumed.
            }
        }
        classifier?.close()
        dbHelper.close()
    }

    private inner class HighlightView(context: Context) : View(context) {
        private val paint = Paint().apply {
            color = TranslucentHemorrhage.value.toInt()
            style = Paint.Style.FILL
        }
        private var rects: List<RectF> = emptyList()

        fun updateRects(newRects: List<RectF>) {
            rects = newRects
            invalidate() 
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            for (rect in rects) {
                canvas.drawRect(rect, paint)
            }
        }
    }
}
