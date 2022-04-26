package com.nhnextsoft.control.application

import android.app.Application
import com.bytedance.sdk.openadsdk.TTAdConfig
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.google.ads.mediation.sample.customevent.AdmobAdapterUtil
import com.inmobi.sdk.InMobiSdk
import com.inmobi.sdk.SdkInitializationListener
import com.nhnextsoft.control.Admod
import com.nhnextsoft.control.AppOpenManager
import com.nhnextsoft.control.FanManagerApp
import com.unity3d.ads.UnityAds
import org.json.JSONException
import org.json.JSONObject
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
            Timber.d("run support Pangle $supportPangle")
            AdmobAdapterUtil.getPangleSdkManager()
            TTAdSdk.init(this, buildAdConfig(), mInitCallbackPangle)
            Admod.instance?.setPangle(true)
        }

        if (supportInMobi) {
//            val INMOBI_ACCOUNT_ID = "3fd8aa9f482f42769d90c21158d45d47"

            Timber.d("run supportInMobi " + supportPangle)
            var consentObject  =  JSONObject()
            try {
                // Provide correct consent value to sdk which is obtained by User
                consentObject.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE, true);
                // Provide 0 if GDPR is not applicable and 1 if applicable
                consentObject.put("gdpr", "0");
            } catch (e: JSONException) {
                e.printStackTrace();
            }
            InMobiSdk.init(this, supportInMobiAccountId, consentObject, mInitInMobiCallback);
            Admod.instance?.setInMobi(true)
        }

        if (supportUnity) {
            Timber.d("run supportUnity $supportUnity")
            UnityAds.initialize(this, supportUnityAppId)
            Admod.instance?.setUnityAds(true)
        }

        if (enableAdsResume()) {
            AppOpenManager.instance?.init(this, openAppAdId)
        }

    }

    private val mInitCallbackPangle: TTAdSdk.InitCallback = object : TTAdSdk.InitCallback {
        override fun success() {
            Timber.d("init TTAdSdk succeeded")
        }

        override fun fail(p0: Int, p1: String?) {
            Timber.d("init TTAdSdk failed. reason = $p1")
        }
    }

    private val mInitInMobiCallback: SdkInitializationListener = object: SdkInitializationListener {
        override fun onInitializationComplete(p0: java.lang.Error?) {
            Timber.d("InMobi Init + $p0")
        }
    }

    private fun buildAdConfig(): TTAdConfig {
        return TTAdConfig.Builder()
            // Please use your own appId,
            .appId(supportPangleAppId)
            // Turn it on during the testing phase, you can troubleshoot with the log, remove it after launching the app
            .debug(AppGlobal.BUILD_DEBUG)
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
    abstract val supportPangleAppId: String
    abstract val supportInMobi: Boolean
    abstract val supportInMobiAccountId: String
    abstract val supportUnity: Boolean
    abstract val supportUnityAppId: String

}