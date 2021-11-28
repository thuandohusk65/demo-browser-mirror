package com.ynsuper.screenmirroring.adsimport android.content.Contextimport android.util.Logimport androidx.appcompat.app.AlertDialogimport com.facebook.ads.Adimport com.facebook.ads.AdErrorimport com.facebook.ads.InterstitialAdListenerimport com.google.android.gms.ads.AdListenerimport com.google.android.gms.ads.AdRequestimport com.google.android.gms.ads.InterstitialAdimport com.ynsuper.screenmirroring.Rclass InterstitialLoader {    lateinit var mContext: Context    var googleId: String? = ""    var facebookId: String? = ""    private var mAdCloseListener: AdmobListener? = null    var progressDialog: AlertDialog? = null    private lateinit var mInterstitialAdFacebook: com.facebook.ads.InterstitialAd    private lateinit var mInterstitialAdGoogle: InterstitialAd    fun setAdsId(context: Context, googleAdsId: String, fanAdsId: String) {        mContext = context        googleId = googleAdsId        facebookId = fanAdsId        initUi()    }    private fun initUi() {        progressDialog = AlertDialog.Builder(mContext)                .setView(R.layout.dialog_loading_ads)                .setCancelable(false)                .create()    }    fun showProgress(){        if(progressDialog!=null && !progressDialog!!.isShowing){            progressDialog!!.show()        }    }    fun showInterstitial(adCloseListener: AdmobListener) {        mAdCloseListener = adCloseListener//        loadAdsInterstitialFacebook()        loadAdsInterstitialGoogle()//        progressDialog?.setOnCancelListener {//            mAdCloseListener?.onAdClosed()//        }    }    private fun loadAdsInterstitialFacebook() {        mInterstitialAdFacebook =                com.facebook.ads.InterstitialAd(mContext, facebookId)        val adListener = (object : InterstitialAdListener {            override fun onInterstitialDisplayed(p0: Ad?) {                Log.d("Ynsuper","onInterstitialDisplayed")            }            override fun onAdClicked(p0: Ad?) {                Log.d("Ynsuper","onAdClicked")            }            override fun onInterstitialDismissed(p0: Ad?) {                Log.d("Ynsuper","onInterstitialDismissed")                if (progressDialog != null) {                    progressDialog?.dismiss()                }                if (mAdCloseListener != null) {                    mAdCloseListener?.onAdClosed()                }            }            override fun onError(ad: Ad?, adError: AdError?) {                Log.d("Ynsuper","Interstitial ad failed to load: "+ adError?.errorMessage)                loadAdsInterstitialGoogle()            }            override fun onAdLoaded(p0: Ad?) {                Log.d("Ynsuper","onAdLoaded: FAN")                progressDialog?.dismiss()                if (mInterstitialAdFacebook.isAdLoaded && mInterstitialAdFacebook.isAdInvalidated.not()) {                    mInterstitialAdFacebook.show()                } else {                    mAdCloseListener?.onAdClosed()                }            }            override fun onLoggingImpression(p0: Ad?) {                Log.d("Ynsuper","onLoggingImpression")            }        })        val loadAdConfig = mInterstitialAdFacebook.buildLoadAdConfig()                .withAdListener(adListener)                .build()        mInterstitialAdFacebook.loadAd(loadAdConfig)    }    private fun loadAdsInterstitialGoogle() {        mInterstitialAdGoogle = InterstitialAd(mContext)        mInterstitialAdGoogle.adUnitId = googleId        val adRequestBuilder = AdRequest.Builder()        mInterstitialAdGoogle.adListener = object : AdListener() {            override fun onAdLoaded() {                mAdCloseListener?.onAdLoaded()                Log.d("Ynsuper","onAdLoaded admob")                if (mInterstitialAdGoogle.isLoaded) {                    mInterstitialAdGoogle.show()                } else {                    progressDialog?.dismiss()                    mAdCloseListener?.onAdClosed()                }            }            override fun onAdFailedToLoad(errorCode: Int) {                Log.d("Ynsuper","""onAdFailedToLoad admob:$errorCode""")                progressDialog?.dismiss()                mAdCloseListener?.onAdClosed()            }            override fun onAdOpened() {                Log.d("Ynsuper","onAdOpened")                // Code to be executed when the ad is displayed.            }            override fun onAdClicked() {                Log.d("Ynsuper","onAdClicked")                // Code to be executed when the user clicks on an ad.            }            override fun onAdLeftApplication() {                Log.d("Ynsuper","onAdLeftApplication")                mAdCloseListener?.onAdClosed()            }            override fun onAdClosed() {                Log.d("Ynsuper","onAdClosed")                progressDialog?.dismiss()                mAdCloseListener?.onAdClosed()            }        }        mInterstitialAdGoogle.loadAd(adRequestBuilder.build())    }    interface AdmobListener {        fun onAdLoaded()        fun onAdClosed()    }}