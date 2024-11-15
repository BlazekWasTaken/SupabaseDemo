package com.example.supabasedemo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
    fun LoggedInScreen(onCreateGameClick: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to the Game!")
            Spacer(modifier = Modifier.padding(8.dp))
            Button(onClick = onCreateGameClick) {
                Text("Create a Game")
            }
        }
    }

    @Composable
    fun CreateGameScreen(onGameCreated: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Create a New Game")
            Spacer(modifier = Modifier.padding(8.dp))
            Button(onClick = {
                // Add your game creation logic here
                onGameCreated()
            }) {
                Text("Generate QR Code")
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

    // region game
    @Composable
    fun MainScreen(viewModel: SupabaseAuthViewModel = viewModel()) {
        val context = LocalContext.current
        val userState by viewModel.userState

        var userEmail by remember { mutableStateOf("") }
        var userPassword by remember { mutableStateOf("") }
        var username by remember { mutableStateOf("") }
        var macAddress by remember { mutableStateOf("") }

        var currentUserState by remember { mutableStateOf("") }

        var navigateToCreateGame by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            viewModel.isUserLoggedIn(
                context,
            )
        }

        if (navigateToCreateGame) {
            CreateGameScreen(onGameCreated = {
                navigateToCreateGame = false
            })
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = userEmail,
                    placeholder = {
                        Text(text = "Enter email")
                    },
                    onValueChange = {
                        userEmail = it
                    })
                Spacer(modifier = Modifier.padding(8.dp))
                TextField(
                    value = username,
                    placeholder = {
                        Text(text = "Enter username")
                    },
                    onValueChange = {
                        username = it
                    })
                Spacer(modifier = Modifier.padding(8.dp))
                TextField(
                    value = macAddress,
                    placeholder = {
                        Text(text = "mac address (will remove tomorrow)")
                    },
                    onValueChange = {
                        macAddress = it
                    }
                )
                Spacer(modifier = Modifier.padding(8.dp))
                TextField(
                    value = userPassword,
                    placeholder = {
                        Text(text = "Enter password")
                    },
                    onValueChange = {
                        userPassword = it
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Button(onClick = {
                    viewModel.signUp(
                        context,
                        userEmail,
                        userPassword,
                        username,
                        macAddress
                    )
                }) {
                    Text(text = "Sign Up")
                }

                Button(onClick = {
                    viewModel.login(
                        context,
                        userEmail,
                        userPassword,
                    )
                }) {
                    Text(text = "Login")
                }

                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = {
                        viewModel.logout(context)
                    }) {
                    Text(text = "Logout")
                }

                when (userState) {
                    is UserState.Loading -> {
                        LoadingComponent()
                    }

                    is UserState.Success -> {
                        val message = (userState as UserState.Success).message
                        currentUserState = message
                    }

                    is UserState.LoggedIn -> {
                        LoggedInScreen(onCreateGameClick = {
                            navigateToCreateGame = true
                        })
                    }

                    is UserState.Error -> {
                        val message = (userState as UserState.Error).message
                        currentUserState = message
                    }
                }

                if (currentUserState.isNotEmpty()) {
                    Text(text = currentUserState)
                }
            }
        }
    }
    //endregion
}