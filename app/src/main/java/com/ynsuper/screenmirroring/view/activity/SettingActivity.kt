package com.ynsuper.screenmirroring.view.activity

import android.content.Intent
import android.content.res.ColorStateList
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
    }

}