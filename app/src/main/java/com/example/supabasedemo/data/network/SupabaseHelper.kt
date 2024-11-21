package com.example.supabasedemo.data.network

import android.content.Context
import androidx.compose.runtime.MutableState
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.data.network.SupabaseClient.client
import com.example.supabasedemo.utils.SharedPreferenceHelper
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseHelper(
    private val scope: CoroutineScope,
    val setState: (UserState) -> Unit,
    private val context: Context) {
    fun signUp(userEmail: String, userPassword: String, username: String) {
        scope.launch {
            try {
                setState(UserState.LoginOrSignupLoading)
                client.auth.signUpWith(Email) {
                    email = userEmail
                    password = userPassword
                    data = buildJsonObject {
                        put("email", userEmail)
                        put("username", username)
                    }
                }
                saveToken()
                setState(UserState.LoginOrSignupSucceeded("Registered successfully!"))
            } catch(e: Exception) {
                setState(UserState.LoginOrSignupFailed(e.message ?: ""))
            }
        }
    }
    private fun saveToken() {
        scope.launch {
            val accessToken = client.auth.currentAccessTokenOrNull()
            val sharedPref = SharedPreferenceHelper(context)
            sharedPref.saveStringData("accessToken",accessToken)
        }
    }
    private fun getToken(): String? {
        val sharedPref = SharedPreferenceHelper(context)
        return sharedPref.getStringData("accessToken")
    }
    fun login(userEmail: String, userPassword: String) {
        scope.launch {
            try {
                setState(UserState.LoginOrSignupLoading)
                client.auth.signInWith(Email) {
                    email = userEmail
                    password = userPassword
                }
                saveToken()
                setState(UserState.LoginOrSignupSucceeded("Logged in successfully!"))
            } catch (e: Exception) {
                setState(UserState.LoginOrSignupFailed(e.message ?: ""))
            }

        }
    }
    fun logout() {
        val sharedPref = SharedPreferenceHelper(context)
        scope.launch {
            try {
                setState(UserState.LogoutLoading)
                client.auth.signOut()
                sharedPref.clearPreferences()
                setState(UserState.LogoutSucceeded("Logged out successfully!"))
            } catch (e: Exception) {
                setState(UserState.LogoutFailed(e.message ?: ""))
            }
        }
    }
    fun isUserLoggedIn() {
        scope.launch {
            try {
                setState(UserState.CheckingLoginStatus)
                val token = getToken()
                if(token.isNullOrEmpty()) {
                    setState(UserState.CheckedLoginStatusSucceeded("User not logged in!"))
                } else {
                    client.auth.retrieveUser(token)
                    client.auth.refreshCurrentSession()
                    saveToken()
                    setState(UserState.CheckedLoginStatusSucceeded("User already logged in!"))
                }
            } catch (e: RestException) {
                setState(UserState.CheckedLoginStatusFailed(e.error))
            }
        }
    }
}