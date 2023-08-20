package com.iksanova.mingle.utils

import android.content.Context
import android.content.SharedPreferences

class AppSharedPreferences(context: Context) {
    private val sharedPreference: SharedPreferences = context.getSharedPreferences("Login_ID", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreference.edit()

    companion object {
        private const val USERNAME_KEY = "username"
        private const val IMG_URL_KEY = "imgUrl"
    }

    fun setUsername(username: String) {
        editor.putString(USERNAME_KEY, username)
        editor.apply()
    }

    fun getUserName(): String? {
        return sharedPreference.getString(USERNAME_KEY, null)
    }

    fun setImgUrl(imgUrl: String) {
        editor.putString(IMG_URL_KEY, imgUrl)
        editor.apply()
    }

    fun getImgUrl(): String? {
        return sharedPreference.getString(IMG_URL_KEY, null)
    }
}
