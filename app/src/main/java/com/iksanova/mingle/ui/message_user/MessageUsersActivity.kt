package com.iksanova.mingle.ui.message_user

import android.annotation.SuppressLint
import android.os.Bundle
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
import com.iksanova.mingle.adapters.MessageUserAdapter
import com.iksanova.mingle.base.BaseActivity
import com.iksanova.mingle.constants.Constants
import com.iksanova.mingle.models.UserModel

class MessageUsersActivity : BaseActivity() {
    private lateinit var list: MutableList<UserModel>
    private lateinit var ref: DatabaseReference
    private lateinit var user: FirebaseUser
    private lateinit var adapter: MessageUserAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_users)
        recyclerView = findViewById(R.id.user_recycler)
        user = FirebaseAuth.getInstance().currentUser!!
        ref = FirebaseDatabase.getInstance().reference
        list = mutableListOf()

        //User RecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        //Function
        readUsers()
    }

    //----------------------------------Read Users--------------------------------//
    private fun readUsers() {
        ref.child(Constants.USER_CONSTANT).addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for (dataSnapshot in snapshot.children) {
                    val model = dataSnapshot.child(Constants.INFO).getValue(UserModel::class.java)
                    if (model!!.key != user.uid) {
                        list.add(model)
                    }
                }
                list.reverse()
                adapter = MessageUserAdapter(this@MessageUsersActivity, list)
                recyclerView.adapter = adapter
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
