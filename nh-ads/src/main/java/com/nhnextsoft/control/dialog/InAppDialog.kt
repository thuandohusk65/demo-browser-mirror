package com.nhnextsoft.control.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.TextView
import com.nhnextsoft.control.R
import com.nhnextsoft.control.application.AppGlobal
import com.nhnextsoft.control.billing.AppPurchase

class InAppDialog(
    mContext: Context,
    private val productID: String,
) : Dialog(
    ContextThemeWrapper(mContext, R.style.AppTheme)
) {
//    mContext, R.style.AppTheme


    var callback: ICallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_inapp_buy)
        initView()
    }

    private fun initView() {
        findViewById<View>(R.id.iv_close).setOnClickListener { onBackPressed() }
        findViewById<View>(R.id.tv_pucharse).setOnClickListener { callback!!.onPurcharse() }
        val tvOldPrice = findViewById<TextView>(R.id.tv_old_price)
        val tvPrice = findViewById<TextView>(R.id.tv_price)
        if (AppPurchase.instance.discount == 1.0) {
            tvOldPrice.visibility = View.GONE
            findViewById<View>(R.id.view_split).visibility = View.GONE
        } else {
            tvOldPrice.visibility = View.VISIBLE
            findViewById<View>(R.id.view_split).visibility = View.VISIBLE
            val currency = if (AppGlobal.BUILD_DEBUG) AppPurchase.instance
                .getCurrency("android.test.purchased",
                    AppPurchase.TYPE_IAP.PURCHASE) else AppPurchase.instance.getCurrency(productID,
                AppPurchase.TYPE_IAP.PURCHASE)
            val priceWithoutCurrency = if (AppGlobal.BUILD_DEBUG) AppPurchase.instance
                .getPriceWithoutCurrency("android.test.purchased",
                    AppPurchase.TYPE_IAP.PURCHASE) else AppPurchase.instance.getPriceWithoutCurrency(
                productID, AppPurchase.TYPE_IAP.PURCHASE)
            val oldPrice = priceWithoutCurrency * AppPurchase.instance.discount
            tvOldPrice.text = AppPurchase.instance.formatCurrency(oldPrice / 1000000, currency)
        }
//                tvOldPrice.setText(AppPurchase.instance.getOldPrice());
        tvPrice.text = if (AppGlobal.BUILD_DEBUG) AppPurchase.instance
            .getPrice("android.test.purchased") else AppPurchase.instance.getPrice(productID)
        tvOldPrice.paintFlags = tvOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    }

    interface ICallback {
        fun onPurcharse()
    }
}