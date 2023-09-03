package com.iksanova.mingle.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.core.content.ContextCompat.startActivity
import com.iksanova.mingle.R
import com.iksanova.mingle.base.BaseActivity
import com.iksanova.mingle.ui.login.LoginActivity
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var mHandler: Handler
    private lateinit var mRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        mRunnable = Runnable {
            val intent = Intent(this@SplashScreenActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        mHandler = Handler()
        mHandler.postDelayed(mRunnable, 1000)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}