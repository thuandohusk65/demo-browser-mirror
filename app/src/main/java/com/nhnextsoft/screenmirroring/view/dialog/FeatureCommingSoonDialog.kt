package com.nhnextsoft.screenmirroring.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.nhnextsoft.screenmirroring.databinding.DialogFeatureCommingSoonBinding

class FeatureCommingSoonDialog: DialogFragment() {
    companion object {
        @JvmStatic
        fun newInstance() =
            FeatureCommingSoonDialog().apply {}
    }

    private lateinit var binding: DialogFeatureCommingSoonBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogFeatureCommingSoonBinding.inflate(inflater)
        return binding.root
    }

}