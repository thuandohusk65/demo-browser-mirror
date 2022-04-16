package com.nhnextsoft.screenmirroring.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.nhnextsoft.control.Admod
import com.nhnextsoft.control.AppOpenManager
import com.nhnextsoft.control.dialog.PrepareLoadingAdsDialog
import com.nhnextsoft.control.funtion.AdCallback
import com.nhnextsoft.screenmirroring.Constants
import com.nhnextsoft.screenmirroring.R
import com.nhnextsoft.screenmirroring.ads.AdConfig
import com.nhnextsoft.screenmirroring.config.AppConfigRemote
import com.nhnextsoft.screenmirroring.config.AppPreferences
import com.nhnextsoft.screenmirroring.databinding.ActivityTutorialBinding
import com.nhnextsoft.screenmirroring.model.TutorialModel
import com.nhnextsoft.screenmirroring.utility.ZoomOutPageTransformer
import com.nhnextsoft.screenmirroring.utility.extensions.isNetworkAvailable
import com.nhnextsoft.screenmirroring.view.adapter.TutorialPagerAdapter
import timber.log.Timber

class TutorialActivity : AppCompatActivity() {

    private var isEndOfStep: Boolean = false;
    private lateinit var modalLoadingAd: PrepareLoadingAdsDialog
    private lateinit var binding: ActivityTutorialBinding
    private var currentItemViewPager = 0
    private var mInterstitialAd: InterstitialAd? = null
    private var isFirstOpen = false;
    private var nativeAd: NativeAd? = null
    private var isLoadNative: Boolean = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        modalLoadingAd = PrepareLoadingAdsDialog(this)
        binding.textStep.text =
            getString(R.string.text_step) + " " + 1 + "/" + (loadAllImageTutorial().size)
        showNativeAdmob()
        loadAds()
    }

    private fun loadAds() {
        Timber.d("LoadAd Tutorial ${AppConfigRemote().isUsingAdsBannerInTutorial}")
        if (AppConfigRemote().isUsingAdsBannerInTutorial == true) {
            binding.flAdplaceholder.visibility = View.GONE
            loadBanner()
        } else {
            binding.bannerContainer.visibility = View.GONE
            showNativeAdmob()
        }
    }

    private fun loadBanner() {
        Admod.instance?.loadBannerWithAdSize(
            this,
            AdConfig.AD_ADMOB_TUTORIAL_BANNER,
            AdSize.MEDIUM_RECTANGLE
        )
    }

    private fun initView() {
        binding.viewpagerTutorial.setPageTransformer(true, ZoomOutPageTransformer())
        val tutorialPagerAdapter = TutorialPagerAdapter(this, loadAllImageTutorial())
        binding.viewpagerTutorial.adapter = tutorialPagerAdapter
        binding.textNextStep.visibility = View.VISIBLE
        binding.textBackStep.visibility = View.GONE
        if (intent.hasExtra(Constants.EXTRA_TUTORIAL)
            && intent.getBooleanExtra(Constants.EXTRA_TUTORIAL, false)
        ) {
            binding.imageClose.visibility = View.GONE
            binding.textNextStep.visibility = View.VISIBLE
            Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(FirebaseAnalytics.Param.SCREEN_NAME, "FirstTutorial")
                param(FirebaseAnalytics.Param.SCREEN_CLASS, "TutorialActivity")
                param("FROM_SCREEN", "SPLASH")
            }
            isFirstOpen = true
        } else {
            Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(FirebaseAnalytics.Param.SCREEN_NAME, "Tutorial")
                param(FirebaseAnalytics.Param.SCREEN_CLASS, "TutorialActivity")
                param("FROM_SCREEN", "Home")
            }
            binding.imageClose.visibility = View.VISIBLE
            binding.textNextStep.visibility = View.VISIBLE
        }
        handleClick()
    }

    private fun showNativeAdmob() {
        val builder = AdLoader.Builder(this, AdConfig.AD_ADMOB_TUTORIAL_NATIVE_ADVANCED)
        builder.forNativeAd {
            if (nativeAd != null) {
                nativeAd?.destroy()
            }
            isLoadNative = true
            nativeAd = it
            Admod.instance?.populateUnifiedNativeAdView(this, it, binding.nativeAdView)
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

    private fun handleClick() {
        binding.viewpagerTutorial.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) {

            }

            override fun onPageSelected(position: Int) {
                @SuppressLint("SetTextI18n")
                binding.textStep.text =
                    getString(R.string.text_step) + " " + (position + 1) + "/" + (loadAllImageTutorial().size)
                currentItemViewPager = position

                when (position) {
                    0 -> {
                        isEndOfStep = false
                        binding.textBackStep.visibility = View.GONE
                    }
                    loadAllImageTutorial().size - 1 -> {
                        binding.textNextStep.text = getString(R.string.start_now)
                        isEndOfStep = true
                    }
                    else -> {
                        binding.textNextStep.text = getString(R.string.next_screen)
                        binding.textBackStep.visibility = View.VISIBLE
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }


        })
        binding.textBackStep.setOnClickListener {
            currentItemViewPager -= 1
            if (currentItemViewPager <= 0) {
                currentItemViewPager = 0
            }
            binding.viewpagerTutorial.currentItem = currentItemViewPager
        }
        binding.textNextStep.setOnClickListener {
            currentItemViewPager += 1
            if (currentItemViewPager >= loadAllImageTutorial().size) {
                currentItemViewPager = loadAllImageTutorial().size - 1
            }
            binding.viewpagerTutorial.currentItem = currentItemViewPager

        }
        binding.imageClose.setOnClickListener {
            onBackPressed()
        }

        binding.textNextStep.setOnClickListener {
            currentItemViewPager += 1
            if (isEndOfStep) {
                AppPreferences().completedTheFirstTutorial = true
                modalLoadingAd.show()
                Admod.instance?.getInterstitalAds(this,
                    AdConfig.AD_ADMOB_TUTORIAL_BACK_HOME_INTERSTITIAL,
                    object : AdCallback() {
                        override fun onInterstitialLoad(interstitialAd: InterstitialAd?) {
                            super.onInterstitialLoad(interstitialAd)
                            mInterstitialAd = interstitialAd
                            modalLoadingAd.dismiss()
                            showAd()
                        }

                        override fun onAdFailedToLoad(i: LoadAdError?) {
                            modalLoadingAd.dismiss()
                            gotoHome()
                        }
                    })
                AppOpenManager.instance?.enableAppResumeWithActivity(TutorialActivity::class.java)
            }
            if (currentItemViewPager >= loadAllImageTutorial().size) {
                currentItemViewPager = loadAllImageTutorial().size - 1
            }
            binding.viewpagerTutorial.currentItem = currentItemViewPager
        }
    }

    private fun gotoHome() {
        val intent = HomeActivity.newIntent(this).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        finish()

    }

    private fun loadAllImageTutorial(): ArrayList<TutorialModel> {
        val arrTutorial = ArrayList<TutorialModel>()
        arrTutorial.add(TutorialModel(R.drawable.ic_step_1, R.string.text_step_1))
//        arrTutorial.add(TutorialModel(R.drawable.ic_step_2, R.string.text_step_2))
        arrTutorial.add(TutorialModel(R.drawable.ic_step_3, R.string.text_step_3))
//        arrTutorial.add(TutorialModel(R.drawable.ic_step_4, R.string.text_step_4))
        arrTutorial.add(TutorialModel(R.drawable.ic_step_5, R.string.text_step_5))
        return arrTutorial
    }

    private fun showAd() {
        Admod.instance?.forceShowInterstitial(
            this,
            mInterstitialAd,
            object : AdCallback() {
                override fun onAdClosed() {
                    super.onAdClosed()
                    gotoHome()
                }
            }
        )
    }

    override fun onBackPressed() {
        if (!isNetworkAvailable()) {
            finish()
        } else {
            modalLoadingAd.show()
            Admod.instance?.getInterstitalAds(this,
                AdConfig.AD_ADMOB_TUTORIAL_BACK_HOME_INTERSTITIAL,
                object : AdCallback() {
                    override fun onInterstitialLoad(interstitialAd: InterstitialAd?) {
                        super.onInterstitialLoad(interstitialAd)
                        mInterstitialAd = interstitialAd
                        modalLoadingAd.dismiss()
                        showAd()
                    }

                    override fun onAdFailedToLoad(i: LoadAdError?) {
                        modalLoadingAd.dismiss()
                        gotoHome()
                    }
                })
        }
    }
}