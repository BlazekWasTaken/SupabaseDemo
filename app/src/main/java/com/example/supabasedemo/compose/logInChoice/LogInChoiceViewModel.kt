package com.example.supabasedemo.compose.logInChoice

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

class LogInChoiceViewModel : ViewModel() {
    private val _userState = mutableStateOf<LogInChoiceUserState>(LogInChoiceUserState.Loading)
    val userState: State<LogInChoiceUserState> = _userState

    fun chooseLogIn() {
        _userState.value = LogInChoiceUserState.LogIn
    }

    fun chooseSignUp() {
        _userState.value = LogInChoiceUserState.SignUp
    }

    fun moveOn() {
        _userState.value = LogInChoiceUserState.MovedOn
    }
}