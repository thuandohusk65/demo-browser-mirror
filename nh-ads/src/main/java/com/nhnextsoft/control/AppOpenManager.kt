package com.nhnextsoft.control

import android.annotation.SuppressLint
import android.app.*
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.nhnextsoft.control.analytic.FirebaseAnalyticsUtil.logPaidAdImpression
import com.nhnextsoft.control.billing.AppPurchase
import com.nhnextsoft.control.dialog.PrepareLoadingAdsDialog

import timber.log.Timber
import java.util.*

class AppOpenManager private constructor() : ActivityLifecycleCallbacks, LifecycleObserver {
    private var appResumeAd: AppOpenAd? = null
    private var splashAd: AppOpenAd? = null
    private var isUsingAdsOpenAppInSplash: Boolean = true
    private var loadCallback: AppOpenAdLoadCallback? = null
    private var fullScreenContentCallback: FullScreenContentCallback? = null
    private var appResumeAdId: String? = null
    private var splashAdId: String? = null
    private var currentActivity: Activity? = null
    private var myApplication: Application? = null
    private var appResumeLoadTime: Long = 0
    private var splashLoadTime: Long = 0
    private var splashTimeout = 0
    var isInitialized = false // bật  - tắt ad resume  trong app
    private var isAppResumeEnabled = true
    private val disabledAppOpenList: MutableList<Class<*>>
    private var splashActivity: Class<*>? = null
    private var isTimeout = false
    private val timeoutHandler = Handler(Looper.getMainLooper()) { msg: Message ->
        if (msg.what == TIMEOUT_MSG) {
            isTimeout = true
        }
        false
    }

    /**
     * Init AppOpenManager
     *
     * @param application
     */
    fun init(application: Application?, appOpenAdId: String?) {
        isInitialized = true
        myApplication = application
        myApplication?.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        appResumeAdId = appOpenAdId
        if (!AppPurchase.instance.isPurchased(application?.applicationContext) &&
            !isAdAvailable(false) && appOpenAdId != null
        ) {
            fetchAd(false)
        }
    }

    /**
     * Check app open ads is showing
     *
     * @return
     */
    val isShowingAd: Boolean
        get() = Companion.isShowingAd

    /**
     * Disable app open app on specific activity
     *
     * @param activityClass
     */
    fun disableAppResumeWithActivity(activityClass: Class<*>) {
        Timber.d("disableAppResumeWithActivity: " + activityClass.name)
        disabledAppOpenList.add(activityClass)
    }

    fun enableAppResumeWithActivity(activityClass: Class<*>) {
        Timber.d("enableAppResumeWithActivity: " + activityClass.name)
        disabledAppOpenList.remove(activityClass)
    }

    fun disableAppResume() {
        isAppResumeEnabled = false
    }

    fun enableAppResume() {
        isAppResumeEnabled = true
    }

    fun setSplashActivity(splashActivity: Class<*>?, adId: String?, timeoutInMillis: Int) {
        this.splashActivity = splashActivity
        splashAdId = adId
        splashTimeout = timeoutInMillis
    }

    fun setAppResumeAdId(appResumeAdId: String?) {
        this.appResumeAdId = appResumeAdId
    }

    fun setFullScreenContentCallback(callback: FullScreenContentCallback?) {
        fullScreenContentCallback = callback
    }

    fun removeFullScreenContentCallback() {
        fullScreenContentCallback = null
    }

