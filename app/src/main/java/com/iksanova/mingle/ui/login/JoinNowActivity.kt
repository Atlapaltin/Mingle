package com.iksanova.mingle.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.iksanova.mingle.R
import com.iksanova.mingle.ui.home.HomeActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.databinding.DataBindingUtil
import com.iksanova.mingle.databinding.ActivityJoinNowBinding

class JoinNowActivity : AppCompatActivity() {

    private lateinit var continueBtn: Button
    private lateinit var apiService: ApiService
    private lateinit var binding: ActivityJoinNowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_join_now)
        apiService = RetrofitClient.getClient().create(ApiService::class.java)

        continueBtn = binding.continueBtn
        continueBtn.setOnClickListener { signIn() }
    }

    private fun signIn() {
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        apiService.registerUser(email, password).enqueue(object :
            Callback<Token> {
            override fun onResponse(call: Call<Token>, response: Response<Token>) {
                if (response.isSuccessful) {
                    // Save the token
                    val id = response.body()?.id
                    val token = response.body()?.token
                    saveTokenToSharedPreferences(id, token)

                    startActivity(Intent(this@JoinNowActivity, HomeActivity::class.java))
                } else {
                    val error = Gson().fromJson(response.errorBody()?.charStream(), Error::class.java)
                    Toast.makeText(this@JoinNowActivity, error.reason, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Token>, t: Throwable) {
                Toast.makeText(this@JoinNowActivity, "Registration failed.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveTokenToSharedPreferences(id: Long?, token: String?) {
        val sharedPreferences = getSharedPreferences("TOKEN_PREFS", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("id", id.toString())
        editor.putString("token", token)
        editor.apply()
    }

    data class Error(val reason: String)
}

