package com.example.supabasedemo.compose.signUp

import com.example.supabasedemo.compose.logIn.LogInUserState

sealed class SignUpUserState {
    object Loading: SignUpUserState()
    data class Success(val message: String): SignUpUserState()
    data class Error(val message: String): SignUpUserState()
    object MovedOn: SignUpUserState()
}
