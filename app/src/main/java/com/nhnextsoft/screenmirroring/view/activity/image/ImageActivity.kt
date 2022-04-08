package com.nhnextsoft.screenmirroring.view.activity.image

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nhnextsoft.screenmirroring.databinding.ActivityCastImageBinding

class ImageActivity: AppCompatActivity() {

    private lateinit var binding: ActivityCastImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCastImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
    }
}