package com.nhnextsoft.screenmirroring.view.activity.video

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nhnextsoft.screenmirroring.databinding.ActivityCastVideoBinding

class VideoActivity: AppCompatActivity() {

    private lateinit var binding: ActivityCastVideoBinding
            
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCastVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
    }
}