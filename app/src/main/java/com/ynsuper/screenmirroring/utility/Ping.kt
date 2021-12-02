package com.ynsuper.screenmirroring.utility

import android.content.Context
import android.os.AsyncTask
import com.taptap.photoedtios.view.customview.dialog_nointernet.NoInternetUtils
import com.ynsuper.screenmirroring.callback.ConnectionCallback

class Ping : AsyncTask<Context, Void, Boolean>() {

    var connectionCallback: ConnectionCallback? = null

    override fun doInBackground(vararg params: Context?): Boolean {

        params[0]?.let {
            return NoInternetUtils.isConnectedToInternet(it)
                    && NoInternetUtils.hasActiveInternetConnection()
        }

        return false

    }

    override fun onPostExecute(result: Boolean) {
        super.onPostExecute(result)

        connectionCallback?.hasActiveConnection(result)

    }

}