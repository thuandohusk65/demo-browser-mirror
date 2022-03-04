package com.nhnextsoft.control.analytic

import android.os.Bundle
import com.google.android.gms.ads.AdValue
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import timber.log.Timber

object FirebaseAnalyticsUtil {
    @JvmStatic
    fun logPaidAdImpression(
        adValue: AdValue,
        adUnitId: String?,
        mediationAdapterClassName: String?,
    ) {
        Timber.d(String.format(
            "Paid event of value %d microcents in currency %s of precision %s%n occurred for ad unit %s from ad network %s.",
            adValue.valueMicros,
            adValue.currencyCode,
            adValue.precisionType,
            adUnitId,
            mediationAdapterClassName))
        val params = Bundle() // Log ad value in micros.
        params.putLong("valuemicros", adValue.valueMicros)
        // These values below won’t be used in ROAS recipe.
        // But log for purposes of debugging and future reference.
        params.putString("currency", adValue.currencyCode)
        params.putInt("precision", adValue.precisionType)
        params.putString("adunitid", adUnitId)
        params.putString("network", mediationAdapterClassName)
        Firebase.analytics.logEvent("paid_ad_impression", params)
    }
}