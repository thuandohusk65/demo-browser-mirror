package com.nhnextsoft.screenmirroring.view.activity.web


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nhnextsoft.screenmirroring.databinding.ActivityWebBinding

class WebActivity: AppCompatActivity() {

    private lateinit var binding: ActivityWebBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
    }
}