package com.example.supabasedemo

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.data.network.SupabaseClient.client
import com.example.supabasedemo.utils.SharedPreferenceHelper
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseAuthViewModel : ViewModel() {
    private val _userState = mutableStateOf<UserState>(UserState.Loading)
    val userState: State<UserState> = _userState

    fun signUp(context: Context, userEmail: String, userPassword: String, username: String, macAddress: String) {
        viewModelScope.launch {
            try {
                _userState.value = UserState.Loading
                val result = client.auth.signUpWith(Email) {
                    email = userEmail
                    password = userPassword
                    data = buildJsonObject {
                        put("email", userEmail)
                        put("username", username)
                        put("mac", macAddress)
                    }
                }
                saveToken(context)
                _userState.value = UserState.Success("Registered successfully!")
            } catch(e: Exception) {
                _userState.value = UserState.Error(e.message ?: "")
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
                _userState.value = UserState.Loading
                val result = client.auth.signInWith(Email) {
                    email = userEmail
                    password = userPassword
                }
                saveToken(context)
                _userState.value = UserState.Success("Logged in successfully!")
            } catch (e: Exception) {
                _userState.value = UserState.Error(e.message ?: "")
            }

        }
    }

    fun logout(context: Context) {
        val sharedPref = SharedPreferenceHelper(context)
        viewModelScope.launch {
            try {
                _userState.value = UserState.Loading
                client.auth.signOut()
                sharedPref.clearPreferences()
                _userState.value = UserState.Success("Logged out successfully!")
            } catch (e: Exception) {
                _userState.value = UserState.Error(e.message ?: "")
            }
        }
    }

    fun isUserLoggedIn(context: Context) {
        viewModelScope.launch {
            try {
                _userState.value = UserState.Loading
                val token = getToken(context)
                if(token.isNullOrEmpty()) {
                    _userState.value = UserState.Success("User not logged in!")
                } else {
                    client.auth.retrieveUser(token)
                    client.auth.refreshCurrentSession()
                    saveToken(context)
                    _userState.value = UserState.Success("User already logged in!")
                }
            } catch (e: RestException) {
                _userState.value = UserState.Error(e.error)
            }
        }
    }

    fun getCurrentUser() : String {
        val user = client.auth.currentUserOrNull()
        val metadata = user?.userMetadata
        return metadata.toString()
    }
}