package com.ynsuper.screenmirroring.view.activity

import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.DisplayManager.DisplayListener
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ynsuper.screenmirroring.R
import com.ynsuper.screenmirroring.databinding.ActivityHomeBinding
import com.ynsuper.screenmirroring.service.MyForegroundService
import com.ynsuper.screenmirroring.utility.Constants
import com.ynsuper.screenmirroring.utility.NoInternetDialog
import java.lang.Exception

class HomeActivity : AppCompatActivity() {

    private var isConnectMirror: Boolean = false
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

    private fun checkConnectionScreenMirroring() {
        val displayManager = applicationContext.getSystemService(DISPLAY_SERVICE) as DisplayManager
        Log.d("Ynsuper", "displayListener size: " + displayManager.displays.size)
        if (displayManager.displays.size > 1) {
            // connect mirroring
            isConnectMirror = true
            val displayMirror = displayManager.displays.get(1)
            binding.textDevice.visibility = View.VISIBLE
            binding.textDevice.text = displayMirror.name
            binding.imageCast.setImageResource(R.drawable.ic_disconnect)
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
                Toast.makeText(
                    this@HomeActivity,
                    "displayListener onDisplayAdded:$displayId",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(
                    "Ynsuper",
                    "displayListener onDisplayAdded ${displayManager.getDisplay(displayId).name}"
                )
                startServiceConnection(displayManager.getDisplay(displayId).name)
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
//        binding.imageReady.setOnClickListener {
//            val intent = Intent(this, SelectDeviceActivity::class.java)
//            startActivity(intent)
//        }
        binding.pulseLayout.start()
        binding.pulseLayout.setOnClickListener {
            // check connect for show disconnect
            if (isConnectMirror) {
                try {
                    startActivity(Intent("android.settings.CAST_SETTINGS"))
                } catch (exception1: Exception) {
                    Toast.makeText(
                        applicationContext,
                        R.string.not_support_device,
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                val intent = Intent(this, SelectDeviceActivity::class.java)
                startActivity(intent)
            }

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

    override fun onResume() {
        super.onResume()
        checkConnectionScreenMirroring()
    }

    fun startServiceConnection(nameTV: String) {

        val myServiceIntent = Intent(this, MyForegroundService::class.java)
        myServiceIntent.putExtra(Constants.SERVICE_EXTRA_NAME_TV, nameTV)
        ContextCompat.startForegroundService(this, myServiceIntent)
    }
    public fun stopServiceConnection() {
        val serviceIntent = Intent(this, MyForegroundService::class.java)
        stopService(serviceIntent)
    }
}