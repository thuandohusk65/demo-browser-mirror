package com.ynsuper.screenmirroring.view.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import com.ynsuper.screenmirroring.BuildConfig
import com.ynsuper.screenmirroring.R
import com.ynsuper.screenmirroring.databinding.ActivitySettingBinding
import java.lang.RuntimeException
import java.util.*

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        initView()
        setContentView(binding.root)
    }

    private fun initView() {
        binding.imageBack.setOnClickListener {
            finish()
        }
        binding.buttonBuyNow.animation =
            AnimationUtils.loadAnimation(this, R.anim.anim_zoom_buy_now)
        binding.switchStyle.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.buttonBuyNow.backgroundTintList = ColorStateList.valueOf(
                    resources.getColor(R.color.color_button_style2)
                )
                binding.imageBackgroundNoAds.setImageResource(R.drawable.ic_bg_remove_ad_2)
            } else {
                binding.buttonBuyNow.backgroundTintList = ColorStateList.valueOf(
                    resources.getColor(R.color.color_button_style1)
                )
                binding.imageBackgroundNoAds.setImageResource(R.drawable.ic_bg_remove_ad)
            }
        }
        binding.buttonBuyNow.setOnClickListener {
            val intent = Intent(this, RemoveAdsActivity::class.java)
            startActivity(intent)
        }
        binding.layoutCheckUpdate.setOnClickListener {
            openAppInStore()
        }
        binding.layoutFeedBack.setOnClickListener {
            var systemInfo = "\n\n\n\n==== SYSTEM-INFO ===\n"
            systemInfo += "Device : " + Build.DEVICE + "\n"
            systemInfo += "SDK Version : " + Build.VERSION.SDK_INT + "\n"
            systemInfo += "App Version : " + BuildConfig.VERSION_NAME + "\n"
            systemInfo += "Language : " + Locale.getDefault().language + "\n"
            systemInfo += "TimeZone : " + TimeZone.getDefault().id + "\n"
            systemInfo += "Total Memory : " + getTotalMemory() + "\n"
            systemInfo += "Free Memory : " + getAvaiableMemory() + "\n"
            systemInfo += "Device Type : " + Build.MODEL + "\n"
            systemInfo += "Data Type : " + networkType() + "\n"

            Log.d("Ynsuper", "System-Info: $systemInfo")

            val email = "testGmail@gmail.com"
            val subject = "Feedback by Screen Mirroring"
            val body = "body"
            val chooserTitle = " /* Your chooser title here */"

            ShareCompat.IntentBuilder.from(this@SettingActivity)
                .setType("message/rfc822")
                .addEmailTo(email)
                .setSubject(subject)
                .setText(body + systemInfo) //.setHtmlText(body) //If you are using HTML in your body text
                .setChooserTitle(chooserTitle)
                .startChooser()
        }
        binding.layoutRateUs.setOnClickListener {
            openAppInStore()
        }
        binding.layoutMoreApp.setOnClickListener {
            val url = "https://play.google.com/store/apps/dev?id=8386891944232233548"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
        binding.layoutPrivacyPolicy.setOnClickListener {
//            https://play.google.com/store/apps/dev?id=8386891944232233548
            val url = "https://play.google.com/store/apps/dev?id=8386891944232233548"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
    }

    private fun getAvaiableMemory(): Long {
        val mi = ActivityManager.MemoryInfo()
        val activityManager: ActivityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(mi)
        return mi.availMem / 1048576L
    }

    private fun getTotalMemory(): Long? {
        val mi = ActivityManager.MemoryInfo()
        val activityManager: ActivityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(mi)
        return mi.totalMem / 1048576L
    }

    @SuppressLint("MissingPermission")
    private fun networkType(): String {
        val conMan: ConnectivityManager =
            getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val mobile: NetworkInfo.State = conMan.getNetworkInfo(0)!!.state
        val wifi: NetworkInfo.State = conMan.getNetworkInfo(1)!!.state
        if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
            //mobile
            return "Mobile"
        } else if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
            //wifi
            return "Wifi"
        }
        return "Unknown"
    }

    private fun openAppInStore() {
        val uri: Uri = Uri.parse("market://details?id=$packageName")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

}