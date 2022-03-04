package com.nhnextsoft.control.funtion

import com.facebook.ads.AdError
import com.facebook.ads.NativeAd
import com.facebook.ads.NativeBannerAd

class FanCallback {
    fun onAdClosed() {}
    fun onAdFailedToLoad(adError: AdError?) {}
    fun onAdOpened() {}
    fun onAdLoaded() {}
    fun onInterstitialLoad() {}
    fun onAdClicked() {}
    fun onAdImpression() {}
    fun onNativeAdLoaded(nativeAd: NativeAd?) {}
    fun onNativeBannerAdLoaded(nativeAd: NativeBannerAd?) {}
}