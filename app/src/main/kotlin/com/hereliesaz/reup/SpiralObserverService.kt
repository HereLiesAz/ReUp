package com.hereliesaz.reup

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SpiralObserverService : AccessibilityService() {

    private val spiralLexicon = setOf(
        "pointless", "give up", "never gets better", "always like this", 
        "failure", "hate myself", "exhausted", "worthless", "fuck it"
    )

    private var windowManager: WindowManager? = null
    private var overlayView: HighlightView? = null
    private lateinit var dbHelper: SpiralDatabaseHelper
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    
    private val lastLoggedWords = mutableMapOf<String, Long>()

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
            // If the user hasn't granted SYSTEM_ALERT_WINDOW, we silently fail to draw.
            // The MainActivity must handle the interrogation.
        }
    }

    @Suppress("DEPRECATION")
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) return

        val source = event.source ?: return
        val currentText = source.text?.toString()?.lowercase() ?: ""

        val rectsToDraw = mutableListOf<RectF>()
        val currentTime = System.currentTimeMillis()

        for (word in spiralLexicon) {
            var startIndex = currentText.indexOf(word)
            
            if (startIndex >= 0) {
                val lastLogged = lastLoggedWords[word] ?: 0L
                if (currentTime - lastLogged > 10000) {
                    // Shove the disk I/O off the main thread. Accessibility events must be processed immediately.
                    serviceScope.launch {
                        dbHelper.logDistortion(word)
                    }
                    lastLoggedWords[word] = currentTime
                }
            }
            
            while (startIndex >= 0) {
                val args = Bundle().apply {
                    putInt(AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_START_INDEX, startIndex)
                    putInt(AccessibilityNodeInfo.EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_LENGTH, word.length)
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
                
                startIndex = currentText.indexOf(word, startIndex + word.length)
            }
        }

        // If rectsToDraw is empty (they deleted the word), the view clears itself.
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
                // Already consumed by the void.
            }
        }
        dbHelper.close()
    }

    private inner class HighlightView(context: Context) : View(context) {
        private val paint = Paint().apply {
            color = Color.parseColor("#668B0000") 
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
