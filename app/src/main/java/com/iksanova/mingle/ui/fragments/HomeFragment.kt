package com.iksanova.mingle.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.iksanova.mingle.R
import com.iksanova.mingle.adapters.PostAdapter
import com.iksanova.mingle.adapters.StoryAdapter
import com.iksanova.mingle.constants.Constants.ALL_POSTS
import com.iksanova.mingle.constants.Constants.INFO
import com.iksanova.mingle.constants.Constants.STORY
import com.iksanova.mingle.constants.Constants.USER_CONSTANT
import com.iksanova.mingle.models.PostModel
import com.iksanova.mingle.models.StoryModel
import com.iksanova.mingle.models.UserModel
import com.todkars.shimmer.ShimmerRecyclerView

class HomeFragment : Fragment() {
    private lateinit var user: FirebaseUser
    private var list: MutableList<PostModel> = mutableListOf()
    private lateinit var adapter: PostAdapter
    private lateinit var recyclerView: ShimmerRecyclerView
    private lateinit var recyclerViewStory: ShimmerRecyclerView
    private lateinit var ref: DatabaseReference
    private var storyModelList: MutableList<StoryModel> = mutableListOf()
    private lateinit var storyAdapter: StoryAdapter
    private var followingList: MutableList<String> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = view.findViewById(R.id.post_recycler)
        recyclerViewStory = view.findViewById(R.id.story_recycler)
        user = FirebaseAuth.getInstance().currentUser!!
        ref = FirebaseDatabase.getInstance().reference
        ref.keepSynced(true)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Post RecyclerView
        recyclerView.showShimmer()
        adapter = PostAdapter(requireContext(), list)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.isNestedScrollingEnabled = false

        //Story RecyclerView
        storyAdapter = StoryAdapter(requireActivity(), storyModelList)
        recyclerViewStory.setHasFixedSize(true)
        recyclerViewStory.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        recyclerViewStory.adapter = storyAdapter
        recyclerViewStory.isNestedScrollingEnabled = false

        //Functions
        readPosts()
        getAllUsersId()
    }

    //----------------------------------Read Posts--------------------------------//
    private fun readPosts() {
        recyclerView.hideShimmer()
        ref.child(ALL_POSTS).addListenerForSingleValueEvent(object : ValueEventListener {
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
        followingList = mutableListOf()
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
