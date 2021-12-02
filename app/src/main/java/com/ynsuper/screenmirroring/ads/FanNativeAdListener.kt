package com.ynsuper.screenmirroring.ads

import android.content.Context
import androidx.core.os.bundleOf
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.NativeAdListener
import com.google.firebase.analytics.FirebaseAnalytics

open class FanNativeAdListener(private val context: Context, private val placementId: String) :
    NativeAdListener {

    companion object {
        private const val EVENT_FAN_NATIVE_AD_ERROR = "fan_native_ad_error"
        private const val EVENT_FAN_NATIVE_AD_LOADED = "fan_native_ad_loaded"
        private const val EVENT_FAN_NATIVE_AD_CLICKED = "fan_native_ad_clicked"
        private const val EVENT_FAN_NATIVE_AD_IMPRESSION = "fan_native_ad_impression"
    }

    override fun onMediaDownloaded(ad: Ad) {}

    override fun onError(ad: Ad, adError: AdError) {
        FirebaseAnalytics.getInstance(context)
            .logEvent(EVENT_FAN_NATIVE_AD_ERROR, bundleOf("placementId" to placementId))
    }

    override fun onAdLoaded(ad: Ad) {
        FirebaseAnalytics.getInstance(context)
            .logEvent(EVENT_FAN_NATIVE_AD_LOADED, bundleOf("placementId" to placementId))
    }

    override fun onAdClicked(ad: Ad) {
        FirebaseAnalytics.getInstance(context)
            .logEvent(EVENT_FAN_NATIVE_AD_CLICKED, bundleOf("placementId" to placementId))
    }

    override fun onLoggingImpression(ad: Ad) {
        FirebaseAnalytics.getInstance(context)
            .logEvent(EVENT_FAN_NATIVE_AD_IMPRESSION, bundleOf("placementId" to placementId))
    }
}