package com.nhnextsoft.screenmirroring

import android.app.Application
import com.nhnextsoft.screenmirroring.ads.AdConfig
import com.nhnextsoft.screenmirroring.ads.AdmobInitializeHelper
import com.nhnextsoft.screenmirroring.ads.AudienceNetworkInitializeHelper
import com.nhnextsoft.screenmirroring.config.AppPreferences
import com.nhnextsoft.screenmirroring.config.Preferences
import com.nhnextsoft.screenmirroring.utility.extensions.ReleaseTree
import timber.log.Timber
import kotlin.reflect.KProperty

class ScreenMirroringApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }

        AdmobInitializeHelper.initialize(this, AdConfig.DEVICES)
        AudienceNetworkInitializeHelper.initialize(this)
        Preferences.init(this)


        Timber.d(AppPreferences()._defaults.toString())
        Timber.d(AppPreferences().toString())

        AppPreferences().addListener(object : Preferences.SharedPrefsListener{
            override fun onSharedPrefChanged(property: KProperty<*>) {
                Timber.d("property:${property.name} | $property")
            }

        })
    }
}