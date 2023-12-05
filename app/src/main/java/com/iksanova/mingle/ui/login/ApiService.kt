package com.iksanova.mingle.ui.login

import android.support.v4.media.session.MediaSessionCompat
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Field

interface ApiService {
    @POST("registration")
    fun registerUser(
        @Field("login") email: String,
        @Field("password") password: String

    ): Call<Token>
}

