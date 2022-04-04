package com.nhnextsoft.screenmirroring.view.activity.stream


import android.app.Activity
import android.content.*
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.elvishew.xlog.XLog
import com.nhnextsoft.screenmirroring.R
import com.nhnextsoft.screenmirroring.databinding.ActivityStreamBinding
import com.nhnextsoft.screenmirroring.databinding.ItemDeviceAddressBinding
import com.nhnextsoft.screenmirroring.service.AppService
import com.nhnextsoft.screenmirroring.service.ServiceMessage
import com.nhnextsoft.screenmirroring.service.helper.IntentAction
import info.dvkr.screenstream.data.model.AppError
import info.dvkr.screenstream.data.model.FatalError
import info.dvkr.screenstream.data.model.FixableError
import info.dvkr.screenstream.data.other.asString
import info.dvkr.screenstream.data.other.getLog
import info.dvkr.screenstream.data.other.setUnderlineSpan
import info.dvkr.screenstream.data.settings.SettingsReadOnly
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber
import androidx.appcompat.app.AlertDialog
import info.dvkr.screenstream.data.settings.Settings


class StreamActivity : AppCompatActivity() {

    private val settingsReadOnly: SettingsReadOnly by inject()

    private lateinit var binding: ActivityStreamBinding
    private val settings: Settings by inject()
    private val serviceMessageLiveData = MutableLiveData<ServiceMessage>()
    private var serviceMessageFlowJob: Job? = null
    private var isBound: Boolean = false
    private var isCastPermissionsPending: Boolean = false
    private var permissionsErrorDialog: MaterialDialog? = null

