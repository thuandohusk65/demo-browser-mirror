package com.nhnextsoft.screenmirroring

import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.ads.AdActivity
import com.nhnextsoft.control.AdmodSP
import com.nhnextsoft.control.AppOpenManager
import com.nhnextsoft.control.application.SupportAdsApplication
import com.nhnextsoft.control.billing.AppPurchase
import com.nhnextsoft.screenmirroring.ads.AdConfig
import com.nhnextsoft.screenmirroring.ads.PurchaseConstants.PRODUCT_ID_REMOTE_ADS
import com.nhnextsoft.screenmirroring.config.AppPreferences
import com.nhnextsoft.screenmirroring.config.Preferences
import com.nhnextsoft.screenmirroring.utility.extensions.ReleaseTree
import com.nhnextsoft.screenmirroring.view.activity.SplashActivity
import timber.log.Timber
import kotlin.reflect.KProperty


class ScreenMirroringApp : SupportAdsApplication() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        initAds()

        Preferences.init(this)


        AppPreferences().addListener(object : Preferences.SharedPrefsListener {
            override fun onSharedPrefChanged(property: KProperty<*>) {
                Timber.d("property:${property.name} | $property")
            }

        })
    }

    private fun initAds() {
        if (PRODUCT_ID_REMOTE_ADS.isNotEmpty()) {
            AppPurchase.instance.initBilling(this)
            AppPurchase.instance.setProductId(PRODUCT_ID_REMOTE_ADS)
            AppPurchase.instance.discount = 0.5
        }
        AppOpenManager.instance?.disableAppResumeWithActivity(AdActivity::class.java)
        AppOpenManager.instance?.disableAppResumeWithActivity(SplashActivity::class.java)
        AdmodSP.instance?.setOpenActivityAfterShowInterAds(false)
        AppOpenManager.instance?.disableAppResume()
//        AdmodSP.instance?.setFan(true)
//        AdmodSP.instance?.setAppLovin(true)
    }

    override fun enableAdsResume(): Boolean = true

    //    override val listTestDeviceId: List<String> = AdConfig.DEVICES
//    override val openAppAdId: String = AdConfig.AD_ADMOB_OPEN_APP
    override fun buildDebug(): Boolean = BuildConfig.DEBUG

    override val listTestDeviceId: List<String> = AdConfig.DEVICES
    override val openAppAdId: String = AdConfig.AD_ADMOB_OPEN_APP
}