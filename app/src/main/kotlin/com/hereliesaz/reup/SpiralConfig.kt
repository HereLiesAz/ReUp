// app/src/main/kotlin/com/hereliesaz/reup/SpiralConfig.kt

package com.hereliesaz.reup

import androidx.compose.ui.graphics.Color

/**
 * The geometrical constants of your configured despair.
 * The illusion of a slider has been removed; the ML threshold is now locked.
 * Custom triggers are now tethered to a physical text file.
 */
object SpiralConfig {

    const val PREFS_NAME = "ReUpSurveillancePrefs"
    const val KEY_FILTER_MASK = "surveillance_filter_mask"

    // The name of the pure text file storing your custom triggers
    const val FILE_CUSTOM_PHRASES = "custom_triggers.txt"

    // The locked "sweet spot" for the neural network.
    // Prevents the machine from highlighting everything, but catches true negativity.
    const val THRESHOLD_CORTEX = 0.48f

    // Dimension A: Focus (Negativity toward what) - First byte (bits 0-7)
    const val FOCUS_SELF = 1 shl 0
    const val FOCUS_OTHERS = 1 shl 1
    const val FOCUS_WORLD = 1 shl 2
    const val MASK_FOCUS = FOCUS_SELF or FOCUS_OTHERS or FOCUS_WORLD

    // Dimension B: Type (The flavor of negativity) - Second byte (bits 8-15)
    const val TYPE_DESPAIR = 1 shl 8
    const val TYPE_WORTHLESS = 1 shl 9
    const val TYPE_ANGER = 1 shl 10
    const val MASK_TYPE = TYPE_DESPAIR or TYPE_WORTHLESS or TYPE_ANGER

    // Default configuration: Total surveillance. Intervene on ALL toxicity vectors.
    const val DEFAULT_MASK = MASK_FOCUS or MASK_TYPE

    // --- SEVERITY ARCHITECTURE ---

    // MILD severity (0.00 - 0.39): Represented by the icon's harsh golden rays.
    const val SEVERITY_MILD_THRESHOLD = 0.0f
    val SEVERITY_MILD_COLOR = Color(0x66FDBE31) // 40% Alpha for transparency

    // MODERATE severity (0.40 - 0.69): Represented by the icon's sunset orange.
    const val SEVERITY_MODERATE_THRESHOLD = 0.4f
    val SEVERITY_MODERATE_COLOR = Color(0x66F3752C) // 40% Alpha

    // VISCERAL severity (0.70 - 1.00): Represented by the icon's central red semicircle.
    const val SEVERITY_VISCERAL_THRESHOLD = 0.7f
    val SEVERITY_VISCERAL_COLOR = Color(0x66C93F2B) // 40% Alpha

    /**
     * Returns true if the specified surveillance flag (vector) is authorized in the mask.
     */
    fun isEnabled(mask: Int, flag: Int): Boolean {
        return (mask and flag) == flag
    }

    /**
     * Toggles a surveillance vector (flag). If authorized, deauthorizes it. If deauthorized, authorizes it.
     */
    fun toggleFlag(currentMask: Int, flag: Int): Int {
        return if (isEnabled(currentMask, flag)) {
            currentMask and flag.inv()
        } else {
            currentMask or flag
        }
    }
}