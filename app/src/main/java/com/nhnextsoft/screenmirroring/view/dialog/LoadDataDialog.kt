package com.nhnextsoft.screenmirroring.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import com.nhnextsoft.screenmirroring.R

class LoadDataDialog(
    context: Context,
) : Dialog(ContextThemeWrapper(context, R.style.AppTheme)) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_loading_data)
    }
}