package com.nhnextsoft.control.application

import android.app.Application
import com.nhnextsoft.control.AdmodSP
import com.nhnextsoft.control.AppOpenManager
import com.nhnextsoft.control.FanManagerApp
import timber.log.Timber

abstract class SupportAdsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppGlobal.BUILD_DEBUG = buildDebug()
        Timber.i(" run debug: " + AppGlobal.BUILD_DEBUG)
        AdmodSP.instance?.init(this, listTestDeviceId)
        FanManagerApp.init(this, listTestDeviceId, AppGlobal.BUILD_DEBUG)
        if (enableAdsResume()) {
            AppOpenManager.instance?.init(this, openAppAdId)
        }
        //        if (enableAdjust()) {
//            setupIdEvent();
//            setupAdjust();
//        }
    }

    abstract fun enableAdsResume(): Boolean
    abstract val listTestDeviceId: List<String>
    abstract val openAppAdId: String
    abstract fun buildDebug(): Boolean


//    private void setupIdEvent() {
    //        AdjustDebug.enableAdjust = true;
    //    }
    //
    //    private void setupAdjust() {
    //
    //        String environment = buildDebug() ? AdjustConfig.ENVIRONMENT_SANDBOX : AdjustConfig.ENVIRONMENT_PRODUCTION;
    //        Log.i("Application", "setupAdjust: " + environment);
    //        AdjustConfig config = new AdjustConfig(this, getAdjustToken(), environment);
    //
    //        // Change the log level.
    //        config.setLogLevel(LogLevel.VERBOSE);
    //        config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
    //            @Override
    //            public void onAttributionChanged(AdjustAttribution attribution) {
    //                Log.d("AdjustDebug", "Attribution callback called!");
    //                Log.d("AdjustDebug", "Attribution: " + attribution.toString());
    //            }
    //        });
    //
    //        // Set event success tracking delegate.
    //        config.setOnEventTrackingSucceededListener(new OnEventTrackingSucceededListener() {
    //            @Override
    //            public void onFinishedEventTrackingSucceeded(AdjustEventSuccess eventSuccessResponseData) {
    //                Log.d("AdjustDebug", "Event success callback called!");
    //                Log.d("AdjustDebug", "Event success data: " + eventSuccessResponseData.toString());
    //            }
    //        });
    //        // Set event failure tracking delegate.
    //        config.setOnEventTrackingFailedListener(new OnEventTrackingFailedListener() {
    //            @Override
    //            public void onFinishedEventTrackingFailed(AdjustEventFailure eventFailureResponseData) {
    //                Log.d("AdjustDebug", "Event failure callback called!");
    //                Log.d("AdjustDebug", "Event failure data: " + eventFailureResponseData.toString());
    //            }
    //        });
    //
    //        // Set session success tracking delegate.
    //        config.setOnSessionTrackingSucceededListener(new OnSessionTrackingSucceededListener() {
    //            @Override
    //            public void onFinishedSessionTrackingSucceeded(AdjustSessionSuccess sessionSuccessResponseData) {
    //                Log.d("AdjustDebug", "Session success callback called!");
    //                Log.d("AdjustDebug", "Session success data: " + sessionSuccessResponseData.toString());
    //            }
    //        });
    //
    //        // Set session failure tracking delegate.
    //        config.setOnSessionTrackingFailedListener(new OnSessionTrackingFailedListener() {
    //            @Override
    //            public void onFinishedSessionTrackingFailed(AdjustSessionFailure sessionFailureResponseData) {
    //                Log.d("AdjustDebug", "Session failure callback called!");
    //                Log.d("AdjustDebug", "Session failure data: " + sessionFailureResponseData.toString());
    //            }
    //        });
    //        config.setSendInBackground(true);
    //        Adjust.onCreate(config);
    //        registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());
    //
    //    }
    //
    //
    //    public abstract boolean enableAdjust();
    //
    //
    //    public abstract String getAdjustToken();
    //
    //
    //    private static final class AdjustLifecycleCallbacks implements ActivityLifecycleCallbacks {
    //        @Override
    //        public void onActivityResumed(Activity activity) {
    //            Adjust.onResume();
    //        }
    //
    //        @Override
    //        public void onActivityPaused(Activity activity) {
    //            Adjust.onPause();
    //        }
    //
    //        @Override
    //        public void onActivityStopped(Activity activity) {
    //        }
    //
    //        @Override
    //        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    //        }
    //
    //        @Override
    //        public void onActivityDestroyed(Activity activity) {
    //        }
    //
    //        @Override
    //        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    //        }
    //
    //        @Override
    //        public void onActivityStarted(Activity activity) {
    //        }
    //    }
}