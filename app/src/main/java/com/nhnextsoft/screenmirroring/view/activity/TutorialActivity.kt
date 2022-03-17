package com.nhnextsoft.screenmirroring.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.nhnextsoft.control.Admod
import com.nhnextsoft.control.funtion.AdCallback
import com.nhnextsoft.screenmirroring.Constants
import com.nhnextsoft.screenmirroring.R
import com.nhnextsoft.screenmirroring.ads.AdConfig
import com.nhnextsoft.screenmirroring.config.AppPreferences
import com.nhnextsoft.screenmirroring.databinding.ActivityTutorialBinding
import com.nhnextsoft.screenmirroring.model.TutorialModel
import com.nhnextsoft.screenmirroring.utility.ZoomOutPageTransformer
import com.nhnextsoft.screenmirroring.utility.extensions.isNetworkAvailable
import com.nhnextsoft.screenmirroring.view.adapter.TutorialPagerAdapter

class TutorialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTutorialBinding
    private var currentItemViewPager = 0
    private var mInterstitialAd: InterstitialAd? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        binding.textStep.text =
            getString(R.string.text_step) + " " + 1 + "/" + (loadAllImageTutorial().size)
        loadAdInterstitial()
    }

    private fun initView() {
        binding.viewpagerTutorial.setPageTransformer(true, ZoomOutPageTransformer())
        val tutorialPagerAdapter = TutorialPagerAdapter(this, loadAllImageTutorial())
        binding.viewpagerTutorial.adapter = tutorialPagerAdapter
        binding.imageArrowRight.visibility = View.VISIBLE
        if (intent.hasExtra(Constants.EXTRA_TUTORIAL)
            && intent.getBooleanExtra(Constants.EXTRA_TUTORIAL, false)
        ) {
            binding.imageClose.visibility = View.GONE
            binding.buttonNext.visibility = View.VISIBLE
            Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(FirebaseAnalytics.Param.SCREEN_NAME, "FirstTutorial")
                param(FirebaseAnalytics.Param.SCREEN_CLASS, "TutorialActivity")
                param("FROM_SCREEN", "SPLASH")
            }
        } else {
            Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(FirebaseAnalytics.Param.SCREEN_NAME, "Tutorial")
                param(FirebaseAnalytics.Param.SCREEN_CLASS, "TutorialActivity")
                param("FROM_SCREEN", "Home")
            }
            binding.imageClose.visibility = View.VISIBLE
            binding.buttonNext.visibility = View.GONE
        }
        handleClick()
    }

    private fun loadAdInterstitial() {
        Admod.instance?.getInterstitalAds(this,
            AdConfig.AD_ADMOB_TUTORIAL_BACK_HOME_INTERSTITIAL,
            object : AdCallback() {
                override fun onInterstitialLoad(interstitialAd: InterstitialAd?) {
                    super.onInterstitialLoad(interstitialAd)
                    mInterstitialAd = interstitialAd
                }
            })
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
                        binding.buttonNext.text = getString(R.string.next_screen)
                        binding.imageArrowLeft.visibility = View.INVISIBLE
//                        binding.textStep.visibility = View.VISIBLE
                    }
                    loadAllImageTutorial().size - 1 -> {
                        binding.imageArrowRight.visibility = View.INVISIBLE
                        binding.buttonNext.text = getString(R.string.start_now)
//                        binding.textStep.visibility = View.GONE
                    }
                    else -> {
                        binding.buttonNext.text = getString(R.string.next_screen)
                        binding.imageArrowRight.visibility = View.VISIBLE
                        binding.imageArrowLeft.visibility = View.VISIBLE
//                        binding.textStep.visibility = View.VISIBLE

                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }


        })
        binding.imageArrowLeft.setOnClickListener {
            currentItemViewPager -= 1
            if (currentItemViewPager <= 0) {
                currentItemViewPager = 0
            }
            binding.viewpagerTutorial.currentItem = currentItemViewPager
        }
        binding.imageArrowRight.setOnClickListener {
            currentItemViewPager += 1
            if (currentItemViewPager >= loadAllImageTutorial().size) {
                currentItemViewPager = loadAllImageTutorial().size - 1
            }
            binding.viewpagerTutorial.currentItem = currentItemViewPager

        }
        binding.imageClose.setOnClickListener {
            onBackPressed()
        }

        binding.buttonNext.setOnClickListener {
            currentItemViewPager += 1
            if (binding.buttonNext.text.equals(getString(R.string.start_now))) {
                AppPreferences().completedTheFirstTutorial = true
                Admod.instance?.forceShowInterstitial(this,
                    mInterstitialAd,
                    object : AdCallback() {
                        override fun onAdClosed() {
                            super.onAdClosed()
                            gotoHome()
                        }
                    })
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
    }

    private fun loadAllImageTutorial(): ArrayList<TutorialModel> {
        val arrTutorial = ArrayList<TutorialModel>()
        arrTutorial.add(TutorialModel(R.drawable.ic_step_1, R.string.text_step_1))
        arrTutorial.add(TutorialModel(R.drawable.ic_step_2, R.string.text_step_2))
        arrTutorial.add(TutorialModel(R.drawable.ic_step_3, R.string.text_step_3))
        arrTutorial.add(TutorialModel(R.drawable.ic_step_4, R.string.text_step_4))
        arrTutorial.add(TutorialModel(R.drawable.ic_step_5, R.string.text_step_5))
        return arrTutorial
    }

    override fun onBackPressed() {
        if (!isNetworkAvailable()) {
            finish()
        } else {
            Admod.instance?.showInterstitialAdByTimes(
                this,
                mInterstitialAd,
                object : AdCallback() {
                    override fun onAdClosed() {
                        super.onAdClosed()
                        finish()
                    }
                }
            )
        }
    }
}