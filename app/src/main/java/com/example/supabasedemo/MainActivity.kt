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
import com.example.supabasedemo.compose.screens.SignupScreen
import com.example.supabasedemo.compose.screens.UwbScreen
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.AppTheme
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    private val permissionRequestCode = 101

    private val _userState = mutableStateOf<UserState>(UserState.InLoginChoice)
    private val activity = this

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
                        onNavigateToDemo = {
                            navController.navigate(route = Demo)
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
                composable<Demo> {
                    UwbScreen(
                        getState = {
                            return@UwbScreen _userState
                        },
                        setState = {
                            setState(it)
                        },
                        activity
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
    object GameStart
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

        val permissions =
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.INTERNET,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.UWB_RANGING,
                Manifest.permission.NEARBY_WIFI_DEVICES
            )

        for (i in permissions.indices) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permissions[i]),
                permissionRequestCode + i
            )
        }


    }
}