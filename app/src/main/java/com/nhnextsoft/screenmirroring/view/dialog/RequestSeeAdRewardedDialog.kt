package com.nhnextsoft.screenmirroring.view.dialog

import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.nhnextsoft.control.Admod
import com.nhnextsoft.control.funtion.AdCallback
import com.nhnextsoft.control.funtion.RewardCallback
import com.nhnextsoft.screenmirroring.ads.AdConfig
import com.nhnextsoft.screenmirroring.config.AppConfigRemote
import com.nhnextsoft.screenmirroring.databinding.DialogRequestSeeAdRewardedBinding
import com.nhnextsoft.screenmirroring.view.activity.stream.StreamActivity
import timber.log.Timber

class RequestSeeAdRewardedDialog : DialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance() =
            RequestSeeAdRewardedDialog().apply {}
    }

    private var isRewarded: Boolean = false
    private var isLoadAdSuccess: Boolean = false
    private var numberCountDown: Long = 3
    private lateinit var binding: DialogRequestSeeAdRewardedBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        loadAds()
        binding = DialogRequestSeeAdRewardedBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWidthPercent(90)
        binding.btnWatchingAd.setOnClickListener {
            showRewardedAd()
        }
        binding.textCancel.setOnClickListener {
            dismiss()
        }

    }

    private fun setWidthPercent(percentage: Int) {
        val percent = percentage.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun countDownShowAd() {
        val timer = object : CountDownTimer(numberCountDown * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Timber.d("CountDownTimer ${millisUntilFinished}")
                binding.btnWatchingAd.text = "Watching Ad (${numberCountDown})s"
                numberCountDown -= 1
            }

            override fun onFinish() {
                Timber.d("CountDownTimer onFinish")
                binding.btnWatchingAd.text = "Watching Ad (${numberCountDown - 0})s"
                showRewardedAd()
            }
        }
        timer.start()
    }

    private val adCallback = object : AdCallback() {
        override fun onAdLoaded() {
            isLoadAdSuccess = true
            binding.progressLoadAd.visibility = View.INVISIBLE
            binding.btnWatchingAd.visibility = View.VISIBLE
            Timber.d("Ad was loaded.")
            if (AppConfigRemote().isUsingRewardedInterstitialDialog == true) {
                countDownShowAd()
            }
        }

        override fun onAdFailedToLoad(i: LoadAdError?) {
            super.onAdFailedToLoad(i)
            isLoadAdSuccess = false
            binding.progressLoadAd.visibility = View.INVISIBLE
            binding.btnWatchingAd.visibility = View.VISIBLE
            Timber.d("Ad loaded failed")
        }
    }

    private fun loadAds() {
        if (AppConfigRemote().isUsingRewardedInterstitialDialog == true) {
            Admod.instance?.initRewardedInterstitialAds(requireActivity(),
                AdConfig.AD_ADMOB_OPEN_STREAM_WEB_REWARDED_INTERSTITIAL,
                adCallback)
        } else {
            Admod.instance?.initRewardAds(requireActivity(),
                AdConfig.AD_ADMOB_OPEN_STREAM_WEB_REWARDED,
                adCallback)
        }
    }

    private val rewardCallback = object : RewardCallback {
        override fun onUserEarnedReward(rewardVal: RewardItem?) {
            isRewarded = true
        }

        override fun onRewardedAdClosed() {
            Timber.d("onRewardedAdClosed $isRewarded")
            if (isRewarded) {
                dismiss()
                startActivity(StreamActivity.newIntent(requireActivity()))
            } else {
                // TODO
            }
        }

        override fun onRewardedAdFailedToShow(codeError: Int) {
            isRewarded = false
        }
    }

    private fun showRewardedAd() {
        if (AppConfigRemote().isUsingRewardedInterstitialDialog == true) {
            Timber.d("showRewardInterstitialAds")
            Admod.instance?.showRewardInterstitialAds(requireActivity(), rewardCallback)
        } else {
            Timber.d("showRewardAds")
            Admod.instance?.showRewardAds(requireActivity(), rewardCallback)
        }
    }
}