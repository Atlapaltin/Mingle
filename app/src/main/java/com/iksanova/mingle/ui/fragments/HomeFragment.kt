package com.iksanova.mingle.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iksanova.mingle.R
import com.iksanova.mingle.adapters.PostAdapter
import com.iksanova.mingle.adapters.StoryAdapter
import com.iksanova.mingle.constants.Constants.ALL_POSTS
import com.iksanova.mingle.constants.Constants.INFO
import com.iksanova.mingle.constants.Constants.STORY
import com.iksanova.mingle.constants.Constants.USER_CONSTANT
import com.iksanova.mingle.databinding.FragmentHomeBinding
import com.iksanova.mingle.models.PostModel
import com.iksanova.mingle.models.StoryModel
import com.iksanova.mingle.models.UserModel
import com.iksanova.mingle.utils.AppSharedPreferences

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var appSharedPreferences: AppSharedPreferences
    private var selectedFragment: Fragment? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        appSharedPreferences = AppSharedPreferences(requireContext())

        //Set up the views using ViewBinding
        val user = appSharedPreferences.getUser()
        Glide.with(requireContext()).load(user.image).into(binding.ivUserImage)
        binding.tvUsername.text = user.username

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Post RecyclerView
        binding.recyclerView.showShimmer()
        val adapter = PostAdapter(requireContext(), mutableListOf())
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.isNestedScrollingEnabled = false
        binding.recyclerView.adapter = adapter

        //Story RecyclerView
        val storyAdapter = StoryAdapter(requireActivity(), mutableListOf())
        binding.recyclerViewStory.setHasFixedSize(true)
        binding.recyclerViewStory.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.recyclerViewStory.adapter = storyAdapter
        binding.recyclerViewStory.isNestedScrollingEnabled = false

        //Functions
        readPosts()
        getAllUsersId()
    }

    //----------------------------------Read Posts--------------------------------//
    private fun readPosts() {
        binding.recyclerView.hideShimmer()
        ref.child(ALL_POSTS).addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for (dataSnapshot in snapshot.children) {
                    val model = dataSnapshot.child(INFO).getValue(PostModel::class.java)
                    model?.let { list.add(it) }
                }
                list.reverse()
                recyclerView.adapter = adapter
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    //--------------------------------Get All Users Id--------------------------------//
    private fun getAllUsersId() {
        val followingList: MutableList<String> = mutableListOf()
        val user = appSharedPreferences.getUser()
        ref.child(USER_CONSTANT).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                followingList.clear()
                var model: UserModel?
                for (dataSnapshot in snapshot.children) {
                    model = dataSnapshot.child(INFO).getValue(UserModel::class.java)
                    model?.let {
                        if (it.key != user.uid) {
                            followingList.add(dataSnapshot.key!!)
                        }
                    }
                }
                readStory()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    //-----------------------------Read Story------------------------//
    private fun readStory() {
        ref.child(STORY).addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                val timeCurrent = System.currentTimeMillis()
                storyModelList.clear()
                storyModelList.add(StoryModel("", 0, 0, FirebaseAuth.getInstance().currentUser!!.uid, "", ""))
                for (id in followingList) {
                    var countStory = 0
                    var storyModel: StoryModel? = null
                    for (snapshot2 in snapshot.child(id).children) {
                        storyModel = snapshot2.getValue(StoryModel::class.java)
                        if (timeCurrent > storyModel!!.timeStart && timeCurrent < storyModel.timeEnd) {
                            countStory++
                        }
                    }
                    if (countStory > 0) {
                        storyModelList.add(storyModel!!)
                    }
                }
                storyAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
