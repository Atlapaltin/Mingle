package com.iksanova.mingle.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.iksanova.mingle.R
import com.iksanova.mingle.base.BaseActivity
import com.iksanova.mingle.constants.Constants.USER_CONSTANT

class EditProfileIntroActivity : BaseActivity() {
    private lateinit var editTextFirstName: EditText
    private lateinit var editTextLastName: EditText
    private lateinit var editTextHeadline: EditText
    private lateinit var editTextPosition: EditText
    private lateinit var editTextEducation: EditText
    private lateinit var editTextLocation: EditText
    private lateinit var stringUserName: String
    private lateinit var stringUserImgUrl: String
    private lateinit var stringUserLocation: String
    private lateinit var ref: DatabaseReference
    private lateinit var user: FirebaseUser
    private lateinit var saveBtn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile_intro)
        user = FirebaseAuth.getInstance().currentUser!!
        ref = FirebaseDatabase.getInstance().reference.child(USER_CONSTANT).child(user.uid)
        editTextFirstName = findViewById(R.id.edit_first_name)
        editTextLastName = findViewById(R.id.edit_last_name)
        editTextHeadline = findViewById(R.id.edit_headline)
        editTextPosition = findViewById(R.id.edit_position)
        editTextEducation = findViewById(R.id.edit_education)
        editTextLocation = findViewById(R.id.edit_location)

        saveBtn = findViewById(R.id.save_btn)

        // Get Data From Activity
        val intent = intent
        stringUserName = intent.getStringExtra("user_name")!!
        stringUserImgUrl = intent.getStringExtra("user_imgUrl")!!
        stringUserLocation = intent.getStringExtra("user_location")!!

        val split = stringUserName.split(" ")
        editTextFirstName.setText(split[0])
        editTextLastName.setText(split[1])

        editTextLocation.setText(stringUserLocation)

        // Save Button
        saveBtn.setOnClickListener {
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val map: MutableMap<String, Any> = HashMap()
                    map["education"] = editTextEducation.text.toString()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }
}
