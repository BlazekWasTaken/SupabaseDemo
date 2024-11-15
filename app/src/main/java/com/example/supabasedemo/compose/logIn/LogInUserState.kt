package com.example.supabasedemo.compose.logIn

sealed class LogInUserState {
    object Loading: LogInUserState()
    data class Success(val message: String): LogInUserState()
    data class Error(val message: String): LogInUserState()
    object MovedOn: LogInUserState()
}
