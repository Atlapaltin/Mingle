package com.iksanova.mingle.ui.login

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
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
    private lateinit var btnSignIn: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        viewPager = findViewById(R.id.viewPager)
        dotsLayout = findViewById(R.id.dots)
        //Call Adapter
        appDescriptionSliderAdapter = AppDescriptionSliderAdapter(this)
        viewPager.adapter = appDescriptionSliderAdapter
        addDots(0)
        viewPager.addOnPageChangeListener(changeListener)
        btnSignIn = findViewById(R.id.btn_signIn)
        btnSignIn.setOnClickListener { signInWithEmail() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun addDots(position: Int) {
        dots = arrayOfNulls<TextView>(3) as? Array<TextView> ?: emptyArray()
        dotsLayout.removeAllViews()
        for (i in dots.indices) {
            dots[i] = TextView(this)
            dots[i].text = HtmlCompat.fromHtml("&#8226", HtmlCompat.FROM_HTML_MODE_LEGACY)
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

    private fun signInWithEmail() {
        val emailEditText = findViewById<EditText>(R.id.edit_email)
        val passwordEditText = findViewById<EditText>(R.id.edit_password)
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    databaseReference = FirebaseDatabase.getInstance().reference.child("Users")
                    databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.hasChild(auth.currentUser!!.uid)) {
                                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                            } else {
                                startActivity(Intent(this@LoginActivity, LocationActivity::class.java))
                            }
                            finish()
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
            finish()
        }
    }

    override fun loggedIn() {}
    override fun fileDownloaded(file: File) {}
    override fun cancelled() {}
    override fun handleError(exception: Exception) {}
}

