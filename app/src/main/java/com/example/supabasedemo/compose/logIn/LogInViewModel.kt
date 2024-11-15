package com.example.supabasedemo.compose.logIn

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.supabasedemo.data.network.SupabaseClient.client
import com.example.supabasedemo.utils.SharedPreferenceHelper
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class LogInViewModel : ViewModel() {
    private val _userState = mutableStateOf<LogInUserState>(LogInUserState.Loading)
    val userState: State<LogInUserState> = _userState

    fun signUp(context: Context, userEmail: String, userPassword: String, username: String) {
        viewModelScope.launch {
            try {
                _userState.value = LogInUserState.Loading
                val result = client.auth.signUpWith(Email) {
                    email = userEmail
                    password = userPassword
                    data = buildJsonObject {
                        put("email", userEmail)
                        put("username", username)
                    }
                }
                saveToken(context)
                _userState.value = LogInUserState.Success("Registered successfully!")
            } catch(e: Exception) {
                _userState.value = LogInUserState.Error(e.message ?: "")
            }
        }
    }

    private fun saveToken(context: Context) {
        viewModelScope.launch {
            val accessToken = client.auth.currentAccessTokenOrNull()
            val sharedPref = SharedPreferenceHelper(context)
            sharedPref.saveStringData("accessToken",accessToken)
        }
    }

    private fun getToken(context: Context): String? {
        val sharedPref = SharedPreferenceHelper(context)
        return sharedPref.getStringData("accessToken")
    }

    fun login(context: Context, userEmail: String, userPassword: String) {
        viewModelScope.launch {
            try {
                _userState.value = LogInUserState.Loading
                val result = client.auth.signInWith(Email) {
                    email = userEmail
                    password = userPassword
                }
                saveToken(context)
                _userState.value = LogInUserState.Success("Logged in successfully!")
            } catch (e: Exception) {
                _userState.value = LogInUserState.Error(e.message ?: "")
            }

        }
    }

    fun logout(context: Context) {
        val sharedPref = SharedPreferenceHelper(context)
        viewModelScope.launch {
            try {
                _userState.value = LogInUserState.Loading
                client.auth.signOut()
                sharedPref.clearPreferences()
                _userState.value = LogInUserState.Success("Logged out successfully!")
            } catch (e: Exception) {
                _userState.value = LogInUserState.Error(e.message ?: "")
            }
        }
    }

    fun isUserLoggedIn(context: Context) {
        viewModelScope.launch {
            try {
                _userState.value = LogInUserState.Loading
                val token = getToken(context)
                if(token.isNullOrEmpty()) {
                    _userState.value = LogInUserState.Success("User not logged in!")
                } else {
                    client.auth.retrieveUser(token)
                    client.auth.refreshCurrentSession()
                    saveToken(context)
                    _userState.value = LogInUserState.Success("User already logged in!")
                }
            } catch (e: RestException) {
                _userState.value = LogInUserState.Error(e.error)
            }
        }
    }

    fun getCurrentUser() : String {
        val user = client.auth.currentUserOrNull()
        val metadata = user?.userMetadata
        return metadata.toString()
    }

    fun moveOn() {
        _userState.value = LogInUserState.MovedOn
    }
}