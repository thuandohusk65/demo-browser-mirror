package com.nhnextsoft.screenmirroring.view.dialog

import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.nhnextsoft.screenmirroring.databinding.FragmentNoWifiBinding

/**
 * A simple [Fragment] subclass.
 * Use the [NoWifiFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NoWifiFragment : DialogFragment() {
    private lateinit var binding: FragmentNoWifiBinding

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment NoWifiFragment.
         */
        @JvmStatic
        fun newInstance() =
            NoWifiFragment().apply {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentNoWifiBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWidthPercent(90)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.btnClose.setOnClickListener {
            dismiss()
        }
        binding.btnTry.setOnClickListener {
            dismiss()
        }
    }


    /**
     * Call this method (in onActivityCreated or later) to set
     * the width of the dialog to a percentage of the current
     * screen width.
     */
    fun setWidthPercent(percentage: Int) {
        val percent = percentage.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    /**
     * Call this method (in onActivityCreated or later)
     * to make the dialog near-full screen.
     */
    fun setFullScreen() {
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}