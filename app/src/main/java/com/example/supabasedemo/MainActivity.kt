package com.example.supabasedemo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.supabasedemo.compose.screens.ChoiceScreen
import com.example.supabasedemo.compose.screens.CreateGameScreen
import com.example.supabasedemo.compose.screens.LoginScreen
import com.example.supabasedemo.compose.screens.MainMenuScreen
import com.example.supabasedemo.compose.screens.MinigameScreen
import com.example.supabasedemo.compose.screens.SignupScreen
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.AppTheme
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    private val CAMERA_PERMISSION_REQUEST_CODE = 101

    private val _userState = mutableStateOf<UserState>(UserState.InLoginChoice)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                Surface {
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
                composable<LoginChoice> {
                    ChoiceScreen(
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
                    )
                }
                composable<Login> {
                    LoginScreen(
                        onNavigateToMainMenu = {
                            navController.navigate(route = MainMenu)
                        },
                        getState = {
                            return@LoginScreen _userState
                        },
                        setState = {
                            setState(it)
                        }
                    )
                }
                composable<Signup> {
                    SignupScreen(
                        onNavigateToLoginChoice = {
                            navController.navigate(route = LoginChoice)
                        },
                        getState = {
                            return@SignupScreen _userState
                        },
                        setState = {
                            setState(it)
                        }
                    )
                }
            }
            navigation<MainMenu>(startDestination = Menu) {
                composable<Menu> {
                    MainMenuScreen(
                        onNavigateToLoginChoice = {
                            navController.navigate(route = LoginChoice)
                        },
                        onNavigateToGame = {
                            navController.navigate(route = Game)
                        },
                        onNavigateToMinigame = {
                            navController.navigate(route = Minigame)
                        },
                        getState = {
                            return@MainMenuScreen _userState
                        },
                        setState = {
                            setState(it)
                        }
                    )
                }
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
            navigation<Game>(startDestination = GameStart) {
                composable<GameStart> {
                    CreateGameScreen(
                        getState = {
                            return@CreateGameScreen _userState
                        },
                        setState = {
                            setState(it)
                        }
                    )
                }
            }

            navigation<Minigame>(startDestination = MinigameStart) {
                composable<MinigameStart> {
                    MinigameScreen (
                        getState = {
                            return@MinigameScreen _userState
                        },
                        setState = {
                            setState(it)
                        }
                    )
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
    object Minigame
    @Serializable
    object GameStart
    @Serializable
    object MinigameStart
    // endregion

    private fun NavOptionsBuilder.popUpToTop(navController: NavController) {
        popUpTo(navController.currentBackStackEntry?.destination?.route ?: return) {
            inclusive = true
        }
    }

    private fun setState(state: UserState) {
        _userState.value = state
        Toast.makeText(this, _userState.value.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }
}