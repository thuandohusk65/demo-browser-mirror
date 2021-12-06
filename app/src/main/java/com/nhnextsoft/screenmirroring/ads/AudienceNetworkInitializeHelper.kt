package com.nhnextsoft.screenmirroring.ads

import android.content.Context
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import com.nhnextsoft.screenmirroring.BuildConfig.DEBUG
import timber.log.Timber

class AudienceNetworkInitializeHelper : AudienceNetworkAds.InitListener {

    override fun onInitialized(result: AudienceNetworkAds.InitResult) {
        Timber.tag(AudienceNetworkAds.TAG).d(result.message)
    }

    companion object {

        /**
         * It's recommended to call this method from Application.onCreate().
         * Otherwise you can call it from all Activity.onCreate()
         * methods for Activities that contain ads.
         *
         * @param context Application or Activity.
         */
        internal fun initialize(context: Context) {
            if (!AudienceNetworkAds.isInitialized(context)) {
                if (DEBUG) {
                    AdSettings.turnOnSDKDebugger(context)
                }

                AdSettings.addTestDevice("0235604c-86ca-48ed-8be7-8922332a7e33")
                AdSettings.addTestDevice("89b0011b-ca66-4ebc-b3f0-7922a479f55c")
                AdSettings.addTestDevice("9cf0a3ca-7033-4701-99f4-a9bfc89e2586")
                AdSettings.addTestDevice("4e05c6ba-9fe5-4d50-aea5-20e55e59d907")
                AudienceNetworkAds
                    .buildInitSettings(context)
                    .withInitListener(AudienceNetworkInitializeHelper())
                    .initialize()
            }
        }
    }
}