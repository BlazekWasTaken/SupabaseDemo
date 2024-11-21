package com.example.supabasedemo.data.model

import kotlinx.serialization.Serializable

@Serializable
sealed class UserState {
    data object InLoginChoice: UserState()
    data object ChoseLogin: UserState()
    data object ChoseSignup: UserState()

    data object LoginOrSignupLoading: UserState()
    data class LoginOrSignupFailed(val message: String): UserState()
    data class LoginOrSignupSucceeded(val message: String): UserState()

    data object LogoutLoading: UserState()
    data class LogoutSucceeded(val message: String): UserState()
    data class LogoutFailed(val message: String): UserState()

    data object CheckingLoginStatus: UserState()
    data class CheckedLoginStatusSucceeded(val message: String): UserState()
    data class CheckedLoginStatusFailed(val message: String): UserState()

    data object InMainMenu: UserState()
    data object ChoseLogout: UserState()
    data object ChoseGame: UserState()
    data object ChoseStats: UserState()
    data object ChoseTutorial: UserState()

    data object InSettings: UserState()
    data object ChoseAccountInfo: UserState()
    data object ChoseSounds: UserState()
    data object ChoseTheme: UserState()
    data object ChoseDemo: UserState()
}