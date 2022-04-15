package com.nhnextsoft.screenmirroring.view.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.nhnextsoft.control.billing.AppPurchase
import com.nhnextsoft.control.dialog.InAppDialog
import com.nhnextsoft.screenmirroring.Constants
import com.nhnextsoft.screenmirroring.R
import com.nhnextsoft.screenmirroring.ads.AdConfig
import com.nhnextsoft.screenmirroring.ads.PurchaseConstants
import com.nhnextsoft.screenmirroring.config.AppConfigRemote
import com.nhnextsoft.screenmirroring.config.AppPreferences
import com.nhnextsoft.screenmirroring.databinding.ActivitySelectDeviceBinding
import com.nhnextsoft.screenmirroring.utility.extensions.checkConnectWifi
import com.nhnextsoft.screenmirroring.view.dialog.NoWifiFragment
import timber.log.Timber


class SelectDeviceActivity : AppCompatActivity() {

    private val TIME_LOADING_PROGRESS: Long = 2000L

    private lateinit var binding: ActivitySelectDeviceBinding
    private var nativeAd: NativeAd? = null
    private var isLoadNative: Boolean = false
    private var numberOfTimesDisplayed: Int? = null
    private var numberOfImpressionsPerDay: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        checkShowDialogRemoveAds()
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "SelectDevice")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "SelectDeviceActivity")
        }
    }

    private fun checkShowDialogRemoveAds() {
        numberOfTimesDisplayed = AppPreferences().numberOfTimesDialogRemoveAdsDisplayed
        numberOfImpressionsPerDay = AppPreferences().numberOfDialogRemoveAdsImpressionsPerDay
        Timber.d("showDialog ${numberOfTimesDisplayed} ${numberOfImpressionsPerDay}")
        if((numberOfTimesDisplayed?.compareTo(numberOfImpressionsPerDay!!)?: 0) < 0) {
            showDialogRemoveAds()
        }
    }

    private fun startCheckWifiStatus() {
        Handler(Looper.getMainLooper()).postDelayed(kotlin.run {
            {
                if (!this.checkConnectWifi()) {
                    if (!supportFragmentManager.isDestroyed) {
                        NoWifiFragment.newInstance().show(supportFragmentManager, "NoWifiFragment")
                    }
                } else {
                    binding.imageCheckWifi.visibility = View.VISIBLE
                    binding.progressWifi.visibility = View.GONE
                    startCheckSpeedStatus()
                }
            }
        }, TIME_LOADING_PROGRESS)
    }

    private fun startCheckSpeedStatus() {
        Handler(Looper.getMainLooper()).postDelayed(kotlin.run {
            {
                startCheckDurationStatus()
                binding.imageCheckSpeed.visibility = View.VISIBLE
                binding.progressSpeed.visibility = View.GONE
            }
        }, TIME_LOADING_PROGRESS)
    }


    private fun startCheckDurationStatus() {
        Handler(Looper.getMainLooper()).postDelayed(kotlin.run {
            {
                binding.imageCheckDuration.visibility = View.VISIBLE
                binding.progressDuration.visibility = View.GONE
                binding.buttonSelectDevice.isEnabled = true
            }
        }, TIME_LOADING_PROGRESS)
    }

    private fun showDialogRemoveAds() {
        val inAppDialog = InAppDialog(this, PurchaseConstants.PRODUCT_ID_REMOTE_ADS)
        inAppDialog.callback = object : InAppDialog.ICallback {
            override fun onPurcharse() {
                AppPurchase.instance
                    .purchase(this@SelectDeviceActivity,
                        PurchaseConstants.PRODUCT_ID_REMOTE_ADS)
                inAppDialog.dismiss()
            }
        }
        AppPreferences().numberOfTimesDialogRemoveAdsDisplayed = numberOfTimesDisplayed?.plus(1)
        inAppDialog.show()
    }

    private fun initView() {
        binding.imageRemoveAds.setOnClickListener {
            showDialogRemoveAds();
        }
        binding.buttonSelectDevice.setOnClickListener {

            try {
                Constants.SELECT_FROM_SETTING = true
                startActivity(Intent("android.settings.WIFI_DISPLAY_SETTINGS"))
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                try {
                    Constants.SELECT_FROM_SETTING = true
                    startActivity(Intent("com.samsung.wfd.LAUNCH_WFD_PICKER_DLG"))
                } catch (e2: java.lang.Exception) {
                    try {
                        Constants.SELECT_FROM_SETTING = true
                        startActivity(Intent("android.settings.CAST_SETTINGS"))
                    } catch (e3: java.lang.Exception) {
                        Toast.makeText(applicationContext,
                            R.string.not_support_device,
                            Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
            finish()

        }
        binding.imageBack.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
            showNativeAdmob()
    }


    private fun showNativeAdmob() {
        val builder = AdLoader.Builder(this, AdConfig.AD_ADMOB_SELECT_DEVICE_NATIVE_ADVANCED)
        builder.forNativeAd {
            startCheckWifiStatus()
            if (nativeAd != null) {
                nativeAd?.destroy()
            }
            isLoadNative = true
            nativeAd = it
            populateUnifiedNativeAdView(it, binding.nativeAdView)
        }
        val videoOptions = VideoOptions.Builder().build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()
        builder.withNativeAdOptions(adOptions)
        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(errorCode: LoadAdError) {
                isLoadNative = false
                startCheckWifiStatus()
            }
        }).build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun populateUnifiedNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        // Set the media view.
        adView.mediaView = adView.findViewById<View>(R.id.ad_media) as MediaView

        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)

        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline
        adView.mediaView.setMediaContent(nativeAd.mediaContent)

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView.visibility = View.INVISIBLE
        } else {
            adView.bodyView.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }
        if (nativeAd.callToAction == null) {
            adView.callToActionView.visibility = View.INVISIBLE
        } else {
            adView.callToActionView.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }


        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        val vc = nativeAd.mediaContent.videoController

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc.hasVideoContent()) {

            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            vc.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
            }
        }
    }
}