package com.nhnextsoft.screenmirroring.view.activity.youtube


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nhnextsoft.screenmirroring.databinding.ActivityCastYoutubeBinding

class YoutubeActivity: AppCompatActivity() {

    private lateinit var binding: ActivityCastYoutubeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCastYoutubeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
    }
}