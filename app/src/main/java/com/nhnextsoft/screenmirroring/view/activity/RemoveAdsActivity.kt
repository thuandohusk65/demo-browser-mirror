package com.nhnextsoft.screenmirroring.view.activity

import android.graphics.Paint
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.nhnextsoft.screenmirroring.R
import com.nhnextsoft.screenmirroring.databinding.ActivityRemoveAdBinding

class RemoveAdsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRemoveAdBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemoveAdBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.imageBack.setOnClickListener {
            finish()
        }
        binding.buttonBuyForever.animation = AnimationUtils.loadAnimation(this, R.anim.anim_zoom_buy_now)
        binding.buttonBuyForever.setOnClickListener {
        }

        binding.tvOriginalPrice.paintFlags = binding.tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

    }
}