package com.ynsuper.screenmirroring.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ynsuper.screenmirroring.databinding.ActivitySplashBinding
import java.lang.Exception
import com.ynsuper.screenmirroring.MainActivity

import android.content.Intent
import com.ynsuper.screenmirroring.utility.Constants


class SplashActivity : AppCompatActivity() {

    private val isRunFistApp = true
    private lateinit var binding: ActivitySplashBinding
    var progressTime = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        initView()
        setContentView(binding.root)

    }

    private fun initView() {
        binding.progressSplash.max = 1000
        startProgressTime()
    }

    private fun checkRunFirstApp() {
        if (isRunFistApp) {
            // Show Tutorial
            val intent = Intent(this, TutorialActivity::class.java)
            intent.putExtra(Constants.EXTRA_TUTORIAL,true)
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
        Thread() {
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
        }.start()
    }

}