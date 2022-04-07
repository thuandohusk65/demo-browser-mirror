package com.nhnextsoft.screenmirroring.config

import com.nhnextsoft.screenmirroring.BuildConfig

class AppConfigRemote: PreferencesAdapterRC(name = "app_config_remote", devMode = BuildConfig.DEBUG) {
    var bannerUpdateType by intPref(defaultValue = 2)
    var isUsingAdsOpenApp by booleanPref(defaultValue = true)
    var numberOfDialogRemoveAdsImpressionsPerDay by intPref(defaultValue = 1)
    val isUsingAdsBannerInTutorial by booleanPref(defaultValue = false)
    val isUsingRewardedInterstitialDialog by booleanPref(defaultValue = true)
}