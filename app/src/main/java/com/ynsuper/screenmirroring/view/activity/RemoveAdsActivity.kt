package com.ynsuper.screenmirroring.view.activity

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ynsuper.screenmirroring.R
import com.ynsuper.screenmirroring.databinding.ActivityRemoveAdBinding

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
    }
}