    /**
     * Request an ad
     */
    fun fetchAd(isSplash: Boolean) {
        Timber.d("fetchAd: isSplash = $isSplash")
        if (isAdAvailable(isSplash)) {
            return
        }
        loadCallback = object : AppOpenAdLoadCallback() {
            /**
             * Called when an app open ad has loaded.
             *
             * @param ad the loaded app open ad.
             */
            override fun onAdLoaded(ad: AppOpenAd) {
                Timber.d("onAppOpenAdLoaded: isSplash = $isSplash")
                if (!isSplash) {
                    appResumeAd = ad
                    appResumeAd?.onPaidEventListener = OnPaidEventListener { adValue: AdValue? ->
//                                AdjustDebug.pushTrackEventAdmod(adValue);
                        logPaidAdImpression(
                            adValue!!,
                            ad.adUnitId,
                            ad.responseInfo
                                .mediationAdapterClassName)
                    }
                    appResumeLoadTime = Date().time
                } else {
                    splashAd = ad
                    splashAd?.onPaidEventListener = OnPaidEventListener { adValue: AdValue? ->
                        logPaidAdImpression(
                            adValue!!,
                            ad.adUnitId,
                            ad.responseInfo
                                .mediationAdapterClassName)
                    }
                    splashLoadTime = Date().time
                }
            }

            /**
             * Called when an app open ad has failed to load.
             *
             * @param loadAdError the error.
             */
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Timber.d("onAppOpenAdFailedToLoad: isSplash" + isSplash + " message " + loadAdError.message)
            }
        }
        if (currentActivity != null) {
            if (currentActivity?.resources?.getStringArray(R.array.list_id_test)?.toMutableList()
                    ?.contains(if (isSplash) splashAdId.toString() else appResumeAdId.toString()) == true
            ) {
                showTestAlert(currentActivity!!,
                    isSplash,
                    if (isSplash) splashAdId else appResumeAdId)
            }
        }
        val request = adRequest
        AppOpenAd.load(
            myApplication, if (isSplash) splashAdId else appResumeAdId, request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback)
    }

    private fun showTestAlert(context: Context, isSplash: Boolean, id: String?) {
        val notification = NotificationCompat.Builder(context, "warning_ads")
            .setContentTitle("Found test ad id")
            .setContentText(if (isSplash) "Splash Ads: " else "AppResume Ads: $id")
            .setSmallIcon(R.drawable.ic_warning)
            .build()
        val notificationManager = NotificationManagerCompat.from(context)
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("warning_ads",
                "Warning Ads",
                NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(if (isSplash) Admod.SPLASH_ADS else Admod.RESUME_ADS,
            notification)
        //        if (!BuildConfig.DEBUG){
//            throw new RuntimeException("Found test ad id on release");
//        }
    }

    /**
     * Creates and returns ad request.
     */
    private val adRequest: AdRequest
        private get() = AdRequest.Builder().build()

    private fun wasLoadTimeLessThanNHoursAgo(loadTime: Long, numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    /**
     * Utility method that checks if ad exists and can be shown.
     */
    fun isAdAvailable(isSplash: Boolean): Boolean {
        val loadTime = if (isSplash) splashLoadTime else appResumeLoadTime
        val wasLoadTimeLessThanNHoursAgo = wasLoadTimeLessThanNHoursAgo(loadTime, 4)
        Timber.d("isAdAvailable: $wasLoadTimeLessThanNHoursAgo")
        return ((if (isSplash) splashAd != null else appResumeAd != null)
                && wasLoadTimeLessThanNHoursAgo)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
        Timber.d("onActivityResumed: ")
        if (splashActivity == null) {
            if (activity.javaClass.name != AdActivity::class.java.name) {
                Timber.d("onActivityResumed 1: with " + activity.javaClass.name)
                fetchAd(false)
            }
        } else {
            if (activity.javaClass.name != splashActivity?.name && activity.javaClass.name != AdActivity::class.java.name) {
                Timber.d("onActivityResumed 2: with " + activity.javaClass.name)
                fetchAd(false)
            }
        }
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
    }

    fun showAdIfAvailable(isSplash: Boolean) {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        if (currentActivity != null && AppPurchase.instance.isPurchased(currentActivity)) {
            if (fullScreenContentCallback != null) {
                fullScreenContentCallback?.onAdDismissedFullScreenContent()
            }
            return
        }
        Timber.d("showAdIfAvailable: " + ProcessLifecycleOwner.get().lifecycle.currentState)
        if (!ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            Timber.d("showAdIfAvailable: return")
            if (fullScreenContentCallback != null) {
                fullScreenContentCallback?.onAdDismissedFullScreenContent()
            }
            return
        }
        if (!Companion.isShowingAd && isAdAvailable(isSplash)) {
            Timber.d("Will show ad.")
            val callback: FullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    appResumeAd = null
                    if (fullScreenContentCallback != null) {
                        fullScreenContentCallback?.onAdDismissedFullScreenContent()
                    }
                    Companion.isShowingAd = false
                    fetchAd(isSplash)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    if (fullScreenContentCallback != null) {
                        fullScreenContentCallback?.onAdFailedToShowFullScreenContent(adError)
                    }
                }

                override fun onAdShowedFullScreenContent() {
                    Timber.d("onAdShowedFullScreenContent: isSplash = $isSplash")
                    Companion.isShowingAd = true
                    if (isSplash) {
                        splashAd = null
                    } else {
                        appResumeAd = null
                    }
                }
            }
            //            if (isSplash) {
//                splashAd.show(currentActivity, callback);
//            } else {
//                appResumeAd.show(currentActivity, callback);
//            }
            showAdsWithLoading(isSplash, callback)
        } else {
            Timber.d("Ad is not ready")
            if (!isSplash) {
                fetchAd(false)
            }
        }
    }

    private fun showAdsWithLoading(isSplash: Boolean, callback: FullScreenContentCallback) {
        if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            var dialog: Dialog? = null
            try {
                dialog = currentActivity?.let { PrepareLoadingAdsDialog(it) }
                try {
                    dialog?.show()
                } catch (e: Exception) {
                    fullScreenContentCallback?.onAdDismissedFullScreenContent()
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val finalDialog = dialog
            Handler(Looper.getMainLooper()).postDelayed({
                if (isSplash) {
                    splashAd?.fullScreenContentCallback = callback
                    splashAd?.show(currentActivity)
                } else {
                    if (appResumeAd != null) {
                        appResumeAd?.fullScreenContentCallback = callback
                        appResumeAd?.show(currentActivity)
                    }
                }
                if (currentActivity != null && currentActivity?.isDestroyed == false && finalDialog != null) {
                    finalDialog.dismiss()
                }
            }, 800)
        }
    }

    fun loadAndShowSplashAds(adId: String?) {
        isTimeout = false
        if (currentActivity != null && AppPurchase.instance.isPurchased(currentActivity)) {
            if (fullScreenContentCallback != null) {
                fullScreenContentCallback?.onAdDismissedFullScreenContent()
            }
            return
        }
        if (isAdAvailable(true)) {
            showAdIfAvailable(true)
            return
        }
        loadCallback = object : AppOpenAdLoadCallback() {
            /**
             * Called when an app open ad has loaded.
             *
             * @param appOpenAd the loaded app open ad.
             */
            override fun onAdLoaded(appOpenAd: AppOpenAd) {
                Timber.d("onAppOpenAdLoaded: splash")
                splashAd = appOpenAd
                splashLoadTime = Date().time
                appOpenAd.onPaidEventListener = OnPaidEventListener { adValue: AdValue? ->
                    logPaidAdImpression(
                        adValue!!,
                        appOpenAd.adUnitId,
                        appOpenAd.responseInfo
                            .mediationAdapterClassName)
                }
                if (isTimeout) {
                    Timber.e("onAppOpenAdLoaded: splash timeout")
                    if (fullScreenContentCallback != null) {
                        fullScreenContentCallback?.onAdDismissedFullScreenContent()
                    }
                    return
                }
                showAdIfAvailable(true)
            }

            /**
             * Called when an app open ad has failed to load.
             *
             * @param loadAdError the error.
             */
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Timber.e("onAppOpenAdFailedToLoad: splash " + loadAdError.message)
                if (fullScreenContentCallback != null) {
                    fullScreenContentCallback?.onAdDismissedFullScreenContent()
                }
            }
        }

        val request = adRequest
        AppOpenAd.load(
            myApplication, splashAdId, request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback)
        if (splashTimeout > 0) {
            timeoutHandler.sendEmptyMessageDelayed(TIMEOUT_MSG, splashTimeout.toLong())
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onResume() {
        if (!isAppResumeEnabled) {
            Timber.d("onResume: app resume is disabled")
            return
        }
        for (activity in disabledAppOpenList) {
            if (activity.name == currentActivity?.javaClass?.name) {
                Timber.d("onStart: activity is disabled")
                return
            }
        }
        if (splashActivity != null && splashActivity?.name == currentActivity?.javaClass?.name) {
            val adId = splashAdId
            if (adId == null) {
                Timber.e("splash ad id must not be null")
            }
            Timber.d("onStart: load and show splash ads")
            loadAndShowSplashAds(adId)
            return
        }
        Timber.d("onStart: show resume ads")
        showAdIfAvailable(false)
    }

    companion object {
        private const val TAG = "AppOpenManager"
        const val AD_UNIT_ID_TEST = "ca-app-pub-3940256099942544/3419835294"

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: AppOpenManager? = null
        private var isShowingAd = false
        private const val TIMEOUT_MSG = 11

        @JvmStatic
        @get:Synchronized
        val instance: AppOpenManager?
            get() {
                if (INSTANCE == null) {
                    INSTANCE = AppOpenManager()
                }
                return INSTANCE
            }
    }

    /**
     * Constructor
     */
    init {
        disabledAppOpenList = ArrayList()
    }
}