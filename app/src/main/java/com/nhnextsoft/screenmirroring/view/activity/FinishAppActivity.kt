package com.nhnextsoft.screenmirroring.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.nhnextsoft.screenmirroring.R
import com.nhnextsoft.screenmirroring.databinding.ActivityFinishAppBinding

class FinishAppActivity : AppCompatActivity() {


    private lateinit var binding: ActivityFinishAppBinding
    private var timer: CountDownTimer = object : CountDownTimer(3000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            (getString(R.string.exit) + "..." + (millisUntilFinished / 1000) + "s").also {
                binding.tvExit.text = it
            }
        }

        override fun onFinish() {
            if (isFinishing.not()) {
                finishAffinity()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.NoActionBar)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_finish_app)
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "FinishApp")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "FinishAppActivity")
        }
    }

    override fun onResume() {
        super.onResume()
        timer.start()
    }

    override fun onPause() {
        super.onPause()
        timer.cancel()
    }


    override fun onBackPressed() {
        timer.cancel()
        finishAffinity()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, FinishAppActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            return intent
        }
    }
}