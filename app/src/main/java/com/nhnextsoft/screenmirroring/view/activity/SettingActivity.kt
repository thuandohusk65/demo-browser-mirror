package com.nhnextsoft.screenmirroring.view.activity

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.nhnextsoft.control.Admod
import com.nhnextsoft.control.billing.AppPurchase
import com.nhnextsoft.control.dialog.InAppDialog
import com.nhnextsoft.control.dialog.PrepareLoadingAdsDialog
import com.nhnextsoft.control.funtion.AdCallback
import com.nhnextsoft.screenmirroring.BuildConfig
import com.nhnextsoft.screenmirroring.R
import com.nhnextsoft.screenmirroring.ads.AdConfig
import com.nhnextsoft.screenmirroring.ads.PurchaseConstants
import com.nhnextsoft.screenmirroring.config.AppConfigRemote
import com.nhnextsoft.screenmirroring.config.AppPreferences
import com.nhnextsoft.screenmirroring.databinding.ActivitySettingBinding
import com.nhnextsoft.screenmirroring.utility.extensions.isNetworkAvailable
import timber.log.Timber
import java.util.*


class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private lateinit var modalLoadingAd: PrepareLoadingAdsDialog
    private var isLoadedAdInterstitial: Boolean = false
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        modalLoadingAd = PrepareLoadingAdsDialog(this)
        initView()
        setContentView(binding.root)
        loadAdInterstitial()
    }

    private fun loadAdInterstitial() {
        Admod.instance?.getInterstitalAds(this,
            AdConfig.AD_ADMOB_CLOSE_BACK_HOME_INTERSTITIAL,
            object : AdCallback() {
                override fun onInterstitialLoad(interstitialAd: InterstitialAd?) {
                    super.onInterstitialLoad(interstitialAd)
                    mInterstitialAd = interstitialAd
                    isLoadedAdInterstitial = true
                }
                override fun onAdFailedToLoad(i: LoadAdError?) {
                    super.onAdFailedToLoad(i)
                    isLoadedAdInterstitial = true
                }
            })
    }

    private fun initView() {
        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
        binding.buttonBuyNow.animation =
            AnimationUtils.loadAnimation(this, R.anim.anim_zoom_buy_now)
//        binding.switchStyle.setOnCheckedChangeListener { buttonView, isChecked ->
//            if (isChecked) {
//                binding.buttonBuyNow.backgroundTintList = ColorStateList.valueOf(
//                    resources.getColor(R.color.color_button_style2)
//                )
//                binding.imageBackgroundNoAds.setImageResource(R.drawable.ic_bg_remove_ad_2)
//            } else {
//                ad)binding.buttonBuyNow.backgroundTintList = ColorStateList.valueOf(
////                    resources.getColor(R.color.color_button_style1)
////                )
////                binding.imageBackgroundNoAds.setImageResource(R.drawable.ic_bg_remove_
//            }
//        }

        when (AppConfigRemote().bannerUpdateType) {
            2 -> {
                binding.buttonBuyNow.background = ContextCompat.getDrawable(this, R.drawable.btn_buy_now_2)
                binding.imageBackgroundNoAds.setImageResource(R.drawable.bg_remove_ad_2)
            }
            else -> {
                binding.buttonBuyNow.background = ContextCompat.getDrawable(this, R.drawable.btn_buy_now_1)
                binding.imageBackgroundNoAds.setImageResource(R.drawable.bg_remove_ad)
            }
        }
        binding.buttonBuyNow.setOnClickListener {
            val inAppDialog = InAppDialog(this, PurchaseConstants.PRODUCT_ID_REMOTE_ADS)
            inAppDialog.callback = object : InAppDialog.ICallback{
                override fun onPurcharse() {
                    AppPurchase.instance
                        .purchase(this@SettingActivity, PurchaseConstants.PRODUCT_ID_REMOTE_ADS)
                    inAppDialog.dismiss()
                }
            }
            inAppDialog.show()
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


            val email = "taptapstudioapp+screenmirroring@gmail.com"
            val subject = "Feedback by Screen Mirroring"
            val chooserTitle = getString(R.string.title_feedback)

            try {
                composeEmail(arrayOf(email), subject, systemInfo)
            } catch (e: Exception) {
                ShareCompat.IntentBuilder(this)
                    .setType("message/rfc822")
                    .addEmailTo(email)
                    .setSubject(subject)
                    .setText("" + systemInfo)
//                .setHtmlText(systemInfo) //If you are using HTML in your body text
                    .setChooserTitle(chooserTitle)
                    .startChooser()
            }

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
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.you_are_redirected_policy_page))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val url = "https://sites.google.com/view/taptapstudio/privacy-Policy"
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    startActivity(i)
                }.setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }.create().show()

        }

        (getString(R.string.version_app) + " ${BuildConfig.VERSION_NAME}").also { binding.tvVersion.text = it }
    }

    private fun composeEmail(addresses: Array<String>, subject: String, text: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, addresses)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        if (intent.resolveActivity(packageManager) != null) {
            val openInChooser = Intent.createChooser(intent, getString(R.string.title_feedback))
            startActivity(openInChooser)
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

    private fun showAd(): Unit {
        Admod.instance?.forceShowInterstitial(
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
    override fun onBackPressed() {
        if (!isNetworkAvailable()) {
            finish()
        } else {
            if (isLoadedAdInterstitial) {
                showAd()
            } else {
                modalLoadingAd.show()
                Admod.instance?.getInterstitalAds(this,
                    AdConfig.AD_ADMOB_CLOSE_BACK_HOME_INTERSTITIAL,
                    object : AdCallback() {
                        override fun onInterstitialLoad(interstitialAd: InterstitialAd?) {
                            super.onInterstitialLoad(interstitialAd)
                            mInterstitialAd = interstitialAd
                            isLoadedAdInterstitial = true
                            modalLoadingAd.dismiss()
                            showAd()
                        }
                        override fun onAdFailedToLoad(i: LoadAdError?) {
                            super.onAdFailedToLoad(i)
                            isLoadedAdInterstitial = true
                            modalLoadingAd.dismiss()
                            finish()
                        }
                    })
            }
        }
    }

}