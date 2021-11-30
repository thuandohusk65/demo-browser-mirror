package com.ynsuper.screenmirroring.view.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ynsuper.screenmirroring.databinding.ActivitySplashBinding
import java.lang.Exception

import android.content.Intent
import com.ynsuper.screenmirroring.ads.InterstitialLoader
import com.ynsuper.screenmirroring.utility.AdConfig
import com.ynsuper.screenmirroring.utility.Constants


class SplashActivity : AppCompatActivity() {

    private var isLoadedAd: Boolean = false
    private lateinit var interstitialLoader: InterstitialLoader
    private lateinit var thread: Thread
    private var isRunFistApp = true
    private lateinit var binding: ActivitySplashBinding
    var progressTime = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        Constants.SELECT_FROM_SETTING = false
        initView()
        setContentView(binding.root)

    }

    private fun initView() {
         interstitialLoader= InterstitialLoader()
        interstitialLoader.setAdsId(this, AdConfig.AD_ADMOB_SPLASH_INTERSTITIAL_GO_TO,
            "")
        binding.progressSplash.max = 1000
        startProgressTime()
    }

    private fun checkRunFirstApp() {
        finish()
        val sharedPreferences =
            getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        isRunFistApp = sharedPreferences.getBoolean(Constants.KEY_START_FISRT_APP, true)
        if (isRunFistApp) {
            // Show Tutorial
            val intent = Intent(this, TutorialActivity::class.java)
            intent.putExtra(Constants.EXTRA_TUTORIAL, true)
            startActivity(intent)
        } else {

            // Show Home App
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun startProgressTime() {

        interstitialLoader.showInterstitial(object : InterstitialLoader.AdmobListener{
            override fun onAdLoaded() {
                isLoadedAd = true
                if (isLoadedAd){
                    checkRunFirstApp()
                }
            }

            override fun onAdClosed() {

            }

            override fun onAdOpen() {

            }

        })

         thread = Thread() {
            kotlin.run {
                while (progressTime < 2000) {
                    try {
                        Thread.sleep(10)
                        binding.progressSplash.progress = progressTime
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    progressTime += 10
                }
                if (!isLoadedAd){
                    checkRunFirstApp()
                }

            }
        }
        thread.start()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}