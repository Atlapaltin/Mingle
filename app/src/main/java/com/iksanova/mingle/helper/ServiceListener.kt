package com.iksanova.mingle.helper

import com.google.gson.JsonElement
import java.io.File

interface ServiceListener {
    fun loggedIn() //1
    fun fileDownloaded(file: File) //2
    fun cancelled() //3
    fun handleError(exception: Exception) //4
    interface ServiceListener {
        fun authenticationRequest(email: String, password: String): JsonElement?
    }

}
