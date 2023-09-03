package com.iksanova.mingle.ui.location

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.iksanova.mingle.R
import com.iksanova.mingle.base.BaseActivity
import com.iksanova.mingle.constants.Constants
import com.iksanova.mingle.ui.home.HomeActivity

class LocationActivity : BaseActivity() {
    private lateinit var editRegion: EditText
    private lateinit var editHeadline: EditText
    private lateinit var continueBtn: FrameLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        editRegion = findViewById(R.id.editRegion)
        editHeadline = findViewById(R.id.edit_headline)
        continueBtn = findViewById(R.id.continue_btn)
        auth = FirebaseAuth.getInstance()
        ref = FirebaseDatabase.getInstance().reference.child(Constants.USER_CONSTANT)

        // Continue Button
        continueBtn.setOnClickListener {
            val map: MutableMap<String, Any> = HashMap()
            map["location"] = editRegion.text.toString()
            map["headline"] = editHeadline.text.toString()

            ref.child(auth.currentUser!!.uid).child(Constants.INFO).updateChildren(map)
                .addOnCompleteListener {
                    startActivity(Intent(this@LocationActivity, HomeActivity::class.java))
                    finish()
                }
        }
    }
}
