package com.nhnextsoft.control.dialog

import android.content.Context
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nhnextsoft.control.funtion.DialogExitListener

object DialogExit {
    fun showDialogExit(context: Context, nativeAd: NativeAd, type: Int, dialogExitListener: DialogExitListener) {
        val dialogExitAppExtend = DialogExitAppExtend(context, nativeAd, type)
        dialogExitAppExtend.setCancelable(false)
        val w = WindowManager.LayoutParams.MATCH_PARENT
        val h = ViewGroup.LayoutParams.MATCH_PARENT
        dialogExitAppExtend.window!!.setLayout(w, h)
        dialogExitAppExtend.dialogExitListener = dialogExitListener
        dialogExitAppExtend.show()
    }

//    fun showDialogExit(context: Context, nativeAd: NativeAd, type: Int, adView: NativeAdView?, dialogExitListener:DialogExitListener) {
//        val dialogExitAppExtend = DialogExitAppExtend(context, nativeAd, type, adView)
//        dialogExitAppExtend.setCancelable(false)
//        val w = WindowManager.LayoutParams.MATCH_PARENT
//        val h = ViewGroup.LayoutParams.MATCH_PARENT
//        dialogExitAppExtend.window!!.setLayout(w, h)
//        dialogExitAppExtend.dialogExitListener = dialogExitListener
//        dialogExitAppExtend.show()
//    }

    fun getDialogExitType():Int = (1..3).random()
}