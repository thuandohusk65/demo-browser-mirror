package com.ynsuper.screenmirroring.view.activity

import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.DisplayManager.DisplayListener
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ynsuper.screenmirroring.R
import com.ynsuper.screenmirroring.databinding.ActivityHomeBinding
import com.ynsuper.screenmirroring.service.MyForegroundService
import com.ynsuper.screenmirroring.utility.Constants
import com.ynsuper.screenmirroring.utility.Constants.SELECT_FROM_SETTING
import com.ynsuper.screenmirroring.utility.NoInternetDialog
import java.lang.Exception
import android.view.LayoutInflater
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
import com.ynsuper.screenmirroring.ads.FanNativeAdListener
import com.ynsuper.screenmirroring.ads.InterstitialLoader
import com.ynsuper.screenmirroring.utility.AdConfig
import com.ynsuper.screenmirroring.utility.UtilAd
import java.util.ArrayList


class HomeActivity : AppCompatActivity() {

    private lateinit var interstitialLoader: InterstitialLoader
    private var isConnectMirror: Boolean = false
    private lateinit var binding: ActivityHomeBinding
    private lateinit var unifiedNativeAd: com.google.android.gms.ads.nativead.NativeAd
    private lateinit var nativeAdFb: NativeAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
//        if (!SELECT_FROM_SETTING) {
//        } else {
//            val intent = Intent(this, SplashActivity::class.java)
//            startActivity(intent)
//            SELECT_FROM_SETTING = false
//
//            finish()
//        }
        checkConnectionScreenMirroring()

        checkingInternet()
        loadAdsNative()
    }

    private fun loadAdsIntersitial() {
        interstitialLoader= InterstitialLoader()
        interstitialLoader.setAdsId(this, AdConfig.AD_ADMOB_SPLASH_INTERSTITIAL_GO_TO,
            "")
        interstitialLoader.showProgress()
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

    private fun checkingInternet() {
        val wifi = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifi.isWifiEnabled) {
            NoInternetDialog.Builder(this).build()
        }
    }

    private fun checkConnectionScreenMirroring() {
        val displayManager = applicationContext.getSystemService(DISPLAY_SERVICE) as DisplayManager
        Log.d("Ynsuper", "displayListener size: " + displayManager.displays.size)
        if (displayManager.displays.size > 1) {
            // connect mirroring
            isConnectMirror = true
            val displayMirror = displayManager.displays.get(1)
            binding.textDevice.visibility = View.VISIBLE
            binding.textDevice.text = displayMirror.name
            binding.imageCast.setImageResource(R.drawable.ic_disconnect)
            binding.textStateConnect.text = getString(R.string.disconnect)
        } else {
            // not connect
            isConnectMirror = false
            binding.textDevice.visibility = View.GONE
            binding.imageCast.setImageResource(R.drawable.ic_connect)
            binding.textStateConnect.text = getString(R.string.ready_connect)
        }
        val displayListener: DisplayListener = object : DisplayListener {
            override fun onDisplayAdded(displayId: Int) {
                if (SELECT_FROM_SETTING) {
                    SELECT_FROM_SETTING = false
                    startServiceConnection(displayManager.getDisplay(displayId).name)
                    val intent = Intent()
                    intent.action = "test.Broadcast"
                    sendBroadcast(intent)
                    Log.d(
                        "Ynsuper",
                        "test.Broadcast onDisplayAdded " + displayManager.displays.size
                    )

                }

            }

            override fun onDisplayRemoved(displayId: Int) {
                stopServiceConnection()
            }

            override fun onDisplayChanged(displayId: Int) {
//                Toast.makeText(this@HomeActivity, "displayListener onDisplayChanged:$displayId",Toast.LENGTH_SHORT).show()
//                Log.d("Ynsuper","displayListener onDisplayChanged ${displayManager.getDisplay(displayId).name}" )

            }
        }
        displayManager.registerDisplayListener(displayListener, Handler())
    }

    private fun initView() {
//        binding.imageReady.setOnClickListener {
//            val intent = Intent(this, SelectDeviceActivity::class.java)
//            startActivity(intent)
//        }

        //Broadcast


        binding.pulseLayout.start()
        binding.pulseLayout.setOnClickListener {
            loadAdsIntersitial()

            // check connect for show disconnect
            if (isConnectMirror) {
                interstitialLoader.showInterstitial(object : InterstitialLoader.AdmobListener{
                    override fun onAdLoaded() {
                        try {
                            startActivity(Intent("android.settings.CAST_SETTINGS"))
                            SELECT_FROM_SETTING = true
                        } catch (exception1: Exception) {
                            Toast.makeText(
                                applicationContext,
                                R.string.not_support_device,
                                Toast.LENGTH_LONG
                            ).show()

                        }

                    }

                    override fun onAdClosed() {

                    }

                    override fun onAdOpen() {
                    }

                })


            } else {
                interstitialLoader.showInterstitial(object : InterstitialLoader.AdmobListener{
                    override fun onAdLoaded() {
                        val intent = Intent(this@HomeActivity, SelectDeviceActivity::class.java)
                        startActivity(intent)
                    }

                    override fun onAdClosed() {

                    }

                    override fun onAdOpen() {
                        TODO("Not yet implemented")
                    }

                })

            }

        }
        binding.imageLight.setOnClickListener {
            val intent = Intent(this, TutorialActivity::class.java)
            intent.putExtra(Constants.EXTRA_TUTORIAL, false)
            startActivity(intent)
        }
        binding.imageRemoveAds.setOnClickListener {
            val intent = Intent(this, RemoveAdsActivity::class.java)
            startActivity(intent)
        }
        binding.imageSetting.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadNativeAdFacebook() {

        nativeAdFb =  NativeAd(this, AdConfig.AD_FACEBOOK_HOME_NATIVE)
        // Request an ad
        val loadAdConfig = nativeAdFb.buildLoadAdConfig()
            .withMediaCacheFlag(NativeAdBase.MediaCacheFlag.ALL)
            .withAdListener(object : FanNativeAdListener(this@HomeActivity, AdConfig.AD_FACEBOOK_HOME_NATIVE) {
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
    override fun onResume() {
        super.onResume()
        Log.d("Ynsuper", "registerBroadCast");

        if (!SELECT_FROM_SETTING) {
            checkConnectionScreenMirroring()
        } else {
            val intent = Intent(this, SplashActivity::class.java)
            startActivity(intent)
            SELECT_FROM_SETTING = false
            finish()
        }
    }

    fun startServiceConnection(nameTV: String) {

        val myServiceIntent = Intent(this, MyForegroundService::class.java)
        myServiceIntent.putExtra(Constants.SERVICE_EXTRA_NAME_TV, nameTV)
        ContextCompat.startForegroundService(this, myServiceIntent)
    }

    public fun stopServiceConnection() {
        val serviceIntent = Intent(this, MyForegroundService::class.java)
        stopService(serviceIntent)
    }

}