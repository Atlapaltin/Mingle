package com.iksanova.mingle.ui.login

import android.content.Intent
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.viewpager.widget.ViewPager
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.iksanova.mingle.R
import com.iksanova.mingle.adapters.AppDescriptionSliderAdapter
import com.iksanova.mingle.helper.ServiceListener
import com.iksanova.mingle.ui.home.HomeActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.IOException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

class LoginActivity : AppCompatActivity(), ServiceListener {
    private lateinit var viewPager: ViewPager
    private lateinit var dotsLayout: LinearLayout
    private lateinit var appDescriptionSliderAdapter: AppDescriptionSliderAdapter
    private lateinit var dots: Array<TextView>
    private lateinit var btnSignIn: TextView
    private lateinit var accessToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
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

        val authenticationRequest = authenticationRequest<Any>(email, password)
        val gson = Gson()
        val requestBody = gson.toJson(authenticationRequest)

        val request = Request.Builder()
            .url("https://netomedia.ru/api/users/")
            .post(RequestBody.create("application/json".toMediaTypeOrNull(), requestBody))
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val token = gson.fromJson(responseBody, MediaSessionCompat.Token::class.java)
                if (response.isSuccessful && token != null) {
                    accessToken = token.token.toString()
                    runOnUiThread {
                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        finish()
                    }
                } else {
                    val error = gson.fromJson(responseBody, Error::class.java)
                    val errorMessage = if (!error.cause.isNullOrEmpty()) error.cause else "Authentication failed"
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }


        })
    }

    override fun onStart() {
        super.onStart()
        if (accessToken.isNotEmpty()) {
            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
            finish()
        }
    }

    override fun loggedIn() {}
    override fun fileDownloaded(file: File) {}
    override fun cancelled() {}
    override fun handleError(exception: Exception) {}
    private fun <T> authenticationRequest(
        email: String,
        password: String
    ): JsonElement? {
        val gson = Gson()
        val jsonObject = JsonObject()
        jsonObject.addProperty("username", email)
        jsonObject.addProperty("password", password)
        jsonObject.addProperty("rememberMe", true)
        val json = gson.toJson(jsonObject)
        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json)
        val request = Request.Builder()
            .url("https://netomedia.ru/api/users/")
            .post(requestBody)
            .build()
        val response = OkHttpClient().newCall(request).execute()
        return gson.fromJson(response.body?.string(), JsonElement::class.java)
    }


}



