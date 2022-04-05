package com.nhnextsoft.screenmirroring.view.dialog

import android.content.Intent
import android.content.res.Resources
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import androidx.fragment.app.DialogFragment
import com.google.android.play.core.review.ReviewManagerFactory
import com.nhnextsoft.screenmirroring.BuildConfig
import com.nhnextsoft.screenmirroring.R
import com.nhnextsoft.screenmirroring.databinding.DialogRateAppBinding
import timber.log.Timber
import java.util.*


class RateAppDialog : DialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance() =
            RateAppDialog().apply {}
    }

    private lateinit var binding: DialogRateAppBinding
    private val manager = activity?.let { ReviewManagerFactory.create(it) }
    private var rateImage: IntArray? = intArrayOf(
        R.drawable.rate_normal,
        R.drawable.rate_1,
        R.drawable.rate_2,
        R.drawable.rate_3,
        R.drawable.rate_4,
        R.drawable.rate_5
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogRateAppBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWidthPercent(90)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.rateBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            Timber.d("ratingBar: $ratingBar -- rating: $rating -- fromUser: $fromUser")
            binding.imageEmoji.setImageResource(rateImage?.get(rating.toInt()) ?: R.drawable.rate_normal)
            binding.btnRate.isEnabled = rating.toInt() > 0
            when (rating.toInt()) {
                0 -> {
                    binding.textTitle.visibility = View.GONE
                    binding.textDescription.text = getString(R.string.appreciate_rating_us)
                }
                in 1..3 -> {
                    binding.textTitle.visibility = View.VISIBLE
                    binding.textTitle.text = getString(R.string.bad_rate_title)
                    binding.textDescription.text = getString(R.string.medium_rate_description)
                    binding.btnRate.text = getString(R.string.rate_us)
                }
                4, 5 -> {
                    binding.textTitle.visibility = View.VISIBLE
                    binding.textTitle.text = getString(R.string.good_rate_title)
                    binding.textDescription.text = getString(R.string.good_rate_description)
                    binding.btnRate.text = getString(R.string.rate_us_on_google_play)
                }
            }
        }

        binding.btnRate.setOnClickListener {
            val numRating = binding.rateBar.rating
            if (numRating > 0 && numRating < 4) {
                feedBackToEmail()
            } else {
                val request = manager?.requestReviewFlow()
                request?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // We got the ReviewInfo object
                        val reviewInfo = task.result
                    } else {

                    }
                }
            }
            dismiss()
        }
    }

    private fun setWidthPercent(percentage: Int) {
        val percent = percentage.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    fun feedBackToEmail() {
        var systemInfo = "\n\n\n\n==== SYSTEM-INFO ===\n"
        systemInfo += "Device : " + Build.DEVICE + "\n"
        systemInfo += "SDK Version : " + Build.VERSION.SDK_INT + "\n"
        systemInfo += "App Version : " + BuildConfig.VERSION_NAME + "\n"
        systemInfo += "Language : " + Locale.getDefault().language + "\n"
        systemInfo += "TimeZone : " + TimeZone.getDefault().id + "\n"
        systemInfo += "Device Type : " + Build.MODEL + "\n"


        val email = "taptapstudioapp+screenmirroring@gmail.com"
        val subject = "Feedback by Screen Mirroring"
        val chooserTitle = getString(R.string.title_feedback)

        try {
            composeEmail(arrayOf(email), subject, systemInfo)
        } catch (e: Exception) {
            activity?.let {
                ShareCompat.IntentBuilder(it)
                    .setType("message/rfc822")
                    .addEmailTo(email)
                    .setSubject(subject)
                    .setText("" + systemInfo)
                    //                .setHtmlText(systemInfo) //If you are using HTML in your body text
                    .setChooserTitle(chooserTitle)
                    .startChooser()
            }
        }

    }

    private fun composeEmail(addresses: Array<String>, subject: String, text: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, addresses)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val openInChooser = Intent.createChooser(intent, getString(R.string.title_feedback))
        startActivity(openInChooser)
    }
}