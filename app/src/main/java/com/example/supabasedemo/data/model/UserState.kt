package com.example.supabasedemo.data.model

sealed class UserState {
    object ChoosingLoginOrSignin: UserState()
    object ChoseLogin: UserState()
    object ChoseSignin: UserState()
    object LoginFailed: UserState()
    object LoginSucceeded: UserState()
    object SigninFailed: UserState()
    object SigninSucceeded: UserState()
}