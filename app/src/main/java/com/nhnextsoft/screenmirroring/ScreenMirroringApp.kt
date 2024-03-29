package com.nhnextsoft.screenmirroring

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import com.nhnextsoft.control.Admod
import com.nhnextsoft.control.AppOpenManager
import com.nhnextsoft.control.application.SupportAdsApplication
import com.nhnextsoft.control.billing.AppPurchase
import com.nhnextsoft.screenmirroring.ads.AdConfig
import com.nhnextsoft.screenmirroring.ads.PurchaseConstants
import com.nhnextsoft.screenmirroring.ads.PurchaseConstants.PRODUCT_ID_REMOTE_ADS
import com.nhnextsoft.screenmirroring.config.AppConfigRemote
import com.nhnextsoft.screenmirroring.config.AppPreferences
import com.nhnextsoft.screenmirroring.config.Preferences
import com.nhnextsoft.screenmirroring.di.baseKoinModule
import com.nhnextsoft.screenmirroring.utility.extensions.ReleaseTree
import com.nhnextsoft.screenmirroring.view.activity.SplashActivity
import com.nhnextsoft.screenmirroring.view.activity.TutorialActivity
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
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
        initLogger()
        listenerMessagingFirebase()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@ScreenMirroringApp)
            modules(baseKoinModule)
        }
    }

    private fun initAds() {
        if (PRODUCT_ID_REMOTE_ADS.isNotEmpty()) {
            AppPurchase.instance.initBilling(this)
            AppPurchase.instance.setProductId(PRODUCT_ID_REMOTE_ADS)
            AppPurchase.instance.discount = 1.5
        }
        AppOpenManager.instance?.disableAppResumeWithActivity(AdActivity::class.java)
//
        Admod.instance?.setOpenActivityAfterShowInterAds(false)
        AppPurchase.instance.initBilling(
            this,
            PurchaseConstants.listINAPId,
            PurchaseConstants.listSubsId
        )
        if (Build.VERSION.SDK_INT > 29)
            Admod.instance?.setOpenActivityAfterShowInterAds(false)
        else
            Admod.instance?.setOpenActivityAfterShowInterAds(false)

        if (AppPreferences().completedTheFirstTutorial == true) {
            if (AppConfigRemote().isUsingAdsOpenApp == true) {
                AppOpenManager.instance?.setSplashActivity(
                    SplashActivity::class.java,
                    AdConfig.AD_ADMOB_OPEN_APP_SPLASH,
                    10000
                )
            } else {
                AppOpenManager.instance?.disableAppResumeWithActivity(SplashActivity::class.java)
                AppOpenManager.instance?.disableAppResumeWithActivity(TutorialActivity::class.java)
            }
        }

    }

    private fun initLogger() {
        if (BuildConfig.DEBUG) {
            XLog.init(LogLevel.ALL)
        } else {
            XLog.init(LogLevel.ERROR)
        }


    }

    @SuppressLint("StringFormatInvalid")
    private fun listenerMessagingFirebase() {
        Firebase.messaging.isAutoInitEnabled = true
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.d("Fetching FCM registration token failed ${task.exception}")
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
//            val msg = getString(R.string.app_name, token)
            Timber.d("TOKEN ==== $token")
//            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })
    }

    override fun enableAdsResume(): Boolean = true

    //    override val listTestDeviceId: List<String> = AdConfig.DEVICES
//    override val openAppAdId: String = AdConfig.AD_ADMOB_OPEN_APP
    override fun buildDebug(): Boolean = BuildConfig.DEBUG

    override val listTestDeviceId: List<String> = AdConfig.DEVICES
    override val openAppAdId: String = AdConfig.AD_ADMOB_OPEN_APP
    override val supportFan: Boolean = true
    override val supportApplovin: Boolean = true
    override val supportPangle: Boolean = true
    override val supportPangleAppId: String = "8034646"
    override val supportInMobi: Boolean = true
    override val supportInMobiAccountId: String = "3fd8aa9f482f42769d90c21158d45d47"
    override val supportUnity: Boolean = true
    override val supportUnityAppId: String = "4695456"
}