    var viewModel: StreamViewModel? = null

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, StreamActivity::class.java)
        }

        private const val SCREEN_CAPTURE_REQUEST_CODE = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStreamBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[StreamViewModel::class.java]

        binding.btnStopStream.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Want to disconnect?")
                .setMessage("Connection will be interrupted.")
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    Timber.d("onPress Stop Stream")

                    IntentAction.StopStream.sendToAppService(this@StreamActivity)
                    onBackPressed()
                }
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }

    }

    private fun setNewPortAndReStart() {
        val newPort = (1025..65535).random()
        Timber.d("setNewPortAndReStart $newPort")
        if (settings.severPort != newPort) settings.severPort = newPort
        IntentAction.StartStream.sendToAppService(this@StreamActivity)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            serviceMessageFlowJob =
                lifecycle.coroutineScope.launch(CoroutineName("StreamActivity.ServiceMessageFlow")) {
                    (service as AppService.AppServiceBinder).getServiceMessageFlow()
                        .onEach { serviceMessage ->
                            Timber.d("onServiceMessage $serviceMessage")
                            serviceMessageLiveData.value = serviceMessage
                        }
                        .catch { cause -> Timber.d("onServiceMessage : $cause") }
                        .collect()
                }

            isBound = true
            IntentAction.GetServiceState.sendToAppService(this@StreamActivity)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceMessageFlowJob?.cancel()
            serviceMessageFlowJob = null
            isBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        val projectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        IntentAction.CastIntent(projectionManager.createScreenCaptureIntent())
            .sendToAppService(this@StreamActivity)

        serviceMessageLiveData.observe(this) { serviceMessage ->
            when (serviceMessage) {
                is ServiceMessage.ServiceState -> onServiceStateMessage(serviceMessage)
            }
        }
        bindService(
            AppService.getAppServiceIntent(this),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )

    }

    override fun onStop() {
        if (isBound) {
            serviceMessageFlowJob?.cancel()
            serviceMessageFlowJob = null
            unbindService(serviceConnection)
            isBound = false
        }
        super.onStop()
    }

    private fun showErrorDialog(
        @StringRes titleRes: Int = R.string.permission_activity_error_title,
        @StringRes messageRes: Int = R.string.permission_activity_error_unknown
    ) {
        permissionsErrorDialog?.dismiss()

        permissionsErrorDialog = MaterialDialog(this).show {
            lifecycleOwner(this@StreamActivity)
            title(titleRes)
            message(messageRes)
            positiveButton(android.R.string.ok)
            cancelable(false)
            cancelOnTouchOutside(false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SCREEN_CAPTURE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                XLog.d(getLog("onActivityResult", "Cast permission granted"))
                require(data != null) { "onActivityResult: data = null" }
                IntentAction.CastIntent(data).sendToAppService(this@StreamActivity)
            } else {
                XLog.w(getLog("onActivityResult", "Cast permission denied"))

                IntentAction.CastPermissionsDenied.sendToAppService(this@StreamActivity)
                isCastPermissionsPending = false

                showErrorDialog(
                    R.string.permission_activity_cast_permission_required_title,
                    R.string.permission_activity_cast_permission_required
                )
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun checkPermission(serviceMessage: ServiceMessage.ServiceState) {
        if (serviceMessage.isWaitingForPermission) {
            if (isCastPermissionsPending) {
                XLog.i(getLog("onServiceMessage", "Ignoring: isCastPermissionsPending == true"))
            } else {
                isCastPermissionsPending = true

                val projectionManager =
                    getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                try {
                    val createScreenCaptureIntent = projectionManager.createScreenCaptureIntent()
                    startActivityForResult(
                        createScreenCaptureIntent, SCREEN_CAPTURE_REQUEST_CODE//,options.toBundle()
                    )
                } catch (ex: ActivityNotFoundException) {
                    showErrorDialog(
                        R.string.permission_activity_error_title_activity_not_found,
                        R.string.permission_activity_error_activity_not_found
                    )
                }
            }
        } else {
            isCastPermissionsPending = false
        }
    }

    private fun showError(appError: AppError?) {
        if (appError == null) {
            binding.tvFragmentStreamError.visibility = View.GONE
        } else {
            when (appError) {
                is FixableError.AddressInUseException -> setNewPortAndReStart()
            }
            XLog.d(getLog("showError", appError.toString()))
            binding.tvFragmentStreamError.text = when (appError) {
                is FixableError.AddressInUseException -> getString(R.string.error_port_in_use)
                is FixableError.CastSecurityException -> getString(R.string.error_invalid_media_projection)
                is FixableError.AddressNotFoundException -> getString(R.string.error_ip_address_not_found)
                is FatalError.BitmapFormatException -> getString(R.string.error_wrong_image_format)
                else -> appError.toString()
            }
            binding.tvFragmentStreamError.visibility = View.VISIBLE
        }
    }

    private fun startStreamScreen() {
        IntentAction.StartStream.sendToAppService(this@StreamActivity)
        binding.btnStopStream.visibility = View.VISIBLE
    }

    private fun onServiceStateMessage(serviceMessage: ServiceMessage.ServiceState) {
        Timber.d("onServiceStateMessage ${serviceMessage}")
        // Interfaces
        binding.llFragmentStreamAddresses.removeAllViews()
        checkPermission(serviceMessage)
        if (serviceMessage.appError == null && !serviceMessage.isStreaming && !serviceMessage.isWaitingForPermission && !serviceMessage.isBusy) {
            startStreamScreen()
        }
        if (serviceMessage.netInterfaces.isEmpty()) {
            with(
                ItemDeviceAddressBinding.inflate(
                    layoutInflater,
                    binding.llFragmentStreamAddresses,
                    false
                )
            ) {
                tvItemDeviceAddressName.text = ""
                binding.llFragmentStreamAddresses.addView(this.root)
            }
        } else {
            serviceMessage.netInterfaces.sortedBy { it.address.asString() }
                .forEach { netInterface ->
                    val fullAddress =
                        "http://${netInterface.address.asString()}:${settingsReadOnly.severPort}"
                    binding.streamForm.tvItemDeviceAddress.text = fullAddress.setUnderlineSpan()

                }
        }

        showError(serviceMessage.appError)
    }

}