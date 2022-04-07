package com.nhnextsoft.control.application

import android.app.Application
import android.util.Log
import com.bytedance.sdk.openadsdk.TTAdConfig
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.nhnextsoft.control.Admod
import com.nhnextsoft.control.AppOpenManager
import com.nhnextsoft.control.BuildConfig
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

        if (supportPangle) {
            Log.d("SupportAds","run supportPangle $supportPangle")
            TTAdSdk.init(this, buildAdConfig(), mInitCallback)
            Admod.instance?.setPangle(true)
        }
        if (enableAdsResume()) {
            AppOpenManager.instance?.init(this, openAppAdId)
        }

    }

    private val mInitCallback: TTAdSdk.InitCallback = object : TTAdSdk.InitCallback {
        override fun success() {
            Timber.d("init TTAdSdk succeeded")
        }

        override fun fail(p0: Int, p1: String?) {
            Timber.d("init TTAdSdk failed. reason = $p1")
        }
    }

    private fun buildAdConfig(): TTAdConfig {
        return TTAdConfig.Builder()
            // Please use your own appId,
            .appId("8034646")
            // Turn it on during the testing phase, you can troubleshoot with the log, remove it after launching the app
            .debug(BuildConfig.DEBUG)
            // The default setting is SurfaceView. We strongly recommend to set this to true.
            // If using TextureView to play the video, please set this and add "WAKE_LOCK" permission in manifest
            .useTextureView(true)
            // Fields to indicate whether you are a child or an adult ，0:adult ，1:child
            .coppa(0)
            .build()
    }

    abstract fun enableAdsResume(): Boolean
    abstract val listTestDeviceId: List<String>
    abstract val openAppAdId: String
    abstract fun buildDebug(): Boolean

    abstract val supportFan: Boolean
    abstract val supportApplovin: Boolean
    abstract val supportPangle: Boolean
}