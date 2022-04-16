package com.nhnextsoft.control

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.os.*
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.applovin.mediation.AppLovinExtras
import com.applovin.mediation.ApplovinAdapter
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.ads.mediation.facebook.FacebookAdapter
import com.google.ads.mediation.facebook.FacebookExtras
import com.google.ads.mediation.inmobi.InMobiAdapter
import com.google.ads.mediation.inmobi.InMobiNetworkKeys
import com.google.ads.mediation.inmobi.InMobiNetworkValues
import com.google.ads.mediation.unity.UnityAdapter
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.nhnextsoft.control.analytic.FirebaseAnalyticsUtil.logPaidAdImpression
import com.nhnextsoft.control.application.AppGlobal
import com.nhnextsoft.control.billing.AppPurchase
import com.nhnextsoft.control.dialog.PrepareLoadingAdsDialog
import com.nhnextsoft.control.funtion.AdCallback
import com.nhnextsoft.control.funtion.AdmodHelper.getNumClickAdsPerDay
import com.nhnextsoft.control.funtion.AdmodHelper.setupAdmodData
import com.nhnextsoft.control.funtion.RewardCallback
import timber.log.Timber
import java.util.*

class Admod private constructor() {
    private var currentClicked = 0
    private var nativeId: String? = null
    private var numShowAds = 3
    private var maxClickAds = 100
    private var handlerTimeout: Handler? = null
    private var rdTimeout: Runnable? = null
    private var dialog: PrepareLoadingAdsDialog? = null
    private var isTimeout // xử lý timeout show ads
            = false
    private var isShowLoadingSplash =
        false //kiểm tra trạng thái ad splash, ko cho load, show khi đang show loading ads splash
    private var isFan = false

    //    private var isAdcolony = false
    private var isAppLovin = false
    private var isPangle = false
    private var isInMobi = false
    private var isUnity = false
    var isTimeDelay = false //xử lý delay time show ads, = true mới show ads
    private var openActivityAfterShowInterAds = false

    private var context: Context? = null
    private var mActivity: Activity? = null

    //    private AppOpenAd appOpenAd = null;
    //    private static final String SHARED_PREFERENCE_NAME = "ads_shared_preference";
    //    private final Map<String, AppOpenAd> appOpenAdMap = new HashMap<>();
    var mInterstitialSplash: InterstitialAd? = null
    var interstitialAd: InterstitialAd? = null

    fun setFan(fan: Boolean) {
        isFan = fan
    }

//    fun setColony(adcolony: Boolean) {
//        isAdcolony = adcolony
//    }

    fun setAppLovin(appLovin: Boolean) {
        isAppLovin = appLovin
    }

    fun setPangle(pangle: Boolean) {
        isPangle = pangle
    }

    fun setInMobi(inMobi: Boolean) {
        isInMobi = inMobi
    }

    fun setUnityAds(unity: Boolean) {
        isUnity = unity
    }

    /**
     * Giới hạn số lần click trên 1 admod tren 1 ngay
     *
     * @param maxClickAds
     */
    fun setMaxClickAdsPerDay(maxClickAds: Int) {
        this.maxClickAds = maxClickAds
    }

    fun setNumToShowAds(numShowAds: Int) {
        this.numShowAds = numShowAds
    }

    fun setNumToShowAds(numShowAds: Int, currentClicked: Int) {
        this.numShowAds = numShowAds
        this.currentClicked = currentClicked
    }

