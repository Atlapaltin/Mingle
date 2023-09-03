package com.iksanova.mingle.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
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
import com.iksanova.mingle.adapters.NetworkAdapter
import com.iksanova.mingle.adapters.RequestAdapter
import com.iksanova.mingle.constants.Constants.INFO
import com.iksanova.mingle.constants.Constants.REQUEST
import com.iksanova.mingle.constants.Constants.USER_CONSTANT
import com.iksanova.mingle.models.RequestModel
import com.iksanova.mingle.models.UserModel
import java.util.Collections

class NetworkFragment : Fragment() {
    private lateinit var adapter: NetworkAdapter
    private lateinit var requestAdapter: RequestAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var requestRecyclerView: RecyclerView
    private lateinit var ref: DatabaseReference
    private lateinit var user: FirebaseUser
    private lateinit var list: MutableList<RequestModel>
    private lateinit var connectionList: MutableList<UserModel>
    private lateinit var requestList: MutableList<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_network, container, false)
        recyclerView = view.findViewById(R.id.recycler_network)
        requestRecyclerView = view.findViewById(R.id.request_recyclerView)
        user = FirebaseAuth.getInstance().currentUser!!
        ref = FirebaseDatabase.getInstance().reference
        list = mutableListOf()
        requestList = mutableListOf()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Network RecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.isNestedScrollingEnabled = false

        // Request RecyclerView
        requestRecyclerView.setHasFixedSize(true)
        requestRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        requestRecyclerView.isNestedScrollingEnabled = false

        // Functions
        readUsers()
        getAllUsersId()
    }

    //--------------------------------Get All Users Id--------------------------------//
    private fun getAllUsersId() {
        requestList = mutableListOf()
        val reference = FirebaseDatabase.getInstance().reference.child(USER_CONSTANT).child(user.uid).child(REQUEST)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                requestList.clear()
                for (dataSnapshot in snapshot.children) {
                    requestList.add(dataSnapshot.key!!)
                }
                readRequest()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    //----------------------------------Read Request--------------------------------//
    private fun readRequest() {
        ref.child(USER_CONSTANT).addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for (id in requestList) {
                    val model = snapshot.child(id).child(INFO).getValue(RequestModel::class.java)
                    list.add(model!!)
                }

                list.reverse()
                requestAdapter = RequestAdapter(activity!!, list)
                requestRecyclerView.adapter = requestAdapter
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    //----------------------------------Read Users--------------------------------//
    private fun readUsers() {
        connectionList = mutableListOf()
        ref.child(USER_CONSTANT).addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                connectionList.clear()
                for (dataSnapshot in snapshot.children) {
                    val model = dataSnapshot.child(INFO).getValue(UserModel::class.java)
                    if (model!!.key != user.uid) {
                        connectionList.add(model)
                    }
                }
                connectionList.reverse()
                adapter = NetworkAdapter(activity!!, connectionList)
                recyclerView.adapter = adapter
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
