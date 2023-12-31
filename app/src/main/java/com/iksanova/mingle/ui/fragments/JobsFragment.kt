package com.iksanova.mingle.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.iksanova.mingle.utils.AppSharedPreferences
import com.iksanova.mingle.R

class JobsFragment : Fragment() {
    private lateinit var profileImg: ImageView
    private lateinit var appSharedPreferences: AppSharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_jobs, container, false)
        profileImg = view.findViewById(R.id.user_img)
        appSharedPreferences = AppSharedPreferences(requireContext())
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(requireContext()).load(appSharedPreferences.imgUrl).into(profileImg)
    }
}
