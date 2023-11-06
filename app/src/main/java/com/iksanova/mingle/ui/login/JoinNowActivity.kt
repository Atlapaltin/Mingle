package com.iksanova.mingle.ui.login

import android.content.Intent
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.iksanova.mingle.R
import com.iksanova.mingle.data.remote.ApiService
import com.iksanova.mingle.data.remote.RetrofitClient
import com.iksanova.mingle.ui.home.HomeActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JoinNowActivity : AppCompatActivity() {

    private lateinit var continueBtn: Button
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_now)
        apiService = RetrofitClient.getClient().create(ApiService::class.java)

        continueBtn = findViewById(R.id.continue_btn)
        continueBtn.setOnClickListener { signIn() }
    }

    private fun signIn() {
        val email = findViewById<EditText>(R.id.edit_email).text.toString()
        val password = findViewById<EditText>(R.id.edit_password).text.toString()

        apiService.registerUser(email, password).enqueue(object :
            Callback<MediaSessionCompat.Token> {
            override fun onResponse(call: Call<MediaSessionCompat.Token>, response: Response<MediaSessionCompat.Token>) {
                if (response.isSuccessful) {
                    val token = response.body()?.token
                    // Save the token to shared preferences or other storage
                    startActivity(Intent(this@JoinNowActivity, HomeActivity::class.java))
                } else {
                    val error = Gson().fromJson(response.errorBody()?.charStream(), Error::class.java)
                    Toast.makeText(this@JoinNowActivity, error.reason, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MediaSessionCompat.Token>, t: Throwable) {
                Toast.makeText(this@JoinNowActivity, "Registration failed.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

data class Error(val reason: String)
