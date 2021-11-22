package com.ynsuper.screenmirroring.view.activity

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ynsuper.screenmirroring.databinding.ActivityHomeBinding
import com.ynsuper.screenmirroring.utility.Constants
import com.ynsuper.screenmirroring.utility.NoInternetDialog

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        initView()
        val wifi = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifi.isWifiEnabled) {
            NoInternetDialog.Builder(this).build()
        }
        setContentView(binding.root)
    }

    private fun initView() {
//        binding.imageReady.setOnClickListener {
//            val intent = Intent(this, SelectDeviceActivity::class.java)
//            startActivity(intent)
//        }
        binding.pulseLayout.start()
        binding.pulseLayout.setOnClickListener {
            val intent = Intent(this, SelectDeviceActivity::class.java)
            startActivity(intent)
        }
        binding.imageLight.setOnClickListener {
            val intent = Intent(this, TutorialActivity::class.java)
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
}