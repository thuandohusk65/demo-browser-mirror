package com.ynsuper.screenmirroring.view.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.ynsuper.screenmirroring.R
import com.ynsuper.screenmirroring.databinding.ActivitySettingBinding

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
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:abc@xyz.com")
            }
            startActivity(Intent.createChooser(emailIntent, "Send feedback"))
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

    private fun openAppInStore() {
        val uri: Uri = Uri.parse("market://details?id=$packageName")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=$packageName")))
        }
    }

}