package com.nhnextsoft.control.dialog

import android.R
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nhnextsoft.control.AdmodSP
import com.nhnextsoft.control.funtion.DialogExitListener

class DialogExitAppExtend(
    private var mContext: Context,
    var nativeAd: NativeAd,
    var type: Int,
    var adView: NativeAdView? = null,
) : Dialog(
    mContext,
    R.style.Theme_Translucent_NoTitleBar
) {
    var dialogExitListener: DialogExitListener? = null

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        when (type) {
            1 -> {
                setContentView(com.nhnextsoft.control.R.layout.view_dialog_exit1)
            }
            2 -> {
                setContentView(com.nhnextsoft.control.R.layout.view_dialog_exit2)
            }
            else -> {
                setContentView(com.nhnextsoft.control.R.layout.view_dialog_exit3)
            }
        }
        initView()
    }

    private fun initView() {
        val btnExit = findViewById<TextView>(com.nhnextsoft.control.R.id.btnExit)
        val btnCancel = findViewById<TextView>(com.nhnextsoft.control.R.id.btnCancel)
        val mFrameLayout = findViewById<FrameLayout>(com.nhnextsoft.control.R.id.frAds)
        if (adView == null) {
            adView = when (type) {
                1 -> {
                    LayoutInflater.from(context)
                        .inflate(com.nhnextsoft.control.R.layout.native_exit1, null) as NativeAdView
                }
                2 -> {
                    LayoutInflater.from(context)
                        .inflate(com.nhnextsoft.control.R.layout.native_exit1, null) as NativeAdView
                }
                else -> {
                    LayoutInflater.from(context)
                        .inflate(com.nhnextsoft.control.R.layout.native_exit3, null) as NativeAdView
                }
            }
        }
        mFrameLayout.addView(adView)
        AdmodSP.instance?.populateUnifiedNativeAdView(context, nativeAd, adView!!)
        btnExit.setOnClickListener {
            dismiss()
            if (dialogExitListener != null) {
                dialogExitListener?.onExit(true)
            } else {
                (context as Activity).finish()
            }
        }
        btnCancel.setOnClickListener {
            if (dialogExitListener != null) {
                dialogExitListener!!.onExit(false)
            }
            dismiss()
        }
    }
}