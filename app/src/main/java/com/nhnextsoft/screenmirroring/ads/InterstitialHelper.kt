package com.nhnextsoft.screenmirroring.ads

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.nhnextsoft.screenmirroring.R
import timber.log.Timber

class InterstitialHelper {
    interface AdHelperListener {
        fun onAdLoaded() {}
        fun onAdLoadError() {}
        fun onAdClosed() {}
    }

    val TAG = "InterstitialHelper"
    lateinit var mActivity: Activity
    var googleId: String = ""
    var isGoogle: Boolean = false
    var isAdLoaded: Boolean = false
    var isAdLoadError: Boolean = false
    var isShowDialogLoading: Boolean = true

    private var mAdHelperListener: AdHelperListener? = null
    var progressDialog: AlertDialog? = null

    private var mInterstitialAd: InterstitialAd? = null

    fun setAdsId(activity: Activity, googleAdsId: String, adHelperListener: AdHelperListener) {
        mActivity = activity
        isGoogle = googleAdsId.isNotEmpty()
        googleId = googleAdsId
        mAdHelperListener = adHelperListener
        initUi()
    }

    private fun initUi() {
        progressDialog = AlertDialog.Builder(mActivity)
            .setView(R.layout.dialog_loading_ads)
            .setCancelable(false)
            .create()

        progressDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun loadAdsInterstitialGoogle() {
        if (isShowDialogLoading && !progressDialog?.isShowing!!) {
            progressDialog?.show()
        }
        val adRequest = AdRequest
            .Builder()
            .build()

        InterstitialAd.load(mActivity, googleId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Timber.tag(TAG).d("Ad was loaded.")
                mInterstitialAd = interstitialAd
                isAdLoaded = true
                mAdHelperListener?.onAdLoaded()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Timber.tag(TAG).d("onAdFailedToLoad%s", adError.message)
                mInterstitialAd = null
                isAdLoadError = false
                mAdHelperListener?.onAdLoadError()
            }
        })
    }

    fun showInterstitial() {
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Timber.tag(TAG).d("Ad was dismissed.")
                mAdHelperListener?.onAdClosed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                Timber.tag(TAG).d("Ad failed to show. %s", adError?.message)
                mAdHelperListener?.onAdClosed()
            }

            override fun onAdShowedFullScreenContent() {
                Timber.tag(TAG).d("Ad showed fullscreen content.")
                mInterstitialAd = null
                isAdLoaded = false
                isAdLoadError = false
            }
        }
        toShowInterstitialAd()

    }

    private fun toShowInterstitialAd() {
        progressDialog?.dismiss()
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(mActivity)
        } else {
            Timber.tag(TAG).d("The interstitial ad wasn't ready yet.")
        }
    }

    fun onDestroy() {
        mInterstitialAd = null
    }
}