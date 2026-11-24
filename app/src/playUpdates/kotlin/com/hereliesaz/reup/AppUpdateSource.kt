package com.hereliesaz.reup

import android.app.Activity
import com.hereliesaz.aznavrail.updates.AzUpdateChecker
import com.hereliesaz.aznavrail.updates.play.AzPlayUpdates

/** Checks the installed distribution without accessing observed text. */
internal fun checkAppUpdate(activity: Activity) {
    AzUpdateChecker.check(activity, "HereLiesAz/ReUp", playSource = AzPlayUpdates)
}
