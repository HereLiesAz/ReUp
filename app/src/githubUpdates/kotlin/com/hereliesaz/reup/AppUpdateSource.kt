package com.hereliesaz.reup

import android.app.Activity
import com.hereliesaz.aznavrail.updates.AzUpdateChecker

/** Checks the installed distribution without accessing observed text. */
internal fun checkAppUpdate(activity: Activity) {
    AzUpdateChecker.check(activity, "HereLiesAz/ReUp")
}
