package com.nhnextsoft.screenmirroring.view.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.nhnextsoft.control.Admod
import com.nhnextsoft.control.AppOpenManager
import com.nhnextsoft.control.funtion.AdCallback
import com.nhnextsoft.screenmirroring.Constants
import com.nhnextsoft.screenmirroring.ads.AdConfig
import com.nhnextsoft.screenmirroring.config.AppConfigRemote
import com.nhnextsoft.screenmirroring.config.AppPreferences
import com.nhnextsoft.screenmirroring.databinding.ActivitySplashBinding
import timber.log.Timber
import kotlin.math.roundToInt


class SplashActivity : AppCompatActivity() {


    private var isTimeOut: Boolean = false
    private var animator: ValueAnimator? = null
    private val MAX_PROGRESS_SPLASH = 5000
    private val ADS_LOADING_TIMEOUT = 30000
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSplash()
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Splash")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "SplashActivity")
        }
    }

    private fun initSplash() {
        Constants.SELECT_FROM_SETTING = false
        initView()
        loadInterstitial()
    }

    private fun loadInterstitial() {
        if (AppPreferences().completedTheFirstTutorial == true) {
            if (AppConfigRemote().isUsingAdsOpenApp == true) {
                AppOpenManager.instance?.setFullScreenContentCallback(object :
                    FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        AppOpenManager.instance?.removeFullScreenContentCallback()
                        startActivity(gotoHome())
                    }
                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        super.onAdFailedToShowFullScreenContent(p0)
                        AppOpenManager.instance?.removeFullScreenContentCallback()
                        startActivity(gotoHome())
                    }
                })
            } else {
                Admod.instance?.loadSplashInterstitial(this,
                    AdConfig.AD_ADMOB_SPLASH_INTERSTITIAL,
                    10000,
                    5000,
                    object : AdCallback() {
                         override fun onAdClosed() {
                             startActivity(gotoHome())
                        }
                        override fun onAdFailedToLoad(i: LoadAdError?) {
                            startActivity(gotoHome())
                        }
                    })
            }

        } else {
            startActivity(gotoTutorial())
        }
    }

    private fun gotoHome() = HomeActivity.newIntent(this).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    private fun gotoTutorial() = Intent(this, TutorialActivity::class.java).apply {
        putExtra(Constants.EXTRA_TUTORIAL, true)
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
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
                binding.progressSplash.progress =
                    ((animation.animatedValue as Int) * 0.99).roundToInt()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    isTimeOut = true
//                    gotoNextScreen()
                }
            })
        }
        animator?.start()
    }

}