package com.nhnextsoft.screenmirroring.utility.extensions

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 *  Report custom log to firebase crashlytics
 *
 * @param logMessage:String
 * @param throwable:Throwable
 */
fun logCrash(logMessage: String, throwable: Throwable?) {
    try {
        FirebaseCrashlytics.getInstance().apply {
            log(logMessage)
            if (throwable != null) {
                recordException(throwable)
            }
        }
    } catch (e: Exception) {
        Timber.tag("FirebaseCrashlytics").d("logCrash")
    }
}

class ReleaseTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.ERROR || priority == Log.WARN) logCrash(message, t)
    }

}