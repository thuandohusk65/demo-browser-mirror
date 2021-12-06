package com.nhnextsoft.screenmirroring.view.adapter

import android.content.Context
import android.view.View
import androidx.viewpager.widget.PagerAdapter
import android.view.LayoutInflater

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import androidx.constraintlayout.widget.ConstraintLayout
import com.nhnextsoft.screenmirroring.R
import com.nhnextsoft.screenmirroring.model.TutorialModel
import java.util.*
import kotlin.collections.ArrayList


class TutorialPagerAdapter(val context: Context, private val arrTutorial: ArrayList<TutorialModel>) : PagerAdapter() {
    private var mLayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return arrTutorial.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as ConstraintLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        // inflating the item.xml
        val itemView: View = mLayoutInflater.inflate(R.layout.item_tutorial, container, false)

        // referencing the image view from the item.xml file
        val arrTutorial = itemView.findViewById<View>(R.id.image_tutorial) as ImageView
        val textDescription = itemView.findViewById(R.id.text_description) as TextView

        // setting the image in the imageView
        arrTutorial.setImageResource(this.arrTutorial[position].image)
        textDescription.text = context.resources.getText(this.arrTutorial[position].description)

        // Adding the View
        Objects.requireNonNull(container).addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
//        super.destroyItem(container, position, `object`)
        container.removeView(`object` as ConstraintLayout?)

    }

}