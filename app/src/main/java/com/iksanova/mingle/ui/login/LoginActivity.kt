package com.iksanova.mingle.ui.login

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
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
import com.iksanova.mingle.adapters.AppDescriptionSliderAdapter
import com.iksanova.mingle.base.BaseActivity
import com.iksanova.mingle.constants.Constants
import com.iksanova.mingle.helper.ServiceListener
import com.iksanova.mingle.models.UserModel
import com.iksanova.mingle.ui.home.HomeActivity
import com.iksanova.mingle.ui.location.LocationActivity
import java.io.File

class LoginActivity : BaseActivity(), ServiceListener {
    private lateinit var viewPager: ViewPager
    private lateinit var dotsLayout: LinearLayout
    private lateinit var appDescriptionSliderAdapter: AppDescriptionSliderAdapter
    private lateinit var dots: Array<TextView>
    private lateinit var oneTapClient: SignInClient
    private val REQ_ONE_TAP = 1
    private val TAG = "LoginActivity"
    private lateinit var btnSignIn: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        //Hooks
        viewPager = findViewById(R.id.viewPager)
        dotsLayout = findViewById(R.id.dots)

        //Call Adapter
        appDescriptionSliderAdapter = AppDescriptionSliderAdapter(this)
        viewPager.adapter = appDescriptionSliderAdapter

        //Dots
        addDots(0)
        viewPager.addOnPageChangeListener(changeListener)
        btnSignIn = findViewById(R.id.btn_signIn)
        btnSignIn.setOnClickListener { startActivity(Intent(this@LoginActivity, JoinNowActivity::class.java)) }

        //Function
        OneTapLogin()
    }

    private fun addDots(position: Int) {
        dots = arrayOfNulls<TextView>(3) as Array<TextView>
        dotsLayout.removeAllViews()
        for (i in dots.indices) {
            dots[i] = TextView(this)
            dots[i].text = Html.fromHtml("&#8226")
            dots[i].textSize = 45f
            dotsLayout.addView(dots[i])
        }
        if (dots.isNotEmpty()) {
            dots[position].setTextColor(ContextCompat.getColor(this, R.color.black))
        }
    }

    private val changeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

        override fun onPageSelected(position: Int) {
            addDots(position)
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }

    private fun OneTapLogin() {
        oneTapClient = Identity.getSignInClient(this)
        val signInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(
                BeginSignInRequest.PasswordRequestOptions.builder()
                    .setSupported(true)
                    .build()
            )
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.default_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            // Automatically sign in when exactly one credential is retrieved.
            .build()

        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this) { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP,
                        null, 0, 0, 0
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "Couldn't start One Tap UI: " + e.localizedMessage)
                }
            }
            .addOnFailureListener(this) { e ->
                e.localizedMessage?.let { Log.d(TAG, it) }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_ONE_TAP) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken
                val username = credential.displayName
                val emailAddress = credential.id
                var imageUrl = credential.profilePictureUri.toString()
                imageUrl = imageUrl.substring(0, imageUrl.length - 5) + "s400-c"
                firebaseAuthWithGoogle(idToken, username, emailAddress, imageUrl)

                if (idToken != null) {
                    // Got an ID token from Google. Use it to authenticate
                    // with your backend.
                    Log.d(TAG, "Got ID token.")
                }
            } catch (e: ApiException) {
                when (e.statusCode) {
                    CommonStatusCodes.CANCELED -> {
                        Log.d(TAG, "One-tap dialog was closed.")
                        // Don't re-prompt the user.
                    }
                    CommonStatusCodes.NETWORK_ERROR -> {
                        Log.d(TAG, "One-tap encountered a network error.")
                        // Try again or just ignore.
                    }
                    else -> {
                        Log.d(TAG, "Couldn't get credential from result." + e.localizedMessage)
                    }
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?, username: String?, emailAddress: String?, finalImageUrl: String?) {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users")
        val authCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(authCredential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (!dataSnapshot.hasChild(auth.currentUser!!.uid)) {
                            val model = UserModel()
                            model.emailAddress = emailAddress
                            model.imageUrl = finalImageUrl
                            model.username = username
                            model.key = auth.currentUser!!.uid
                            databaseReference.child(auth.currentUser!!.uid).child(Constants.INFO)
                                .setValue(model)
                                .addOnCompleteListener { startActivity(Intent(this@LoginActivity, LocationActivity::class.java)); finish() }
                        } else {
                            startActivity(Intent(this@LoginActivity, HomeActivity::class.java)); finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInWithCustomToken:failure", task.exception)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            startActivity(Intent(this@LoginActivity, HomeActivity::class.java)); finish()
        }
    }

    override fun loggedIn() {}

    override fun fileDownloaded(file: File) {}

    override fun cancelled() {}

    override fun handleError(exception: Exception) {}
}
