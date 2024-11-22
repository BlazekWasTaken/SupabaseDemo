package com.example.supabasedemo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.supabasedemo.compose.screens.ChoiceScreen
import com.example.supabasedemo.compose.screens.LoginScreen
import com.example.supabasedemo.compose.screens.MainMenuScreen
import com.example.supabasedemo.compose.screens.SignupScreen
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.AppTheme
import com.example.supabasedemo.ui.theme.SupabaseDemoTheme
import com.example.supabasedemo.ui.theme.Surface
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    private val _userState = mutableStateOf<UserState>(UserState.InLoginChoice)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                Surface() {
                    Navigation()
                }

            }
        }
    }

    @Composable
    private fun Navigation() {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = LoginProcess
        ) {
            navigation<LoginProcess>(startDestination = LoginChoice) {
                composable<LoginChoice> { ChoiceScreen(
                    onNavigateToLogIn = {
                        navController.navigate(route = Login)
                    },
                    onNavigateToSignUp = {
                        navController.navigate(route = Signup)
                    },
                    getState = {
                        return@ChoiceScreen _userState
                    },
                    setState = {
                        setState(it)
                    }
                ) }
                composable<Login> { LoginScreen(
                    onNavigateToMainMenu = {
                        navController.navigate(route = MainMenu)
                        {
                            popUpToTop(navController)
                        }
                    },
                    getState = {
                        return@LoginScreen _userState
                    },
                    setState = {
                        setState(it)
                    }
                ) }
                composable<Signup> { SignupScreen(
                    onNavigateToChoice = {
                        navController.navigate(route = LoginChoice) {
                            popUpToTop(navController)
                        }
                    },
                    getState = {
                        return@SignupScreen _userState
                    },
                    setState = {
                        setState(it)
                    }
                ) }
            }
            navigation<MainMenu>(startDestination = Menu) {
                composable<Menu> { MainMenuScreen(
                    getState = {
                        return@MainMenuScreen _userState
                    },
                    setState = {
                        setState(it)
                    }
                ) }
                composable<Stats> {

                }
                composable<Tutorial> {

                }
            }
            navigation<Settings>(startDestination = SettingsMenu) {
                composable<SettingsMenu> {

                }
                composable<AccountInfo> {

                }
                composable<Sounds> {

                }
                composable<Theme> {

                }
                composable<Demo> {

                }
            }
            navigation<Game>(startDestination = GameChoice) {
                composable<GameChoice> {

                }
            }
        }
    }

    // region objects
    @Serializable
    object LoginProcess
    @Serializable
    object LoginChoice
    @Serializable
    object Login
    @Serializable
    object Signup

    @Serializable
    object MainMenu
    @Serializable
    object Menu
    @Serializable
    object Stats
    @Serializable
    object Tutorial
    @Serializable
    object Settings
    @Serializable
    object SettingsMenu
    @Serializable
    object AccountInfo
    @Serializable
    object Sounds
    @Serializable
    object Theme
    @Serializable
    object Demo

    @Serializable
    object Game
    @Serializable
    object GameChoice
    // endregion

    private fun NavOptionsBuilder.popUpToTop(navController: NavController) {
        popUpTo(navController.currentBackStackEntry?.destination?.route ?: return) {
            inclusive =  true
        }
    }

    private fun setState(state: UserState) {
        _userState.value = state
        Toast.makeText(this, _userState.value.toString(), Toast.LENGTH_SHORT).show()
    }
}