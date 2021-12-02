package com.ynsuper.screenmirroring.utility

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Button
import com.ynsuper.screenmirroring.R
import com.ynsuper.screenmirroring.callback.ConnectionCallback
import android.content.ActivityNotFoundException

import android.content.Intent
import android.net.*
import android.provider.Settings
import androidx.core.content.ContextCompat

import androidx.core.content.ContextCompat.startActivity


// todo: check dialog width in symphony mobile
public class NoInternetDialog private constructor(
    private val activity: Activity,
    private val cancelable: Boolean,

    private val noInternetConnectionTitle: String,
    private val noInternetConnectionMessage: String,
    private val showInternetOnButtons: Boolean,
    private val pleaseTurnOnText: String
) : Dialog(activity), View.OnClickListener {

    private val TAG = "NoInternetDialog"

    private var connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private lateinit var connectivityManagerCallback: ConnectivityManager.NetworkCallback

    var connectionCallback: ConnectionCallback? = null

    init {
        initReceivers()
    }

    private fun initReceivers() {

        updateConnection()

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                connectivityManager.registerDefaultNetworkCallback(
                    getConnectivityManagerCallback()
                )
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                val builder = NetworkRequest.Builder()
                    .addTransportType(android.net.NetworkCapabilities.TRANSPORT_CELLULAR)
                    .addTransportType(android.net.NetworkCapabilities.TRANSPORT_WIFI)
                    .addTransportType(android.net.NetworkCapabilities.TRANSPORT_ETHERNET)
                connectivityManager.registerNetworkCallback(
                    builder.build(),
                    getConnectivityManagerCallback()
                )
            }
        }

    }

    private fun updateConnection() {
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        if (activeNetwork?.isConnected == true) {
            dismiss()

            connectionCallback?.hasActiveConnection(true)
        } else {
            showDialog()

            connectionCallback?.hasActiveConnection(false)
        }
    }

    private fun getConnectivityManagerCallback(): ConnectivityManager.NetworkCallback {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            connectivityManagerCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Log.d(TAG, "onAvailable(): ${network.toString()}")

                    dismiss()

                    connectionCallback?.hasActiveConnection(true)
                }

                override fun onLost(network: Network) {
                    Log.d(TAG, "onLost(): ${network.toString()}")

                    showDialog()

                    connectionCallback?.hasActiveConnection(false)
                }
            }

            return connectivityManagerCallback
        } else {
            throw IllegalAccessError("This should not happened")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_no_internet)

        initProperties()
        initMainWindow()
        initViews()
    }

    private fun initProperties() {
        setCancelable(cancelable)
    }

    private fun initMainWindow() {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun initViews() {
        // Check if the dialog width is bigger then the screen width!
        val displayMetrics = DisplayMetrics()
        window?.apply {
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val widthDp = displayMetrics.widthPixels.toFloat().toDp(context)

            Log.d(TAG, "width: $widthDp")


        }
        findViewById<Button>(R.id.btn_wifi_on).setOnClickListener {
            try {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)

                context.startActivity(intent)
                dismiss()
            } catch (activitynotfoundexception: ActivityNotFoundException) {
                activitynotfoundexception.printStackTrace()
            }
        }
    }


    override fun show() {
        if (!activity.isFinishing) {
            super.show()
        }
    }

    fun showDialog() {

        Ping().apply {
            connectionCallback = object : ConnectionCallback {
                override fun hasActiveConnection(hasActiveConnection: Boolean) {
                    if (!hasActiveConnection) {
                        show()
                    }
                }
            }

            execute(context)
        }

    }

    fun destroy() {

        dismiss()

        connectivityManager.unregisterNetworkCallback(connectivityManagerCallback)
    }

    class Builder(
        private val activity: Activity
    ) {

        var cancelable = false
        var connectionCallback: ConnectionCallback? = null

        var noInternetConnectionTitle = activity.getString(R.string.default_title)
        var noInternetConnectionMessage = activity.getString(R.string.default_message)
        var showInternetOnButtons = true
        var pleaseTurnOnText = activity.getString(R.string.please_turn_on)
        var wifiOnButtonText = activity.getString(R.string.wifi)
        var showAirplaneModeOffButtons = true

        fun build(): NoInternetDialog {
            val dialog = NoInternetDialog(
                activity,
                cancelable,
                noInternetConnectionTitle,
                noInternetConnectionMessage,
                showInternetOnButtons,
                pleaseTurnOnText
            )

            dialog.connectionCallback = connectionCallback

            return dialog
        }


    }

    override fun onClick(v: View?) {

    }
}

private fun Float.toPx(context: Context): Int {
    return (this * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
}

private fun Float.toDp(context: Context): Float {
    return this / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}