package com.nhnextsoft.control.application

import android.app.Application
import com.nhnextsoft.control.Admod
import com.nhnextsoft.control.AppOpenManager
import com.nhnextsoft.control.FanManagerApp
import timber.log.Timber

abstract class SupportAdsApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        AppGlobal.BUILD_DEBUG = buildDebug()
        Timber.i(" run debug: " + AppGlobal.BUILD_DEBUG)
        Admod.instance?.init(this, listTestDeviceId)
        if (supportApplovin) {
            Admod.instance?.setAppLovin(true)
        }
        if (supportFan) {
            FanManagerApp.init(this, listTestDeviceId, AppGlobal.BUILD_DEBUG)
            Admod.instance?.setFan(true)
        }

        if (enableAdsResume()) {
            AppOpenManager.instance?.init(this, openAppAdId)
        }

    }

    abstract fun enableAdsResume(): Boolean
    abstract val listTestDeviceId: List<String>
    abstract val openAppAdId: String
    abstract fun buildDebug(): Boolean

    abstract val supportFan: Boolean
    abstract val supportApplovin: Boolean
}