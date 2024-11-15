package com.example.supabasedemo.compose.logInChoice

sealed class LogInChoiceUserState {
    object Loading: LogInChoiceUserState()
    object LogIn: LogInChoiceUserState()
    object SignUp: LogInChoiceUserState()
    data class Error(val message: String): LogInChoiceUserState()
    object MovedOn: LogInChoiceUserState()
}