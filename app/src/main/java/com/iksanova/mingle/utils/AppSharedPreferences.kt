package com.iksanova.mingle.utils

import android.content.Context
import android.content.SharedPreferences

class AppSharedPreferences(context: Context) {
    private val sharedPreference: SharedPreferences = context.getSharedPreferences("Login_ID", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreference.edit()
    var userName: String?
        set(value) {
            editor.putString(USERNAME_KEY, value)
            editor.apply()
        }
        get() = sharedPreference.getString(USERNAME_KEY, null)

    var imgUrl: String?
        set(value) {
            editor.putString(IMG_URL_KEY, value)
            editor.apply()
        }
        get() = sharedPreference.getString(IMG_URL_KEY, null)

    companion object {
        private const val USERNAME_KEY = "username"
        private const val IMG_URL_KEY = "imgUrl"
    }
}
