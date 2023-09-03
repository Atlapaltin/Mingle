package com.iksanova.mingle.ui.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.iksanova.mingle.R
import com.iksanova.mingle.base.BaseActivity
import com.iksanova.mingle.constants.Constants.INFO
import com.iksanova.mingle.constants.Constants.USER_CONSTANT
import com.iksanova.mingle.models.UserModel
import de.hdodenhof.circleimageview.CircleImageView

class ProfileActivity : BaseActivity() {
    private lateinit var imgEditAbout: ImageView
    private lateinit var imgEditProfile: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var editAboutLayout: RelativeLayout
    private lateinit var model: UserModel
    private lateinit var name: TextView
    private lateinit var location: TextView
    private lateinit var aboutTxt: TextView
    private lateinit var headlineTxt: TextView
    private lateinit var profileImageView: CircleImageView
    private lateinit var connectionsTxt: TextView
    private lateinit var editTextAbout: EditText
    private lateinit var searchInput: EditText
    private lateinit var saveAboutBtn: CardView
    private lateinit var userRef: DatabaseReference
    private lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        user = FirebaseAuth.getInstance().currentUser!!
        userRef = FirebaseDatabase.getInstance().reference.child(USER_CONSTANT).child(user.uid)

        imgEditAbout = findViewById(R.id.img_edit)
        saveAboutBtn = findViewById(R.id.save_btn)
        editTextAbout = findViewById(R.id.about_edittext)
        aboutTxt = findViewById(R.id.aboutTxt)
        connectionsTxt = findViewById(R.id.connections)
        imgEditProfile = findViewById(R.id.edit_profile)
        editAboutLayout = findViewById(R.id.edit_about_layout)
        name = findViewById(R.id.txt_name)
        headlineTxt = findViewById(R.id.headlineTxt)
        location = findViewById(R.id.txt_location)
        profileImageView = findViewById(R.id.profileImg)
        btnBack = findViewById(R.id.btn_back)
        searchInput = findViewById(R.id.item_search_input)
        model = UserModel()

        // Back Button
        btnBack.setOnClickListener { onBackPressed() }

        //Get Data from Firebase
        userRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                model = snapshot.child(INFO).getValue(UserModel::class.java)!!
                name.text = model.username
                location.text = model.location
                headlineTxt.text = model.headline
                searchInput.setText(model.username)
                Glide.with(applicationContext).load(model.imageUrl).into(profileImageView)

                if (snapshot.child("Data").child("about").exists()) {
                    aboutTxt.text = snapshot.child("Data").child("about").getValue(String::class.java)
                    aboutTxt.setLines(3)
                } else {
                    aboutTxt.text = String.format("%s", "Add a summary about yourself")
                    aboutTxt.setLines(1)
                }
                connectionsTxt.text = "${snapshot.child("Connections").childrenCount} connections"
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // Save Button
        saveAboutBtn.setOnClickListener {
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userRef.child("Data").child("about").setValue(editTextAbout.text.toString())
                        .addOnCompleteListener { task ->
                            if (editAboutLayout.visibility == View.VISIBLE) {
                                startActivity(Intent(this@ProfileActivity, ProfileActivity::class.java))
                            }
                        }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        // Edit Profile
        imgEditAbout.setOnClickListener { editAboutLayout.visibility = View.VISIBLE }
        imgEditProfile.setOnClickListener {
            val intent = Intent(this@ProfileActivity, EditProfileIntroActivity::class.java)
            intent.putExtra("user_name", model.username)
            intent.putExtra("user_imgUrl", model.imageUrl)
            intent.putExtra("user_location", model.location)
            startActivity(intent)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        if (editAboutLayout.visibility == View.VISIBLE) {
            startActivity(Intent(this@ProfileActivity, ProfileActivity::class.java))
        }
    }
}
