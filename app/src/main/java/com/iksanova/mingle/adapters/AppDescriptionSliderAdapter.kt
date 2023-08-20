package com.iksanova.mingle.adapters

import com.iksanova.mingle.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter

class AppDescriptionSliderAdapter(private val context: Context) : PagerAdapter() {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private val images = intArrayOf(
        R.drawable.pic_1,
        R.drawable.pic_2,
        R.drawable.pic_3
    )
    private val headings = intArrayOf(
        R.string.search_heading,
        R.string.build_heading,
        R.string.stay_heading
    )

    override fun getCount(): Int {
        return headings.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object` as ConstraintLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = layoutInflater.inflate(R.layout.slides_layout, container, false)
        val imageView = view.findViewById<ImageView>(R.id.slider_image)
        val heading = view.findViewById<TextView>(R.id.slider_heading)
        imageView.setImageResource(images[position])
        heading.setText(headings[position])
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ConstraintLayout)
    }
}