package com.iksanova.mingle.helper

import com.google.gson.JsonElement
import java.io.File

interface ServiceListener {
    fun loggedIn(isLoggedIn: Boolean)
    fun authenticationRequest(email: String, password: String): JsonElement?

}
