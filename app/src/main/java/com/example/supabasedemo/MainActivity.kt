package com.example.supabasedemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.supabasedemo.ui.theme.SupabaseDemoTheme
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SupabaseDemoTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Choice
                ) {
                    composable<Test> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "TEST")
                        }
                    }

                    composable<Choice> { com.example.supabasedemo.compose.logInChoice.Screen(
                        onNavigateToLogIn = {
                            navController.navigate(route = LogIn)
                        },
                        onNavigateToSignUp = {
                            navController.navigate(route = SignUp)
                        }
                    ) }
                    composable<LogIn> { com.example.supabasedemo.compose.logIn.Screen(
                        onNavigateTo = {
                            navController.navigate(route = Test)
                        }
                    ) }
                    composable<SignUp> { com.example.supabasedemo.compose.signUp.Screen(
                        onNavigateTo = {
                            navController.navigate(route = Test)
                        }
                    ) }
                }
            }
        }
    }

    @Serializable
    object Choice
    @Serializable
    object LogIn
    @Serializable
    object SignUp
    @Serializable
    object Test
}