package com.nhnextsoft.screenmirroring.view.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.nhnextsoft.screenmirroring.Constants
import com.nhnextsoft.screenmirroring.ads.InterstitialHelper
import com.nhnextsoft.screenmirroring.config.AppPreferences
import com.nhnextsoft.screenmirroring.databinding.ActivitySplashBinding
import com.nhnextsoft.screenmirroring.ads.AdConfig


class SplashActivity : AppCompatActivity() {

    private var isAdLoaded: Boolean = false
    private var isAdLoadError: Boolean = false
    private var isTimeOut: Boolean = false
    private var animator: ValueAnimator? = null
    private val MAX_PROGRESS_SPLASH = 3000
    private lateinit var binding: ActivitySplashBinding
    var interstitialSplash = InterstitialHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Constants.SELECT_FROM_SETTING = false
        initView()
        loadInterstitial()

        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Splash")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "SplashActivity")
        }
    }

    private fun loadInterstitial() {
        interstitialSplash.isShowDialogLoading = false
        interstitialSplash.setAdsId(this,
            AdConfig.AD_ADMOB_SPLASH_INTERSTITIAL,
            object : InterstitialHelper.AdHelperListener {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    isAdLoaded = true
                    gotoNextScreen()
                    interstitialSplash.showInterstitial()
                }

                override fun onAdLoadError() {
                    super.onAdLoadError()
                    isAdLoadError = true
                }

            })
        interstitialSplash.loadAdsInterstitialGoogle()

    }

    private fun gotoNextScreen() {
        val intent = if (AppPreferences().completedTheFirstTutorial == true) gotoHome() else gotoTutorial()
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        if(isAdLoaded){
            if(!isTimeOut) animator?.cancel()
            startActivity(intent)
        }else{
            if (isAdLoadError){
                if(!isTimeOut) animator?.cancel()
                startActivity(intent)
            }
        }
    }

    private fun gotoHome() = HomeActivity.newIntent(this)

    private fun gotoTutorial() = Intent(this, TutorialActivity::class.java).apply {
        putExtra(Constants.EXTRA_TUTORIAL, true)
    }

    private fun initView() {
        binding.progressSplash.max = MAX_PROGRESS_SPLASH
        binding.progressSplash.progress = 0
        startProgressTime()
    }

    private fun startProgressTime() {
        animator = ValueAnimator.ofInt(0, binding.progressSplash.max).apply {
            duration = MAX_PROGRESS_SPLASH.toLong()
            addUpdateListener { animation ->
                binding.progressSplash.progress = animation.animatedValue as Int
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    isTimeOut = true
                    gotoNextScreen()
                }
            })
        }
        animator?.start()
    }

}