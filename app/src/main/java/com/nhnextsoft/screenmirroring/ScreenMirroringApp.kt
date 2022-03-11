package com.nhnextsoft.screenmirroring

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.ads.AdActivity
import com.nhnextsoft.control.Admod
import com.nhnextsoft.control.AppOpenManager
import com.nhnextsoft.control.application.SupportAdsApplication
import com.nhnextsoft.control.billing.AppPurchase
import com.nhnextsoft.screenmirroring.ads.AdConfig
import com.nhnextsoft.screenmirroring.ads.PurchaseConstants
import com.nhnextsoft.screenmirroring.ads.PurchaseConstants.PRODUCT_ID_REMOTE_ADS
import com.nhnextsoft.screenmirroring.config.AppPreferences
import com.nhnextsoft.screenmirroring.config.Preferences
import com.nhnextsoft.screenmirroring.utility.extensions.ReleaseTree
import com.nhnextsoft.screenmirroring.view.activity.SplashActivity
import timber.log.Timber


class ScreenMirroringApp : SupportAdsApplication() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        Preferences.init(this)
        initAds()

//        AppPreferences().addListener(object : Preferences.SharedPrefsListener {
//            override fun onSharedPrefChanged(property: KProperty<*>) {
//                Timber.d("property:${property.name} | $property")
//            }
//
//        })
    }

    private fun initAds() {
        if (PRODUCT_ID_REMOTE_ADS.isNotEmpty()) {
            AppPurchase.instance.initBilling(this)
            AppPurchase.instance.setProductId(PRODUCT_ID_REMOTE_ADS)
            AppPurchase.instance.discount = 1.5
        }
        AppOpenManager.instance?.disableAppResumeWithActivity(AdActivity::class.java)
//        AppOpenManager.instance?.disableAppResumeWithActivity(SplashActivity::class.java)
        Admod.instance?.setOpenActivityAfterShowInterAds(false)
        AppPurchase.instance.initBilling(this,
            PurchaseConstants.listINAPId,
            PurchaseConstants.listSubsId)
        if (Build.VERSION.SDK_INT > 29)
            Admod.instance?.setOpenActivityAfterShowInterAds(true)
        else
            Admod.instance?.setOpenActivityAfterShowInterAds(false)

        if (AppPreferences().completedTheFirstTutorial == true) {
            AppOpenManager.instance?.setSplashActivity(SplashActivity::class.java,
                AdConfig.AD_ADMOB_OPEN_APP_SPLASH,
                10000)
        }

    }

    override fun enableAdsResume(): Boolean = true

    //    override val listTestDeviceId: List<String> = AdConfig.DEVICES
//    override val openAppAdId: String = AdConfig.AD_ADMOB_OPEN_APP
    override fun buildDebug(): Boolean = BuildConfig.DEBUG

    override val listTestDeviceId: List<String> = AdConfig.DEVICES
    override val openAppAdId: String = AdConfig.AD_ADMOB_OPEN_APP
    override val supportFan: Boolean = true
    override val supportApplovin: Boolean = true
}