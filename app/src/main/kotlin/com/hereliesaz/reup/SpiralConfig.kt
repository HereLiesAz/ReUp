package com.hereliesaz.reup

import androidx.compose.ui.graphics.Color

/**
 * The geometrical constants of your configured despair.
 * We use bitmasks for targets and float vectors for severity ranges.
 */
object SpiralConfig {

    const val PREFS_NAME = "ReUpSurveillancePrefs"
    const val KEY_FILTER_MASK = "surveillance_filter_mask"

    // Dimension A: Focus (Negativity toward what) - First byte (bits 0-7)
    const val FOCUS_SELF = 1 shl 0   // 00000000 00000001 (Decimal 1)
    const val FOCUS_OTHERS = 1 shl 1 // 00000000 00000010 (Decimal 2)
    const val FOCUS_WORLD = 1 shl 2  // 00000000 00000100 (Decimal 4)
    const val MASK_FOCUS = FOCUS_SELF or FOCUS_OTHERS or FOCUS_WORLD // 00000000 00000111 (Decimal 7)

    // Dimension B: Type (The flavor of negativity) - Second byte (bits 8-15)
    const val TYPE_DESPAIR = 1 shl 8      // 00000001 00000000 (Decimal 256)
    const val TYPE_WORTHLESS = 1 shl 9   // 00000010 00000000 (Decimal 512)
    const val TYPE_ANGER = 1 shl 10      // 00000100 00000000 (Decimal 1024)
    const val MASK_TYPE = TYPE_DESPAIR or TYPE_WORTHLESS or TYPE_ANGER // 00000111 00000000 (Decimal 1792)

    // Default configuration: Total surveillance. Intervene on ALL toxicity vectors.
    const val DEFAULT_MASK = MASK_FOCUS or MASK_TYPE

    // --- SEVERITY ARCHITECTURE (NEW) ---
    // Severity scores ($S_s$) are probabilistic ranges [0.0 - 1.0].

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
            currentMask and flag.inv() // Turn off surveillance for this vector via bitwise NOT.
        } else {
            currentMask or flag // Turn on surveillance via bitwise OR.
        }
    }
}