package com.ynsuper.screenmirroring.view.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ynsuper.screenmirroring.databinding.ActivitySelectDeviceBinding
import android.widget.Toast

import android.content.Intent
import android.os.Handler
import java.lang.Exception
import android.net.wifi.WifiManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.facebook.ads.*
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.ynsuper.screenmirroring.R
import com.ynsuper.screenmirroring.ads.FanNativeAdListener
import com.ynsuper.screenmirroring.utility.AdConfig
import com.ynsuper.screenmirroring.utility.Constants
import com.ynsuper.screenmirroring.utility.NoInternetDialog
import com.ynsuper.screenmirroring.utility.UtilAd
import com.ynsuper.screenmirroring.utility.dialog.ViewDialog
import java.util.ArrayList


class SelectDeviceActivity : AppCompatActivity() {

    private val TIME_LOADING_PROGRESS: Long = 200L

    private lateinit var binding: ActivitySelectDeviceBinding
    private lateinit var unifiedNativeAd: com.google.android.gms.ads.nativead.NativeAd
    private lateinit var nativeAdFb: NativeAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        showViewUpgrade()
        loadAdsNative()

    }
    private fun loadAdsNative() {
        binding.adsWall.shimmerSdLoading.visibility = View.VISIBLE
        binding.adsWall.adContainer.visibility = View.GONE
        if (UtilAd.checkShowFacebookAds(this)) {
            loadNativeAdFacebook()
        } else {
            loadNativeAdGoogle()
        }
    }
    private fun loadNativeAdFacebook() {

        nativeAdFb =  NativeAd(this, AdConfig.AD_FACEBOOK_HOME_NATIVE)
        // Request an ad
        val loadAdConfig = nativeAdFb.buildLoadAdConfig()
            .withMediaCacheFlag(NativeAdBase.MediaCacheFlag.ALL)
            .withAdListener(object : FanNativeAdListener(this@SelectDeviceActivity, AdConfig.AD_FACEBOOK_HOME_NATIVE) {
                override fun onError(ad: Ad, adError: com.facebook.ads.AdError) {
                    super.onError(ad, adError)
                    Log.d("Ynsuper","Native ad failed to load:"+ adError.errorMessage)
                    loadNativeAdGoogle()
                }

                override fun onAdLoaded(ad: Ad) {
                    Log.d("Ynsuper","onAdLoaded facebook")
                    if (nativeAdFb != ad) return
                    if (ad.isAdInvalidated) return
                    showAdsNativeFacebook(nativeAdFb)
                }

                override fun onLoggingImpression(ad: Ad) {
                    super.onLoggingImpression(ad)
                    Log.d("Ynsuper","ad FAN: onLoggingImpression")
                }
            })
            .build()
        nativeAdFb.loadAd(loadAdConfig)
    }

    private fun loadNativeAdGoogle() {
        val videoOptions = VideoOptions.Builder()
            .setStartMuted(false)
            .build()

        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()
        val adRequestBuilder = AdRequest.Builder()
        val adLoader = AdLoader.Builder(this,AdConfig.AD_ADMOB_HOME_NATIVE)
            .forNativeAd { it ->
                Log.d("Ynsuper","loadNativeAdGoogle Success")
                unifiedNativeAd = it
                showAdsNativeGoogle(unifiedNativeAd)
            }
            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdImpression() {
                    super.onAdImpression()
                    Log.d("Ynsuper","onAdImpression")
                }

                override fun onAdFailedToLoad(p0: Int) {
                    Log.d("Ynsuper","onAdFailedToLoad $p0")
                    binding.adsWall.root.visibility = View.GONE
                }
            })
            .withNativeAdOptions(adOptions)
            .build()
        adLoader.loadAd(adRequestBuilder.build())
    }
    private fun showAdsNativeGoogle(unifiedNativeAd: com.google.android.gms.ads.nativead.NativeAd?) {
        Log.d("Ynsuper","showAdsNativeGoogle")
        unifiedNativeAd?.let {
            val adViewNativeGoogle = UtilAd.buildViewNativeGoogleMedium2(this, it)
            binding.adsWall.shimmerSdLoading.visibility = View.GONE
            binding.adsWall.adContainer.visibility = View.VISIBLE
            binding.adsWall.adContainer.removeAllViews()
            binding.adsWall.adContainer.addView(
                adViewNativeGoogle,
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800)
            )
        }
    }
    private fun showAdsNativeFacebook(nativeAd: NativeAd) {
        Log.d("Ynsuper","showAdsNativeFacebook")
        nativeAd.unregisterView()
        binding.adsWall.shimmerSdLoading.visibility = View.GONE
        binding.adsWall.adContainer.visibility = View.VISIBLE
        findViewById<NativeAdLayout>(R.id.fanNativeAdContainer).visibility = View.VISIBLE
        val inflater = LayoutInflater.from(this)
        // Inflate the Ad view.  The layout referenced should be the one you created in the last step.
        val adView =
            inflater.inflate(
                R.layout.ad_facebook_native_ad,
                findViewById<NativeAdLayout>(R.id.fanNativeAdContainer),
                false
            ) as LinearLayout
        findViewById<NativeAdLayout>(R.id.fanNativeAdContainer).addView(adView)

        // Add the AdOptionsView
        val adChoicesContainer: LinearLayout = adView.findViewById(R.id.ad_choices_container)
        val adOptionsView =
            AdOptionsView(this, nativeAd, findViewById<NativeAdLayout>(R.id.fanNativeAdContainer))
        adChoicesContainer.removeAllViews()
        adChoicesContainer.addView(adOptionsView, 0)

        // Create native UI using the ad metadata.
        val nativeAdIcon: MediaView = adView.findViewById(R.id.native_ad_icon)
        val nativeAdTitle: TextView = adView.findViewById(R.id.native_ad_title)
        val nativeAdMedia: MediaView = adView.findViewById(R.id.native_ad_media)
        val nativeAdSocialContext: TextView = adView.findViewById(R.id.native_ad_social_context)
        val nativeAdBody: TextView = adView.findViewById(R.id.native_ad_body)
        val sponsoredLabel: TextView = adView.findViewById(R.id.native_ad_sponsored_label)
        val nativeAdCallToAction: Button = adView.findViewById(R.id.native_ad_call_to_action)

        // Set the Text.
        nativeAdTitle.text = nativeAd.advertiserName
        nativeAdBody.text = nativeAd.adBodyText
        nativeAdSocialContext.text = nativeAd.adSocialContext
        nativeAdCallToAction.visibility = if (nativeAd.hasCallToAction()) {
            View.VISIBLE
        } else View.INVISIBLE
        nativeAdCallToAction.text = nativeAd.adCallToAction
        sponsoredLabel.text = nativeAd.sponsoredTranslation

        // Create a list of clickable views
        val clickableViews: MutableList<View> = ArrayList()
        clickableViews.add(nativeAdTitle)
        clickableViews.add(nativeAdCallToAction)
        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(
            adView,
            nativeAdMedia,
            nativeAdIcon,
            clickableViews
        )

        NativeAdBase.NativeComponentTag.tagView(
            nativeAdIcon,
            NativeAdBase.NativeComponentTag.AD_ICON
        )
        NativeAdBase.NativeComponentTag.tagView(
            nativeAdTitle,
            NativeAdBase.NativeComponentTag.AD_TITLE
        )
        NativeAdBase.NativeComponentTag.tagView(
            nativeAdBody,
            NativeAdBase.NativeComponentTag.AD_BODY
        )
        NativeAdBase.NativeComponentTag.tagView(
            nativeAdSocialContext,
            NativeAdBase.NativeComponentTag.AD_SOCIAL_CONTEXT
        )
        NativeAdBase.NativeComponentTag.tagView(
            nativeAdCallToAction,
            NativeAdBase.NativeComponentTag.AD_CALL_TO_ACTION
        )
    }
    private fun showViewUpgrade() {
        ViewDialog.showDialogUpgrade(this)
    }

    private fun startCheckWifiStatus() {
        Handler().postDelayed(kotlin.run {
            {
                val wifi = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                if (wifi.isWifiEnabled) {
                    binding.imageCheckWifi.visibility = View.VISIBLE
                    binding.progressWifi.visibility = View.GONE
                    startCheckSpeedStatus()
                } else {
                    // show Dialog check wifi
                    NoInternetDialog.Builder(this).build()
                }
            }
        }, TIME_LOADING_PROGRESS)
    }

    private fun startCheckSpeedStatus() {
        Handler().postDelayed(kotlin.run {
            {
                startCheckDurationStatus()
                binding.imageCheckSpeed.visibility = View.VISIBLE
                binding.progressSpeed.visibility = View.GONE
            }
        }, TIME_LOADING_PROGRESS)
    }


    private fun startCheckDurationStatus() {
        Handler().postDelayed(kotlin.run {
            {
                binding.imageCheckDuration.visibility = View.VISIBLE
                binding.progressDuration.visibility = View.GONE
                binding.buttonSelectDevice.isEnabled = true
            }
        }, TIME_LOADING_PROGRESS)
    }

    private fun initView() {

        binding.buttonSelectDevice.setOnClickListener {
            try {
                Constants.SELECT_FROM_SETTING = true
                startActivity(Intent("android.settings.CAST_SETTINGS"))
            } catch (exception1: Exception) {
                Toast.makeText(applicationContext, R.string.not_support_device, Toast.LENGTH_LONG)
                    .show()
            }
            finish()

        }
        binding.imageBack.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        startCheckWifiStatus()

    }
}