    /**
     * khởi tạo admod
     *
     * @param context
     */
    fun init(context: Context, testDeviceList: List<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = Application.getProcessName()
            val packageName = context.packageName
            if (packageName != processName) {
                WebView.setDataDirectorySuffix(processName)
            }
        }
        MobileAds.initialize(context) { initializationStatus: InitializationStatus? ->
            Timber.i("$initializationStatus")
        }
        if (BuildConfig.DEBUG) {
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder()
                    .setTestDeviceIds(testDeviceList)
                    .build()
            )
        }
        this.context = context
    }

    private fun getProcessName(context: Context?): String? {
        if (context == null) return null
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (processInfo in manager.runningAppProcesses) {
            if (processInfo.pid == Process.myPid()) {
                return processInfo.processName
            }
        }
        return null
    }

    fun setOpenActivityAfterShowInterAds(openActivityAfterShowInterAds: Boolean) {
        this.openActivityAfterShowInterAds = openActivityAfterShowInterAds
    }

    //        builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
    val adRequest: AdRequest
        get() {
            val builder = AdRequest.Builder()
            if (isFan) {
                val extras = FacebookExtras()
                    .setNativeBanner(true)
                    .build()
                builder.addNetworkExtrasBundle(FacebookAdapter::class.java, extras)
            }
//            if (isAdcolony) {
//                AdColonyBundleBuilder.setShowPrePopup(true)
//                AdColonyBundleBuilder.setShowPostPopup(true)
//                builder.addNetworkExtrasBundle(AdColonyAdapter::class.java,
//                    AdColonyBundleBuilder.build())
//            }
            if (isAppLovin) {
                val extras = AppLovinExtras.Builder()
                    .setMuteAudio(true)
                    .build()
                builder.addNetworkExtrasBundle(ApplovinAdapter::class.java, extras)
            }
//            if (isPangle) {
//                val extrasPangle = Bundle()
//                builder.addCustomEventExtrasBundle(AdmobNativeFeedAdAdapter::class.java, extrasPangle)
//            }

            if (isInMobi) {
                Timber.d("add builder inMobi")
                val extrasInMobi = Bundle()
                builder.addNetworkExtrasBundle(InMobiAdapter::class.java, extrasInMobi)
                    .build()
            }

            if (isUnity) {
                Timber.d("add builder Unity")
                val extrasUnity = Bundle()
                builder.addNetworkExtrasBundle(UnityAdapter::class.java, extrasUnity)
                    .build()
            }
            //        builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
            return builder.build()
        }


    fun interstitialSplashLoaded(): Boolean {
        return mInterstitialSplash != null
    }

    fun getInterstitialSplash(): InterstitialAd? {
        return mInterstitialSplash
    }

    /**
     * Load quảng cáo Full tại màn SplashActivity
     * Sau khoảng thời gian timeout thì load ads và callback về cho View
     *
     * @param context
     * @param id
     * @param timeOut    : thời gian chờ ads, timeout <= 0 tương đương với việc bỏ timeout
     * @param timeDelay  : thời gian chờ show ad từ lúc load ads
     * @param adListener
     */
    fun loadSplashInterstitial(
        activity: Activity,
        id: String,
        timeOut: Long,
        timeDelay: Long,
        adListener: AdCallback?,
    ) {
        mActivity = activity
        isTimeDelay = false
        isTimeout = false
        Timber.i("loadSplashInterstitalAds  start time loading:" + Calendar.getInstance().timeInMillis + "  ShowLoadingSplash:" + isShowLoadingSplash)
        if (AppPurchase.instance.isPurchased(activity)) {
            adListener?.onAdClosed()
            return
        }
        Handler(Looper.getMainLooper()).postDelayed(Runnable { //check delay show ad splash
            Timber.i("loadSplashInterstitalAds: timeDelay ${mInterstitialSplash != null}")
            if (mInterstitialSplash != null) {
                Timber.i("loadSplashInterstitalAds:show ad on delay ")
                onShowSplash(activity, adListener)
                return@Runnable
            }
            Timber.i("loadSplashInterstitalAds: delay validate")
            isTimeDelay = true
        }, timeDelay)
        if (timeOut > 0) {
            handlerTimeout = Handler(Looper.getMainLooper())
            rdTimeout = Runnable {
                Timber.d("loadSplashInterstitalAds: on timeout")
                isTimeout = true
                if (mInterstitialSplash != null) {
                    Timber.i("loadSplashInterstitalAds:show ad on timeout ")
                    onShowSplash(activity, adListener)
                    return@Runnable
                }
                if (adListener != null) {
                    adListener.onAdClosed()
                    isShowLoadingSplash = false
                }
            }
            handlerTimeout?.postDelayed(rdTimeout!!, timeOut)
        }

//        if (isShowLoadingSplash)
//            return;
        Timber.d("loadSplashInterstitalAds")
        isShowLoadingSplash = true
        getInterstitalAds(activity, id, object : AdCallback() {
            override fun onInterstitialLoad(interstitialAd: InterstitialAd?) {
                super.onInterstitialLoad(interstitialAd)
                Timber.d("loadSplashInterstitalAds $id  end time loading success:" + Calendar.getInstance().timeInMillis + "     time limit:" + isTimeout)
//                if (isTimeout) return
                Timber.d("loadSplashInterstitalAds ${(interstitialAd != null)} isTimeDelay $isTimeDelay")
                if (interstitialAd != null) {
                    mInterstitialSplash = interstitialAd
                    if (isTimeDelay) {
                        Timber.d("loadSplashInterstitalAds to show")
                        onShowSplash(activity, adListener)
                        Timber.i("loadSplashInterstitalAds:show ad on loaded ")
                    }
                }
            }

            override fun onAdFailedToLoad(i: LoadAdError?) {
                super.onAdFailedToLoad(i)
                Timber.e("loadSplashInterstitalAds $id end time loading error:" + Calendar.getInstance().timeInMillis + "     time limit:" + isTimeout)
                if (isTimeout) return
                if (adListener != null) {
                    if (handlerTimeout != null && rdTimeout != null) {
                        handlerTimeout?.removeCallbacks(rdTimeout!!)
                    }
                    if (i != null) Timber.e("loadSplashInterstitalAds: load fail " + i.message)
                    adListener.onAdFailedToLoad(i)
                }
            }
        })
    }

    private fun onShowSplash(activity: Activity, adListener: AdCallback?) {
        Timber.d("onShowSplash ${(mInterstitialSplash != null)}")
        isShowLoadingSplash = true
        if (mInterstitialSplash != null) {
            mInterstitialSplash?.onPaidEventListener = OnPaidEventListener { adValue: AdValue ->
                Timber.d("OnPaidEvent splash:" + adValue.valueMicros)
                //                AdjustDebug.pushTrackEventAdmod(adValue);
                logPaidAdImpression(
                    adValue,
                    mInterstitialSplash?.adUnitId,
                    mInterstitialSplash?.responseInfo
                        ?.mediationAdapterClassName
                )
            }
            Timber.d("onShowSplash onPaidEventListener")
        }
        if (handlerTimeout != null && rdTimeout != null) {
            handlerTimeout?.removeCallbacks(rdTimeout!!)
        }
        adListener?.onAdLoaded()
        mInterstitialSplash?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                isShowLoadingSplash = false
            }

            override fun onAdDismissedFullScreenContent() {
                if (AppOpenManager.instance?.isInitialized == true) {
                    AppOpenManager.instance?.enableAppResume()
                }
                if (adListener != null) {
                    if (!openActivityAfterShowInterAds) {
                        adListener.onAdClosed()
                    }
                    if (dialog != null) {
                        dialog?.dismiss()
                    }
                }
                mInterstitialSplash = null
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                mInterstitialSplash = null
                isShowLoadingSplash = false
                if (adListener != null) {
                    if (!openActivityAfterShowInterAds) {
                        adListener.onAdFailedToShow(adError)
                    }
                    if (dialog != null) {
                        dialog?.dismiss()
                    }
                }
            }
        }

        Timber.d("onShowSplash fullScreenContentCallback")
        if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            Timber.d("onShowSplash lifecycle currentState ")
            try {
                if (dialog != null && dialog?.isShowing == true) dialog?.dismiss()
                Timber.d("onShowSplash PrepareLoadingAdsDialog ")
                dialog = PrepareLoadingAdsDialog(activity)
                try {
                    dialog?.show()
                } catch (e: Exception) {
                    adListener?.onAdClosed()
                    Timber.d("onShowSplash Exception dialog " + e.message)
                    return
                }
            } catch (e: Exception) {
                dialog = null
                e.printStackTrace()
                Timber.d("onShowSplash Exception " + e.message)
            }
            Handler(Looper.getMainLooper()).postDelayed({
                Timber.d("onShowSplash postDelayed show")
                if (AppOpenManager.instance?.isInitialized == true) {
                    AppOpenManager.instance?.disableAppResume()
                }
                if (openActivityAfterShowInterAds && adListener != null) {
                    adListener.onAdClosed()
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (dialog != null && dialog?.isShowing == true && !activity.isDestroyed) dialog?.dismiss()
                    }, 1500)
                }
                Timber.d("onShowSplash mInterstitialSplash?.show")
                mInterstitialSplash?.show(activity)
                isShowLoadingSplash = false
            }, 800)
        }
    }

    /**
     * @param context
     * @param id
     * @param timeOut
     * @param adListener
     */
    fun loadInterstitialAds(
        activity: Activity,
        id: String,
        timeOut: Long,
        adListener: AdCallback?,
    ) {
        isTimeout = false
        if (AppPurchase.instance.isPurchased(activity)) {
            adListener?.onAdClosed()
            return
        }
        interstitialAd = null
        getInterstitalAds(activity, id, object : AdCallback() {
            override fun onInterstitialLoad(interstitialAd: InterstitialAd?) {
                interstitialAd.also { this@Admod.interstitialAd = it }
                if (interstitialAd == null) {
                    adListener?.onAdFailedToLoad(null)
                    return
                }
                if (handlerTimeout != null && rdTimeout != null) {
                    handlerTimeout?.removeCallbacks(rdTimeout!!)
                }
                if (isTimeout) {
                    return
                }
                if (adListener != null) {
                    if (handlerTimeout != null && rdTimeout != null) {
                        handlerTimeout?.removeCallbacks(rdTimeout!!)
                    }
                    adListener.onInterstitialLoad(interstitialAd)
                }
                interstitialAd.onPaidEventListener = OnPaidEventListener { adValue: AdValue ->
                    Timber.d("OnPaidEvent loadInterstitialAds:%s", adValue.valueMicros)
                    logPaidAdImpression(
                        adValue,
                        interstitialAd.adUnitId,
                        interstitialAd.responseInfo
                            .mediationAdapterClassName
                    )
                }
            }

            override fun onAdFailedToLoad(i: LoadAdError?) {
                if (adListener != null) {
                    if (handlerTimeout != null && rdTimeout != null) {
                        handlerTimeout?.removeCallbacks(rdTimeout!!)
                    }
                    adListener.onAdFailedToLoad(i)
                }
            }
        })
        if (timeOut > 0) {
            handlerTimeout = Handler(Looper.getMainLooper())
            rdTimeout = Runnable {
                isTimeout = true
                if (interstitialAd != null) {
                    adListener?.onInterstitialLoad(interstitialAd)
                    return@Runnable
                }
                adListener?.onAdClosed()
            }
            handlerTimeout?.postDelayed(rdTimeout!!, timeOut)
        }
    }

    /**
     * Trả về 1 InterstitialAd và request Ads
     *
     * @param context
     * @param id
     * @return
     */
    fun getInterstitalAds(activity: Activity, id: String, adCallback: AdCallback) {
//        if (mutableListOf(*context.resources.getStringArray(R.array.list_id_test)).contains(id)) {
//            showTestIdAlert(context, INTERS_ADS, id)
//        }
        if (AppPurchase.instance.isPurchased(activity) || getNumClickAdsPerDay(
                activity,
                id
            ) >= maxClickAds
        ) {
            Timber.i(
                "isPurchased ${AppPurchase.instance.isPurchased(activity)} getNumClickAdsPerDay ${
                    getNumClickAdsPerDay(
                        activity,
                        id
                    )
                }"
            )
            adCallback.onInterstitialLoad(null)
        }
        Timber.i("start loading $id")
        InterstitialAd.load(activity, id, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Timber.i("onAdLoaded $id")
                    adCallback.onInterstitialLoad(interstitialAd)

                    //tracking adjust
                    interstitialAd.onPaidEventListener = OnPaidEventListener { adValue: AdValue ->
                        Timber.d("OnPaidEvent getInterstitalAds:" + adValue.valueMicros)
                        logPaidAdImpression(
                            adValue,
                            interstitialAd.adUnitId,
                            interstitialAd.responseInfo
                                .mediationAdapterClassName
                        )
                    }
                    Timber.i("onAdLoaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error
                    Timber.i("onAdFailedToLoad")
                    Timber.i(loadAdError.message)
                    adCallback.onAdFailedToLoad(loadAdError)
                }
            })
    }

    /**
     * Hiển thị ads  timeout
     * Sử dụng khi reopen app in splash
     *
     * @param context
     * @param mInterstitialAd
     * @param timeDelay
     */
    fun showInterstitialAdByTimes(
        activity: Activity,
        mInterstitialAd: InterstitialAd?,
        callback: AdCallback?,
        timeDelay: Long,
    ) {
        if (timeDelay > 0) {
            handlerTimeout = Handler(Looper.getMainLooper())
            rdTimeout =
                Runnable { forceShowInterstitial(activity, mInterstitialAd, callback, false) }
            handlerTimeout?.postDelayed(rdTimeout!!, timeDelay)
        } else {
            forceShowInterstitial(activity, mInterstitialAd, callback, false)
        }
    }

    /**
     * Hiển thị ads theo số lần được xác định trước và callback result
     * vd: click vào 3 lần thì show ads full.
     * AdmodHelper.setupAdmodData(context) -> kiểm tra xem app đc hoạt động đc 1 ngày chưa nếu YES thì reset lại số lần click vào ads
     *
     * @param context
     * @param mInterstitialAd
     * @param callback
     * @param shouldReloadAds
     */

    @JvmOverloads
    fun showInterstitialAdByTimes(
        activity: Activity,
        mInterstitialAd: InterstitialAd?,
        callback: AdCallback?,
        shouldReloadAds: Boolean = true,
    ) {
        setupAdmodData(activity)
        if (AppPurchase.instance.isPurchased(activity)) {
            callback?.onAdClosed()
            return
        }
        if (mInterstitialAd == null) {
            callback?.onAdClosed()
            return
        }
        mInterstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                // Called when fullscreen content is dismissed.
                if (AppOpenManager.instance?.isInitialized == true) {
                    AppOpenManager.instance?.enableAppResume()
                }
                if (callback != null) {
                    if (!openActivityAfterShowInterAds) {
                        callback.onAdClosed()
                    }
                    if (dialog != null) {
                        dialog?.dismiss()
                    }
                }
                Timber.e("onAdDismissedFullScreenContent")
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                super.onAdFailedToShowFullScreenContent(adError)
                Timber.e("onAdFailedToShowFullScreenContent: " + adError.message)
                // Called when fullscreen content failed to show.
                if (callback != null) {
                    if (!openActivityAfterShowInterAds) {
                        callback.onAdClosed()
                    }
                    if (dialog != null) {
                        dialog?.dismiss()
                    }
                }
            }

            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                // Called when fullscreen content is shown.
            }
        }
        if (getNumClickAdsPerDay(activity, mInterstitialAd.adUnitId) < maxClickAds) {
            showInterstitialAd(activity, mInterstitialAd, callback)
            return
        }
        callback?.onAdClosed()
    }

    /**
     * Bắt buộc hiển thị  ads full và callback result
     *
     * @param context
     * @param mInterstitialAd
     * @param callback
     * @param shouldReload
     */
    @JvmOverloads
    fun forceShowInterstitial(
        activity: Activity,
        mInterstitialAd: InterstitialAd?,
        callback: AdCallback?,
        shouldReload: Boolean = true,
    ) {
        currentClicked = numShowAds
        showInterstitialAdByTimes(activity, mInterstitialAd, callback, shouldReload)
    }

    /**
     * Kiểm tra và hiện thị ads
     *
     * @param context
     * @param mInterstitialAd
     * @param callback
     */
    private fun showInterstitialAd(
        activity: Activity,
        mInterstitialAd: InterstitialAd?,
        callback: AdCallback?,
    ) {
        currentClicked++
        if (currentClicked >= numShowAds && mInterstitialAd != null) {
            if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                try {
                    if (dialog != null && dialog?.isShowing == true) dialog?.dismiss()
                    Timber.d("showInterstitialAd PrepareLoadingAdsDialog ")
                    PrepareLoadingAdsDialog(activity).also { dialog = it }
                    try {
                        dialog?.show()
                    } catch (e: Exception) {
                        Timber.d(e)
                        callback?.onAdClosed()
                        return
                    }
                } catch (e: Exception) {
                    Timber.d(e)
                    dialog = null
                    e.printStackTrace()
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    if (AppOpenManager.instance?.isInitialized == true) {
                        AppOpenManager.instance?.disableAppResume()
                    }
                    if (openActivityAfterShowInterAds && callback != null) {
                        callback.onAdClosed()
                        Handler(Looper.getMainLooper()).postDelayed(
                            { if (dialog != null && dialog?.isShowing == true && !activity.isDestroyed) dialog?.dismiss() },
                            1500
                        )
                    }
                    mInterstitialAd.show(activity)
                }, 800)
            }
            currentClicked = 0
        } else if (callback != null) {
            if (dialog != null) {
                dialog?.dismiss()
            }
            callback.onAdClosed()
        }
    }

    /**
     * Load quảng cáo Banner Trong Activity set Inline adaptive banners
     *
     * @param mActivity
     * @param id
     */
    fun loadBanner(mActivity: Activity, id: String, useInlineAdaptive: Boolean?) {
        val adContainer = mActivity.findViewById<FrameLayout>(R.id.banner_container)
        val containerShimmer: ShimmerFrameLayout =
            mActivity.findViewById(R.id.shimmer_container_banner)
        loadBanner(mActivity, id, adContainer, containerShimmer, true)
    }

    /**
     * Load quảng cáo Banner Trong Activity
     *
     * @param mActivity
     * @param id
     */
    fun loadBanner(mActivity: Activity, id: String) {
        val adContainer = mActivity.findViewById<FrameLayout>(R.id.banner_container)
        val containerShimmer: ShimmerFrameLayout =
            mActivity.findViewById(R.id.shimmer_container_banner)
        loadBanner(mActivity, id, adContainer, containerShimmer, false)
    }

    /**
     * Load quảng cáo banner với adSize
     * @param mActivity
     * @param id
     */

    fun loadBannerWithAdSize(mActivity: Activity, id: String, adSize: AdSize) {
        val adContainer = mActivity.findViewById<FrameLayout>(R.id.banner_container)
        val containerShimmer: ShimmerFrameLayout =
            mActivity.findViewById(R.id.shimmer_container_banner)
        setBannerWithAdSize(mActivity, id, adContainer, containerShimmer, adSize)
    }

    private fun setBannerWithAdSize(
        mActivity: Activity,
        id: String,
        adContainer: FrameLayout,
        containerShimmer: ShimmerFrameLayout,
        adSize: AdSize,
    ) {
        if (listOf(*mActivity.resources.getStringArray(R.array.list_id_test)).contains(id)) {
            showTestIdAlert(mActivity, BANNER_ADS, id)
        }
        if (AppPurchase.instance?.isPurchased(mActivity)) {
            containerShimmer.visibility = View.GONE
            return
        }
        containerShimmer.visibility = View.VISIBLE
        containerShimmer.startShimmer()
        try {
            val adView = AdView(mActivity)
            adView.adUnitId = id
            adContainer.addView(adView)
            adView.adSize = adSize
            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            adView.loadAd(adRequest)
            adView.adListener = object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    containerShimmer.stopShimmer()
                    adContainer.visibility = View.GONE
                    containerShimmer.visibility = View.GONE
                }

                override fun onAdLoaded() {
                    Timber.d("Banner adapter class name: " + adView.responseInfo.mediationAdapterClassName)
                    containerShimmer.stopShimmer()
                    containerShimmer.visibility = View.GONE
                    adContainer.visibility = View.VISIBLE
                    if (adView != null) {
                        adView.onPaidEventListener = OnPaidEventListener { adValue: AdValue? ->
//                            AdjustDebug.pushTrackEventAdmod(adValue);
                            logPaidAdImpression(
                                adValue!!,
                                adView.adUnitId,
                                adView.responseInfo
                                    .mediationAdapterClassName
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.d(e)
            e.printStackTrace()
        }
    }

    /**
     * Load quảng cáo Banner Trong Activity
     *
     * @param mActivity
     * @param id
     */
    fun loadBanner(mActivity: Activity, id: String, callback: AdCallback?) {
        val adContainer = mActivity.findViewById<FrameLayout>(R.id.banner_container)
        val containerShimmer: ShimmerFrameLayout =
            mActivity.findViewById(R.id.shimmer_container_banner)
        loadBanner(mActivity, id, adContainer, containerShimmer, callback, false)
    }

    /**
     * Load quảng cáo Banner Trong Activity set Inline adaptive banners
     *
     * @param mActivity
     * @param id
     */
    fun loadBanner(
        mActivity: Activity,
        id: String,
        callback: AdCallback?,
        useInlineAdaptive: Boolean,
    ) {
        val adContainer = mActivity.findViewById<FrameLayout>(R.id.banner_container)
        val containerShimmer: ShimmerFrameLayout =
            mActivity.findViewById(R.id.shimmer_container_banner)
        loadBanner(mActivity, id, adContainer, containerShimmer, callback, useInlineAdaptive)
    }

    /**
     * Load Quảng Cáo Banner Trong Fragment
     *
     * @param mActivity
     * @param id
     * @param rootView
     */
    fun loadBannerFragment(mActivity: Activity, id: String, rootView: View) {
        val adContainer = rootView.findViewById<FrameLayout>(R.id.banner_container)
        val containerShimmer: ShimmerFrameLayout =
            rootView.findViewById(R.id.shimmer_container_banner)
        loadBanner(mActivity, id, adContainer, containerShimmer, false)
    }

    /**
     * Load Quảng Cáo Banner Trong Fragment set Inline adaptive banners
     *
     * @param mActivity
     * @param id
     * @param rootView
     */
    fun loadBannerFragment(
        mActivity: Activity,
        id: String,
        rootView: View,
        useInlineAdaptive: Boolean,
    ) {
        val adContainer = rootView.findViewById<FrameLayout>(R.id.banner_container)
        val containerShimmer: ShimmerFrameLayout =
            rootView.findViewById(R.id.shimmer_container_banner)
        loadBanner(mActivity, id, adContainer, containerShimmer, useInlineAdaptive)
    }

    /**
     * Load Quảng Cáo Banner Trong Fragment
     *
     * @param mActivity
     * @param id
     * @param rootView
     */
    fun loadBannerFragment(mActivity: Activity, id: String, rootView: View, callback: AdCallback?) {
        val adContainer = rootView.findViewById<FrameLayout>(R.id.banner_container)
        val containerShimmer: ShimmerFrameLayout =
            rootView.findViewById(R.id.shimmer_container_banner)
        loadBanner(mActivity, id, adContainer, containerShimmer, callback, false)
    }

    /**
     * Load Quảng Cáo Banner Trong Fragment set Inline adaptive banners
     *
     * @param mActivity
     * @param id
     * @param rootView
     * @param callback
     */
    fun loadBannerFragment(
        mActivity: Activity,
        id: String,
        rootView: View,
        callback: AdCallback?,
        useInlineAdaptive: Boolean,
    ) {
        val adContainer = rootView.findViewById<FrameLayout>(R.id.banner_container)
        val containerShimmer: ShimmerFrameLayout =
            rootView.findViewById(R.id.shimmer_container_banner)
        loadBanner(mActivity, id, adContainer, containerShimmer, callback, useInlineAdaptive)
    }

    var bannerLoaded = false
    private fun loadBanner(
        mActivity: Activity,
        id: String,
        adContainer: FrameLayout,
        containerShimmer: ShimmerFrameLayout,
        useInlineAdaptive: Boolean,
    ) {
        if (listOf(*mActivity.resources.getStringArray(R.array.list_id_test)).contains(id)) {
            showTestIdAlert(mActivity, BANNER_ADS, id)
        }
        if (AppPurchase.instance?.isPurchased(mActivity)) {
            containerShimmer.visibility = View.GONE
            return
        }
        containerShimmer.visibility = View.VISIBLE
        containerShimmer.startShimmer()
        try {
            val adView = AdView(mActivity)
            adView.adUnitId = id
            adContainer.addView(adView)
            val adSize = getAdSize(mActivity, useInlineAdaptive)
            adView.adSize = adSize
            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            adView.loadAd(adRequest)
            adView.adListener = object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    containerShimmer.stopShimmer()
                    adContainer.visibility = View.GONE
                    containerShimmer.visibility = View.GONE
                }

                override fun onAdLoaded() {
                    Timber.d("Banner adapter class name: " + adView.responseInfo.mediationAdapterClassName)
                    containerShimmer.stopShimmer()
                    containerShimmer.visibility = View.GONE
                    adContainer.visibility = View.VISIBLE
                    if (adView != null) {
                        adView.onPaidEventListener = OnPaidEventListener { adValue: AdValue? ->
//                            AdjustDebug.pushTrackEventAdmod(adValue);
                            logPaidAdImpression(
                                adValue!!,
                                adView.adUnitId,
                                adView.responseInfo
                                    .mediationAdapterClassName
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.d(e)
            e.printStackTrace()
        }
    }

    private fun loadBanner(
        mActivity: Activity,
        id: String,
        adContainer: FrameLayout,
        containerShimmer: ShimmerFrameLayout,
        callback: AdCallback?,
        useInlineAdaptive: Boolean,
    ) {
        if (Arrays.asList(*mActivity.resources.getStringArray(R.array.list_id_test)).contains(id)) {
            showTestIdAlert(mActivity, BANNER_ADS, id)
        }
        if (AppPurchase.instance?.isPurchased(mActivity)) {
            containerShimmer.visibility = View.GONE
            return
        }
        containerShimmer.visibility = View.VISIBLE
        containerShimmer.startShimmer()
        try {
            val adView = AdView(mActivity)
            adView.adUnitId = id
            adContainer.addView(adView)
            val adSize = getAdSize(mActivity, useInlineAdaptive)
            adView.adSize = adSize
            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            adView.loadAd(adRequest)
            adView.adListener = object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    containerShimmer.stopShimmer()
                    adContainer.visibility = View.GONE
                    containerShimmer.visibility = View.GONE
                }

                override fun onAdLoaded() {
                    Timber.d("Banner adapter class name: " + adView.responseInfo.mediationAdapterClassName)
                    containerShimmer.stopShimmer()
                    containerShimmer.visibility = View.GONE
                    adContainer.visibility = View.VISIBLE
                    if (adView != null) {
                        adView.onPaidEventListener = OnPaidEventListener { adValue: AdValue? ->
//                            AdjustDebug.pushTrackEventAdmod(adValue);
                            logPaidAdImpression(
                                adValue!!,
                                adView.adUnitId,
                                adView.responseInfo
                                    .mediationAdapterClassName
                            )
                        }
                    }
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    if (callback != null) {
                        callback.onAdClicked()
                        Timber.d("onAdClicked")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.d(e)
            e.printStackTrace()
        }
    }

    private fun getAdSize(mActivity: Activity, useInlineAdaptive: Boolean): AdSize {

        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        val display = mActivity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density
        val adWidth = (widthPixels / density).toInt()

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return if (useInlineAdaptive) {
            AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(
                mActivity,
                adWidth
            )
        } else AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
            mActivity,
            adWidth
        )
    }

    /**
     * load quảng cáo big native
     *
     * @param mActivity
     * @param id
     */
    fun loadNative(mActivity: Activity, id: String) {
        val frameLayout = mActivity.findViewById<FrameLayout>(R.id.fl_adplaceholder)
        val containerShimmer: ShimmerFrameLayout =
            mActivity.findViewById(R.id.shimmer_container_native)
        loadNative(mActivity, containerShimmer, frameLayout, id, R.layout.native_admob_ad)
    }

    fun loadNativeFragment(mActivity: Activity, id: String, parent: View) {
        val frameLayout = parent.findViewById<FrameLayout>(R.id.fl_adplaceholder)
        val containerShimmer: ShimmerFrameLayout =
            parent.findViewById(R.id.shimmer_container_native)
        loadNative(mActivity, containerShimmer, frameLayout, id, R.layout.native_admob_ad)
    }

    fun loadSmallNative(mActivity: Activity, adUnitId: String) {
        val frameLayout = mActivity.findViewById<FrameLayout>(R.id.fl_adplaceholder)
        val containerShimmer: ShimmerFrameLayout =
            mActivity.findViewById(R.id.shimmer_container_small_native)
        loadNative(
            mActivity,
            containerShimmer,
            frameLayout,
            adUnitId,
            R.layout.small_native_admod_ad
        )
    }

    fun loadSmallNativeFragment(mActivity: Activity, adUnitId: String, parent: View) {
        val frameLayout = parent.findViewById<FrameLayout>(R.id.fl_adplaceholder)
        val containerShimmer: ShimmerFrameLayout =
            parent.findViewById(R.id.shimmer_container_small_native)
        loadNative(
            mActivity,
            containerShimmer,
            frameLayout,
            adUnitId,
            R.layout.small_native_admod_ad
        )
    }

    fun loadNativeAd(context: Context, id: String, callback: AdCallback?) {
        if (Arrays.asList(*context.resources.getStringArray(R.array.list_id_test)).contains(id)) {
            showTestIdAlert(context, NATIVE_ADS, id)
        }
        if (AppPurchase.instance?.isPurchased(context)) {
            callback?.onAdClosed()
            return
        }
        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()
        val adLoader = AdLoader.Builder(context, id)
            .forNativeAd { nativeAd ->
                callback?.onNativeAdLoaded(nativeAd)
                nativeAd.setOnPaidEventListener { adValue: AdValue? ->
//                            AdjustDebug.pushTrackEventAdmod(adValue);
                    logPaidAdImpression(
                        adValue!!,
                        "",
                        "native"
                    )
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Timber.e("NativeAd onAdFailedToLoad: " + error.message)
                    callback?.onAdFailedToLoad(error)
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    if (callback != null) {
                        callback.onAdClicked()
                        Timber.d("onAdClicked")
                    }
                }
            })
            .withNativeAdOptions(adOptions)
            .build()
        adLoader.loadAd(adRequest)
    }

    private fun loadNative(
        context: Context,
        containerShimmer: ShimmerFrameLayout,
        frameLayout: FrameLayout,
        id: String,
        layout: Int,
    ) {
        if (Arrays.asList(*context.resources.getStringArray(R.array.list_id_test)).contains(id)) {
            showTestIdAlert(context, NATIVE_ADS, id)
        }
        if (AppPurchase.instance?.isPurchased(context)) {
            containerShimmer.visibility = View.GONE
            return
        }
        frameLayout.removeAllViews()
        frameLayout.visibility = View.GONE
        containerShimmer.visibility = View.VISIBLE
        containerShimmer.startShimmer()
        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()
        val adLoader = AdLoader.Builder(context, id)
            .forNativeAd { nativeAd ->
                containerShimmer.stopShimmer()
                containerShimmer.visibility = View.GONE
                frameLayout.visibility = View.VISIBLE
                @SuppressLint("InflateParams") val adView = LayoutInflater.from(context)
                    .inflate(layout, null) as NativeAdView
                nativeAd.setOnPaidEventListener { adValue: AdValue? ->
//                            AdjustDebug.pushTrackEventAdmod(adValue);
                    logPaidAdImpression(
                        adValue!!,
                        "",
                        "native"
                    )
                }
                populateUnifiedNativeAdView(context, nativeAd, adView)
                frameLayout.removeAllViews()
                frameLayout.addView(adView)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Timber.e("onAdFailedToLoad: " + error.message)
                    containerShimmer.stopShimmer()
                    containerShimmer.visibility = View.GONE
                    frameLayout.visibility = View.GONE
                }
            })
            .withNativeAdOptions(adOptions)
            .build()
        adLoader.loadAd(adRequest)
    }

    private fun loadNative(
        context: Context,
        containerShimmer: ShimmerFrameLayout,
        frameLayout: FrameLayout,
        id: String,
        layout: Int,
        callback: AdCallback?,
    ) {
        if (Arrays.asList(*context.resources.getStringArray(R.array.list_id_test)).contains(id)) {
            showTestIdAlert(context, NATIVE_ADS, id)
        }
        if (AppPurchase.instance?.isPurchased(context)) {
            containerShimmer.visibility = View.GONE
            return
        }
        frameLayout.removeAllViews()
        frameLayout.visibility = View.GONE
        containerShimmer.visibility = View.VISIBLE
        containerShimmer.startShimmer()
        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()
        val adLoader = AdLoader.Builder(context, id)
            .forNativeAd { nativeAd ->
                containerShimmer.stopShimmer()
                containerShimmer.visibility = View.GONE
                frameLayout.visibility = View.VISIBLE
                @SuppressLint("InflateParams") val adView = LayoutInflater.from(context)
                    .inflate(layout, null) as NativeAdView
                nativeAd.setOnPaidEventListener { adValue: AdValue? ->
//                            AdjustDebug.pushTrackEventAdmod(adValue);
                    logPaidAdImpression(
                        adValue!!,
                        "",
                        "native"
                    )
                }
                populateUnifiedNativeAdView(context, nativeAd, adView)
                frameLayout.removeAllViews()
                frameLayout.addView(adView)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Timber.e("onAdFailedToLoad: " + error.message)
                    containerShimmer.stopShimmer()
                    containerShimmer.visibility = View.GONE
                    frameLayout.visibility = View.GONE
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    if (callback != null) {
                        callback.onAdClicked()
                        Timber.d("onAdClicked")
                    }
                }
            })
            .withNativeAdOptions(adOptions)
            .build()
        adLoader.loadAd(adRequest)
    }

    fun populateUnifiedNativeAdView(context: Context, nativeAd: NativeAd, adView: NativeAdView) {
        adView.mediaView = adView.findViewById(R.id.ad_media)
        if (adView.mediaView != null) {
            adView.mediaView.postDelayed({
                if (BuildConfig.DEBUG) {
                    val sizeMin = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 120f,
                        context?.resources?.displayMetrics
                    )
                    Timber.e("Native sizeMin: $sizeMin")
                    Timber.e("Native w/h media : " + adView.mediaView.width + "/" + adView.mediaView.height)
                    if (adView.mediaView.width < sizeMin || adView.mediaView.height < sizeMin) {
                        Toast.makeText(context, "Size media native not valid", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }, 1000)
        }
        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        //        adView.setPriceView(adView.findViewById(R.id.ad_price));
//        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
//        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        // The headline is guaranteed to be in every UnifiedNativeAd.
        try {
            (adView.headlineView as TextView).text = nativeAd.headline
        } catch (e: Exception) {
            Timber.d(e)
            e.printStackTrace()
        }

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        try {
            if (nativeAd.body == null) {
                adView.bodyView.visibility = View.INVISIBLE
            } else {
                adView.bodyView.visibility = View.VISIBLE
                (adView.bodyView as TextView).text = nativeAd.body
            }
        } catch (e: Exception) {
            Timber.d(e)
            e.printStackTrace()
        }
        try {
            if (nativeAd.callToAction == null) {
                Objects.requireNonNull(adView.callToActionView).visibility = View.INVISIBLE
            } else {
                Objects.requireNonNull(adView.callToActionView).visibility = View.VISIBLE
                (adView.callToActionView as TextView).text = nativeAd.callToAction
            }
        } catch (e: Exception) {
            Timber.d(e)
            e.printStackTrace()
        }
        try {
            if (nativeAd.icon == null) {
                Objects.requireNonNull(adView.iconView).visibility = View.GONE
            } else {
                (adView.iconView as ImageView).setImageDrawable(
                    nativeAd.icon.drawable
                )
                adView.iconView.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Timber.d(e)
            e.printStackTrace()
        }
        try {
            if (nativeAd.price == null) {
                Objects.requireNonNull(adView.priceView).visibility = View.INVISIBLE
            } else {
                Objects.requireNonNull(adView.priceView).visibility = View.VISIBLE
                (adView.priceView as TextView).text = nativeAd.price
            }
        } catch (e: Exception) {
            Timber.d(e)
            e.printStackTrace()
        }
        try {
            if (nativeAd.store == null) {
                Objects.requireNonNull(adView.storeView).visibility = View.INVISIBLE
            } else {
                Objects.requireNonNull(adView.storeView).visibility = View.VISIBLE
                (adView.storeView as TextView).text = nativeAd.store
            }
        } catch (e: Exception) {
            Timber.d(e)
            e.printStackTrace()
        }
        try {
            if (nativeAd.starRating == null) {
                Objects.requireNonNull(adView.starRatingView).visibility = View.INVISIBLE
            } else {
                (Objects.requireNonNull(adView.starRatingView) as RatingBar).rating =
                    nativeAd.starRating.toFloat()
                adView.starRatingView.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Timber.d(e)
            e.printStackTrace()
        }
        try {
            if (nativeAd.advertiser == null) {
                adView.advertiserView.visibility = View.INVISIBLE
            } else {
                (adView.advertiserView as TextView).text = nativeAd.advertiser
                adView.advertiserView.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Timber.d(e)
            e.printStackTrace()
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad. The SDK will populate the adView's MediaView
        // with the media content from this native ad.
        adView.setNativeAd(nativeAd)
    }

    var rewardedInterstitialAd: RewardedInterstitialAd? = null
        private set

    var rewardedAd: RewardedAd? = null
        private set

    /**
     * Khởi tạo quảng cáo reward
     *
     * @param context
     * @param id
     */
    fun initRewardAds(context: Context, id: String?) {
        if (Arrays.asList(*context.resources.getStringArray(R.array.list_id_test)).contains(id)) {
            showTestIdAlert(context, REWARD_ADS, id)
        }
        if (AppPurchase.instance.isPurchased(context)) {
            return
        }
        nativeId = id
        if (AppPurchase.instance.isPurchased(context)) {
            return
        }
        RewardedAd.load(context, id, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(rewardedAd: RewardedAd) {
                this@Admod.rewardedAd = rewardedAd
                this@Admod.rewardedAd?.onPaidEventListener =
                    OnPaidEventListener { adValue: AdValue? ->
//                    AdjustDebug.pushTrackEventAdmod(adValue);
                        logPaidAdImpression(
                            adValue!!,
                            "",
                            "native"
                        )
                    }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                Timber.e("RewardedAd onAdFailedToLoad: " + loadAdError.message)
            }
        })
    }

    /**
     * Khởi tạo quảng cáo reward
     *
     * @param context
     * @param id
     */
    fun initRewardAds(context: Context, id: String, callback: AdCallback) {
        if (Arrays.asList(*context.resources.getStringArray(R.array.list_id_test)).contains(id)) {
            showTestIdAlert(context, REWARD_ADS, id)
        }
        if (AppPurchase.instance?.isPurchased(context)) {
            return
        }
        nativeId = id
        if (AppPurchase.instance.isPurchased(context)) {
            return
        }
        RewardedAd.load(context, id, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(rewardedAd: RewardedAd) {
                callback.onAdLoaded()
                this@Admod.rewardedAd = rewardedAd
                this@Admod.rewardedAd?.onPaidEventListener =
                    OnPaidEventListener { adValue: AdValue? ->
//                    AdjustDebug.pushTrackEventAdmod(adValue);
                        logPaidAdImpression(
                            adValue!!,
                            "",
                            "native"
                        )
                    }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                callback.onAdFailedToLoad(loadAdError)
                Timber.e("RewardedAd onAdFailedToLoad: " + loadAdError.message)
            }
        })
    }

    /**
     * Khởi tạo quảng cáo reward interstitial
     *
     * @param context
     * @param id
     */
    fun initRewardedInterstitialAds(context: Context, id: String, callback: AdCallback) {
        if (Arrays.asList(*context.resources.getStringArray(R.array.list_id_test)).contains(id)) {
            showTestIdAlert(context, REWARD_ADS, id)
        }
        if (AppPurchase.instance?.isPurchased(context)) {
            return
        }
        nativeId = id
        if (AppPurchase.instance.isPurchased(context)) {
            return
        }

        RewardedInterstitialAd.load(
            context,
            id,
            adRequest,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedInterstitialAd) {
                    callback.onAdLoaded()
                    this@Admod.rewardedInterstitialAd = rewardedAd
                    this@Admod.rewardedInterstitialAd?.onPaidEventListener =
                        OnPaidEventListener { adValue: AdValue? ->
                            logPaidAdImpression(
                                adValue!!,
                                "",
                                "native"
                            )
                        }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    callback.onAdFailedToLoad(loadAdError)
                    Timber.e("RewardedAd onAdFailedToLoad: " + loadAdError.message)
                }
            })
    }

    /**
     * Show quảng cáo reward và nhận kết quả trả về
     *
     * @param context
     * @param adCallback
     */
    fun showRewardAds(context: Activity, adCallback: RewardCallback?) {
        if (AppPurchase.instance?.isPurchased(context)) {
            adCallback?.onUserEarnedReward(null)
            return
        }
        if (rewardedAd == null) {
            initRewardAds(context, nativeId)
            adCallback?.onRewardedAdFailedToShow(0)
            return
        } else {
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    adCallback?.onRewardedAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    super.onAdFailedToShowFullScreenContent(adError)
                    adCallback?.onRewardedAdFailedToShow(adError.code)
                }
            }
            rewardedAd?.show(context) { rewardItem ->
                if (adCallback != null) {
                    adCallback.onUserEarnedReward(rewardItem)
                    initRewardAds(context, nativeId)
                }
            }
        }
    }

    /**
     * Show quảng cáo reward interstitial và nhận kết quả trả về
     *
     * @param context
     * @param adCallback
     */
    fun showRewardInterstitialAds(context: Activity, adCallback: RewardCallback?) {
        if (AppPurchase.instance?.isPurchased(context)) {
            adCallback?.onUserEarnedReward(null)
            return
        }
        if (rewardedInterstitialAd == null) {
            initRewardAds(context, nativeId)
            adCallback?.onRewardedAdFailedToShow(0)
            return
        } else {
            rewardedInterstitialAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        adCallback?.onRewardedAdClosed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        super.onAdFailedToShowFullScreenContent(adError)
                        adCallback?.onRewardedAdFailedToShow(adError.code)
                    }
                }
            rewardedInterstitialAd?.show(context) { rewardItem ->
                if (adCallback != null) {
                    adCallback.onUserEarnedReward(rewardItem)
                    initRewardAds(context, nativeId)
                }
            }
        }
    }

