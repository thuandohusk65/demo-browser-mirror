package com.ynsuper.screenmirroring.view.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.ynsuper.screenmirroring.R
import com.ynsuper.screenmirroring.databinding.ActivityTutorialBinding
import com.ynsuper.screenmirroring.model.TutorialModel
import com.ynsuper.screenmirroring.utility.Constants
import com.ynsuper.screenmirroring.utility.ZoomOutPageTransformer
import com.ynsuper.screenmirroring.view.adapter.TutorialPagerAdapter

class TutorialActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTutorialBinding
    private var currentItemViewPager = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
    }

    private fun initView() {
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        binding.viewpagerTutorial.setPageTransformer(true, ZoomOutPageTransformer())
        val tutorialPagerAdapter = TutorialPagerAdapter(this, loadAllImageTutorial())
        binding.viewpagerTutorial.adapter = tutorialPagerAdapter
        binding.imageArrowRight.visibility = View.VISIBLE
        if (intent.hasExtra(Constants.EXTRA_TUTORIAL)
            && intent.getBooleanExtra(Constants.EXTRA_TUTORIAL, false)
        ) {
            binding.imageClose.visibility = View.GONE
            binding.buttonNext.visibility = View.VISIBLE
        } else {
            binding.imageClose.visibility = View.VISIBLE
            binding.buttonNext.visibility = View.GONE
        }
        handleClick()
        setContentView(binding.root)

    }

    private fun handleClick() {
        binding.viewpagerTutorial.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                binding.textStep.text = "Step " + (position + 1) +"/"+ (loadAllImageTutorial().size)
                currentItemViewPager = position

                when (position) {
                    0 -> {
                        binding.buttonNext.text = getString(R.string.next_screen)
                        binding.imageArrowLeft.visibility = View.INVISIBLE
                        binding.textStep.visibility = View.VISIBLE
                    }
                    loadAllImageTutorial().size - 1 -> {
                        binding.imageArrowRight.visibility = View.INVISIBLE
                        binding.buttonNext.text = getString(R.string.start_now)
                        binding.textStep.visibility = View.GONE
                    }
                    else -> {
                        binding.buttonNext.text = getString(R.string.next_screen)
                        binding.imageArrowRight.visibility = View.VISIBLE
                        binding.imageArrowLeft.visibility = View.VISIBLE
                        binding.textStep.visibility = View.VISIBLE

                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }


        })
        binding.imageArrowLeft.setOnClickListener {
            currentItemViewPager -= 1
            if (currentItemViewPager <= 0) {
                currentItemViewPager = 0
            }
            binding.viewpagerTutorial.currentItem = currentItemViewPager
        }
        binding.imageArrowRight.setOnClickListener {
            currentItemViewPager += 1
            if (currentItemViewPager >= loadAllImageTutorial().size) {
                currentItemViewPager = loadAllImageTutorial().size - 1
            }
            binding.viewpagerTutorial.currentItem = currentItemViewPager

        }
        binding.imageClose.setOnClickListener {
            finish()
        }

        binding.buttonNext.setOnClickListener {
            currentItemViewPager += 1
            if (binding.buttonNext.text.equals(getString(R.string.start_now))) {
                val sharedPreferences =
                    getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean(Constants.KEY_START_FISRT_APP, false)
                editor.commit()
                editor.apply()

                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
            if (currentItemViewPager >= loadAllImageTutorial().size) {
                currentItemViewPager = loadAllImageTutorial().size - 1
            }
            binding.viewpagerTutorial.currentItem = currentItemViewPager
        }
    }

    private fun loadAllImageTutorial(): ArrayList<TutorialModel> {
        val arrTutorial = ArrayList<TutorialModel>()
//        return if (!intent.hasExtra(Constants.EXTRA_TUTORIAL)) {
            arrTutorial.add(TutorialModel(R.drawable.ic_step_1, R.string.text_step_1))
            arrTutorial.add(TutorialModel(R.drawable.ic_step_2, R.string.text_step_2))
            arrTutorial.add(TutorialModel(R.drawable.ic_step_3, R.string.text_step_3))
            arrTutorial.add(TutorialModel(R.drawable.ic_step_4, R.string.text_step_4))
            arrTutorial.add(TutorialModel(R.drawable.ic_step_5, R.string.text_step_5))
//            arrTutorial
//        } else {
//        arrTutorial.add(TutorialModel(R.drawable.ic_step_1, R.string.text_step_1))
//        arrTutorial.add(TutorialModel(R.drawable.ic_step_2, R.string.text_step_2))
//        arrTutorial.add(TutorialModel(R.drawable.ic_step_5, R.string.text_step_5))
        return arrTutorial
//        }

    }
}