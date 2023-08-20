package com.iksanova.mingle.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.iksanova.mingle.R
import com.iksanova.mingle.base.BaseActivity
import com.iksanova.mingle.models.UserModel
import com.iksanova.mingle.ui.home.HomeActivity
import com.iksanova.mingle.ui.location.LocationActivity

class JoinNowActivity : AppCompatActivity() {

    private lateinit var googleBtn: RelativeLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var client: GoogleSignInClient
    private val Rc_Sign_in = 1
    private val TAG = "JoinActivity"
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_now)
        auth = FirebaseAuth.getInstance()

        googleBtn = findViewById(R.id.card_google_btn)
        googleBtn.setOnClickListener { googlesignIn() }
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users")

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestIdToken(getString(R.string.default_web_client_id))
            .build()

        client = GoogleSignIn.getClient(this, gso)
    }

    private fun googlesignIn() {
        val intent = client.signInIntent
        startActivityForResult(intent, Rc_Sign_in)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Rc_Sign_in) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val email = account.email
                val username = account.displayName
                var imageUrl = account.photoUrl.toString()
                imageUrl = imageUrl.substring(0, imageUrl.length - 5) + "s400-c"

                firebaseAuthWithGoogle(account.idToken, username, email, imageUrl)
            } catch (e: ApiException) {
                Toast.makeText(this, "Signin Error", Toast.LENGTH_LONG).show()
                Log.w("Error", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(
        idToken: String?,
        username: String?,
        emailAddress: String?,
        finalImageUrl: String?
    ) {
        val authCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(authCredential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.hasChild(auth.currentUser!!.uid)) {
                                startActivity(Intent(this@JoinNowActivity, HomeActivity::class.java))
                            } else {
                                val model = UserModel()
                                model.emailAddress = emailAddress
                                model.imageUrl = finalImageUrl
                                model.username = username
                                model.key = auth.currentUser!!.uid
                                databaseReference.child(auth.currentUser!!.uid).child("Info")
                                    .setValue(model)
                                    .addOnCompleteListener { startActivity(Intent(this@JoinNowActivity, LocationActivity::class.java)) }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
                } else {
                    Log.w(TAG, "signInWithCustomToken:failure", task.exception)
                }
            }
    }
}