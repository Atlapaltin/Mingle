package com.iksanova.mingle.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.iksanova.mingle.R
import com.iksanova.mingle.models.UserModel
import com.iksanova.mingle.ui.home.HomeActivity
import com.iksanova.mingle.ui.location.LocationActivity

class JoinNowActivity : AppCompatActivity() {

    private lateinit var continueBtn: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_now)
        auth = FirebaseAuth.getInstance()

        continueBtn = findViewById(R.id.continue_btn)
        continueBtn.setOnClickListener { signIn() }
        databaseReference = FirebaseDatabase.getInstance().reference.child("Users")
    }

    private fun signIn() {
        val email = findViewById<EditText>(R.id.edit_email).text.toString()
        val password = findViewById<EditText>(R.id.edit_password).text.toString()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.hasChild(auth.currentUser!!.uid)) {
                                startActivity(Intent(this@JoinNowActivity, HomeActivity::class.java))
                            } else {
                                val model = UserModel(
                                    emailAddress = email,
                                    imageUrl = null,
                                    username = null,
                                    key = auth.currentUser!!.uid,
                                    token = null,
                                    location = null,
                                    headline = null,
                                    about = null,
                                )
                                databaseReference.child(auth.currentUser!!.uid).child("Info")
                                    .setValue(model)
                                    .addOnCompleteListener { startActivity(Intent(this@JoinNowActivity, LocationActivity::class.java)) }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
                } else {
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

