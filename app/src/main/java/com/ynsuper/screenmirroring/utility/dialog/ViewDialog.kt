package com.ynsuper.screenmirroring.utility.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import com.ynsuper.screenmirroring.R
import com.ynsuper.screenmirroring.view.activity.RemoveAdsActivity

object ViewDialog {
    fun showDialogUpgrade(context: Context?) {
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_upgrade)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val buttonCancel = dialog.findViewById<TextView>(R.id.button_cancel)
        val buttonUpgradeNow = dialog.findViewById<TextView>(R.id.button_upgrade_now)
        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }
        buttonUpgradeNow.setOnClickListener {
            val intent = Intent(context, RemoveAdsActivity::class.java)
            context.startActivity(intent)
            dialog.dismiss()
        }
        dialog.show()

    }
}