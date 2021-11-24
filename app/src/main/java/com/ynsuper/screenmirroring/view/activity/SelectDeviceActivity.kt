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
import android.view.View
import com.ynsuper.screenmirroring.R
import com.ynsuper.screenmirroring.utility.NoInternetDialog
import com.ynsuper.screenmirroring.utility.dialog.ViewDialog


class SelectDeviceActivity : AppCompatActivity() {

    private val TIME_LOADING_PROGRESS: Long = 200L

    private lateinit var binding: ActivitySelectDeviceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectDeviceBinding.inflate(layoutInflater)
        initView()
        showViewUpgrade()
        setContentView(binding.root)
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
                finish()
                startActivity(Intent("android.settings.CAST_SETTINGS"))
            } catch (exception1: Exception) {
                Toast.makeText(applicationContext, R.string.not_support_device, Toast.LENGTH_LONG).show()
            }
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