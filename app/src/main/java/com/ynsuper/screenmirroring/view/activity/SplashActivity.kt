package com.ynsuper.screenmirroring.view.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ynsuper.screenmirroring.databinding.ActivitySplashBinding
import java.lang.Exception
import com.ynsuper.screenmirroring.MainActivity

import android.content.Intent
import com.ynsuper.screenmirroring.utility.Constants


class SplashActivity : AppCompatActivity() {

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
        binding.progressSplash.max = 1000
        startProgressTime()
    }

    private fun checkRunFirstApp() {
        val sharedPreferences =
            getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        isRunFistApp = sharedPreferences.getBoolean(Constants.KEY_START_FISRT_APP, true)
        if (isRunFistApp) {
            // Show Tutorial
            val intent = Intent(this, TutorialActivity::class.java)
            intent.putExtra(Constants.EXTRA_TUTORIAL, true)
            startActivity(intent)
            finish()
        } else {
            // Show Home App
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun startProgressTime() {


         thread = Thread() {
            kotlin.run {
                while (progressTime < 1000) {
                    try {
                        Thread.sleep(10)
                        binding.progressSplash.progress = progressTime
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    progressTime += 10
                }

                checkRunFirstApp()

            }
        }
        thread.start()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}