//    @SuppressLint("HardwareIds")
//    fun getDeviceId(activity: Activity): String {
//        val android_id = Settings.Secure.getString(activity.contentResolver,
//            Settings.Secure.ANDROID_ID)
//        return md5(android_id).toUpperCase()
//    }

//    private fun md5(s: String): String {
//        try {
//            // Create MD5 Hash
//            val digest = MessageDigest
//                .getInstance("MD5")
//            digest.update(s.toByteArray())
//            val messageDigest = digest.digest()
//
//            // Create Hex String
//            val hexString = StringBuffer()
//            for (i in messageDigest.indices) {
//                var h = Integer.toHexString(0xFF and messageDigest[i]
//                    .toInt())
//                while (h.length < 2) h = "0$h"
//                hexString.append(h)
//            }
//            return hexString.toString()
//        } catch (e: NoSuchAlgorithmException) {
//        }
//        return ""
//    }

    private fun showTestIdAlert(context: Context, typeAds: Int, id: String?) {
        var content: String? = ""
        when (typeAds) {
            BANNER_ADS -> content = "Banner Ads: "
            INTERS_ADS -> content = "Interstitial Ads: "
            REWARD_ADS -> content = "Rewarded Ads: "
            NATIVE_ADS -> content = "Native Ads: "
        }
        content += id
        val notification = NotificationCompat.Builder(context, "warning_ads")
            .setContentTitle("Found test ad id")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_warning)
            .build()
        val notificationManager = NotificationManagerCompat.from(context)
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "warning_ads",
                "Warning Ads",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(typeAds, notification)
        Timber.e("Found test ad id on debug : " + AppGlobal.BUILD_DEBUG)

//        if (!AppGlobal.BUILD_DEBUG) {
//            throw new RuntimeException("Found test ad id on release");
//        }
    }

    companion object {


        var instance: Admod? = null
            @JvmName("getInstance")
            get() {
                if (field == null) {
                    field = Admod()
                    field?.isShowLoadingSplash = false
                }
                return field
            }
            private set

        const val SPLASH_ADS = 0
        const val RESUME_ADS = 1
        private const val BANNER_ADS = 2
        private const val INTERS_ADS = 3
        private const val REWARD_ADS = 4
        private const val NATIVE_ADS = 5
    }
}