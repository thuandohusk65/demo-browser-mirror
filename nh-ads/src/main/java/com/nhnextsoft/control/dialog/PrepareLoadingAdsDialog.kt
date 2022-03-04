package com.nhnextsoft.control.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import com.nhnextsoft.control.R

class PrepareLoadingAdsDialog(
    context: Context,
) : Dialog(ContextThemeWrapper(context, R.style.AppTheme)) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_prepair_loading_ads)
    }

    fun hideLoadingAdsText() {
        findViewById<View>(R.id.loading_dialog_tv).visibility = View.INVISIBLE
    }


}