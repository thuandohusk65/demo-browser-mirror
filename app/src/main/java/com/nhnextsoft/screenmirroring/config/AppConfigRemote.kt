package com.nhnextsoft.screenmirroring.config

import com.nhnextsoft.screenmirroring.BuildConfig

class AppConfigRemote: PreferencesAdapterRC(name = "app_config_remote", devMode = BuildConfig.DEBUG) {
    var bannerUpdateType by intPref(defaultValue = 2)
}