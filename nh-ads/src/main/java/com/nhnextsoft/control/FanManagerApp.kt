package com.nhnextsoft.control

import android.content.Context
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import timber.log.Timber

object FanManagerApp {

    fun init(context: Context?, testDeviceList: List<String?>?, isDebug: Boolean) {
        if (isDebug) {
            Timber.i("init: enable debug")
            AdSettings.turnOnSDKDebugger(context)
        }
        AdSettings.addTestDevices(testDeviceList)
        AudienceNetworkAds.initialize(context)
    }
}