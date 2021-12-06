package com.nhnextsoft.screenmirroring.view.activity

import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.DisplayManager.DisplayListener
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.nhnextsoft.screenmirroring.Constants
import com.nhnextsoft.screenmirroring.Constants.SELECT_FROM_SETTING
import com.nhnextsoft.screenmirroring.R
import com.nhnextsoft.screenmirroring.ads.AdConfig
import com.nhnextsoft.screenmirroring.ads.InterstitialHelper
import com.nhnextsoft.screenmirroring.config.AppConfigRemote
import com.nhnextsoft.screenmirroring.config.AppPreferences
import com.nhnextsoft.screenmirroring.databinding.ActivityHomeBinding
import com.nhnextsoft.screenmirroring.service.MyForegroundService
import com.nhnextsoft.screenmirroring.utility.extensions.checkConnectWifi
import com.nhnextsoft.screenmirroring.utility.extensions.isNetworkAvailable
import com.nhnextsoft.screenmirroring.view.dialog.NoWifiFragment
import timber.log.Timber
import java.util.*


class HomeActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            return intent
        }
    }

    private val interstitialBackFinishApp = InterstitialHelper()
    private val interstitialSelectDevice = InterstitialHelper()
    private var isConnectMirror: Boolean = false
    private lateinit var binding: ActivityHomeBinding
    private var nativeAd: NativeAd? = null
    private var isLoadNative: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Home")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "HomeActivity")
        }

        Timber.d("AppConfigRemote().bannerUpdateType: ${AppConfigRemote().bannerUpdateType}")
        Timber.d("AppPreferences().completedTheFirstTutorial: ${AppPreferences().completedTheFirstTutorial}")
        initView()
        showNativeAdmob()
        checkConnectionScreenMirroring()

        checkingInternet()

    }

    private fun checkingInternet() {
        if (!this.checkConnectWifi()) {
            NoWifiFragment.newInstance().show(supportFragmentManager, "NoWifiFragment")
        }
    }

    private fun checkConnectionScreenMirroring() {
        val displayManager = applicationContext.getSystemService(DISPLAY_SERVICE) as DisplayManager
        Timber.d("displayListener size: " + displayManager.displays.size)
        if (displayManager.displays.size > 1) {
            // connect mirroring
            isConnectMirror = true
            val displayMirror = displayManager.displays.get(1)
            binding.textDevice.visibility = View.VISIBLE
            binding.textDevice.text = displayMirror.name
            binding.imageCast.setImageResource(R.drawable.ic_disconnect_2)
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
                    Timber.d("test.Broadcast onDisplayAdded " + displayManager.displays.size)

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
        binding.pulseLayout.start()
        binding.pulseLayout.setOnClickListener {
            openScreenMirroring()
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

    private fun openScreenMirroring() {
        if (isConnectMirror) {
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
        } else {
            interstitialSelectDevice.setAdsId(this,
                AdConfig.AD_ADMOB_HOME_TO_SELECT_DEVICE_INTERSTITIAL,
                object : InterstitialHelper.AdHelperListener {
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        Timber.d("interstitialBackFinishApp onAdLoaded")
                        openSelectDevices()
                        if (interstitialSelectDevice.isAdLoaded) {
                            interstitialSelectDevice.showInterstitial()
                        }
                    }

                    override fun onAdLoadError() {
                        super.onAdLoadError()
                        interstitialSelectDevice.progressDialog?.dismiss()
                        openSelectDevices()
                    }

                })
            interstitialSelectDevice.loadAdsInterstitialGoogle()
        }
    }

    private fun openSelectDevices() {
        val intent = Intent(this@HomeActivity, SelectDeviceActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        Timber.d("registerBroadCast")

        if (!SELECT_FROM_SETTING) {
            checkConnectionScreenMirroring()
        } else {
            val intent = Intent(this, SplashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
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

    fun stopServiceConnection() {
        val serviceIntent = Intent(this, MyForegroundService::class.java)
        stopService(serviceIntent)
    }

    private fun showNativeAdmob() {
        val builder = AdLoader.Builder(this, AdConfig.AD_ADMOB_HOME_UI_NATIVE_ADVANCED)
        builder.forNativeAd { unifiedNativeAd ->
            if (nativeAd != null) {
                nativeAd?.destroy()
            }
            isLoadNative = true
            nativeAd = unifiedNativeAd
            populateUnifiedNativeAdView(unifiedNativeAd, binding.nativeAdView)
        }
        val videoOptions = VideoOptions.Builder().build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()
        builder.withNativeAdOptions(adOptions)
        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(errorCode: LoadAdError) {
                isLoadNative = false
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


    override fun onBackPressed() {
//        super.onBackPressed()
        if (!isNetworkAvailable()) {
            startActivity(FinishAppActivity.newIntent(this))
        } else {
            interstitialBackFinishApp.setAdsId(this,
                AdConfig.AD_ADMOB_CLOSE_BACK_HOME_INTERSTITIAL,
                object : InterstitialHelper.AdHelperListener {
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        Timber.d("interstitialBackFinishApp onAdLoaded")
                        startActivity(FinishAppActivity.newIntent(this@HomeActivity))
                        if (interstitialBackFinishApp.isAdLoaded) {
                            interstitialBackFinishApp.showInterstitial()
                        }
                    }

                    override fun onAdLoadError() {
                        super.onAdLoadError()
                        interstitialBackFinishApp.progressDialog?.dismiss()
                        startActivity(FinishAppActivity.newIntent(this@HomeActivity))
                    }

                })
            interstitialBackFinishApp.loadAdsInterstitialGoogle()
        }
    }
}