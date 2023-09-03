package com.iksanova.mingle.ui.custom_user

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.iksanova.mingle.R
import com.iksanova.mingle.base.BaseActivity
import com.iksanova.mingle.constants.Constants.REQUEST
import com.iksanova.mingle.constants.Constants.USER_CONSTANT
import com.iksanova.mingle.models.UserModel
import de.hdodenhof.circleimageview.CircleImageView

class CustomUserActivity : BaseActivity() {
    private lateinit var profileImg: CircleImageView
    private lateinit var txtName: TextView
    private lateinit var txtTitle: TextView
    private lateinit var txtLocation: TextView
    private lateinit var itemSearchInput: TextView
    private lateinit var userEmail: TextView
    private lateinit var profileLink: TextView
    private lateinit var connectBtn: CardView
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var backBtn: ImageView



    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_user)
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        database = FirebaseDatabase.getInstance().reference

        // Get Data From Adapter
        val userModel = intent.getParcelableExtra<UserModel>("user_data")

        txtName = findViewById(R.id.txt_name)
        txtTitle = findViewById(R.id.txt_headline)
        txtLocation = findViewById(R.id.txt_location)
        profileImg = findViewById(R.id.profileImg)
        itemSearchInput = findViewById(R.id.item_search_input)
        connectBtn = findViewById(R.id.connectBtn)
        userEmail = findViewById(R.id.user_email)
        backBtn = findViewById(R.id.btn_back)
        profileLink = findViewById(R.id.profile_link)

        // Back Button
        backBtn.setOnClickListener { finish() }

        //Set Values
        if (userModel != null) {
            itemSearchInput.text = userModel.username
        }
        if (userModel != null) {
            txtName.text = userModel.username
        }
        if (userModel != null) {
            txtLocation.text = userModel.location
        }
        if (userModel != null) {
            userEmail.text = userModel.emailAddress
        }
        if (userModel != null) {
            txtTitle.text = userModel.headline
        }
        if (userModel != null) {
            profileLink.text = "https://www.linkedin.com/in/${userModel.username}-a785i1b7/"
        }
        if (userModel != null) {
            Glide.with(this).load(userModel.imageUrl).into(profileImg)
        }

        // Send Connection Request
        connectBtn.setOnClickListener {
            if (userModel != null) {
                userModel.key?.let { it1 -> database.child(USER_CONSTANT).child(it1).child(REQUEST).child(user.uid).setValue(true) }
            }
            connectBtn.setCardBackgroundColor(ContextCompat.getColor(this, R.color.gray))
            connectBtn.isEnabled = false
        }


    }
}
