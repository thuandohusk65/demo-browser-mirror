package com.nhnextsoft.control.funtion

import com.google.android.gms.ads.rewarded.RewardItem

interface RewardCallback {
    fun onUserEarnedReward(var1: RewardItem?)
    fun onRewardedAdClosed()
    fun onRewardedAdFailedToShow(codeError: Int)
}