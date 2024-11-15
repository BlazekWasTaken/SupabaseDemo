package com.example.supabasedemo.data.model

sealed class UserState {
    object Loading: UserState()
    data class Success(val message: String): UserState()
    data class LoggedIn(val message: String) : UserState()
    data class Error(val message: String): UserState()
}
