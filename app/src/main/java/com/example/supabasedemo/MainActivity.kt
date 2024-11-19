package com.example.supabasedemo

import android.icu.text.ListFormatter.Width
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment.Companion.Rectangle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.AppTheme
import com.example.supabasedemo.ui.theme.Black
import com.example.supabasedemo.ui.theme.DarkBlue
import com.example.supabasedemo.ui.theme.GreyBlue
import com.example.supabasedemo.ui.theme.LightBlue
import com.example.supabasedemo.ui.theme.SupabaseDemoTheme
import com.example.supabasedemo.ui.theme.Typography

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    MainScreen()
                }
            }
        }
    }

    @Composable
    fun MainScreen(viewModel: SupabaseAuthViewModel = viewModel()) {
        val context = LocalContext.current
        val userState by viewModel.userState

        var userEmail by remember { mutableStateOf("") }
        var userPassword by remember { mutableStateOf("") }
        var username by remember { mutableStateOf("") }
        var macAddress by remember { mutableStateOf("") }

        var currentUserState by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            viewModel.isUserLoggedIn(
                context,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = userEmail,
                placeholder = {
                    Text(
                        text = "Enter email")
                },
                onValueChange = {
                    userEmail = it
                })
            Spacer(modifier = Modifier.padding(8.dp))
            OutlinedTextField(
                value = username,
                placeholder = {
                    Text(
                        text = "Enter username")
                },
                onValueChange = {
                    username = it
                })
            Spacer(modifier = Modifier.padding(8.dp))
            OutlinedTextField(
                value = userPassword,
                placeholder = {
                    Text(
                        text = "Enter password")
                },
                onValueChange = {
                    userPassword = it
                },
                visualTransformation =  PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
            Spacer(modifier = Modifier.padding(8.dp))
            OutlinedButton(
                onClick = {
                viewModel.signUp(
                    context,
                    userEmail,
                    userPassword,
                    username,
                    macAddress
                )
            }) {

                Text(
                    text = "Sign Up")
            }

            OutlinedButton(
                onClick = {
                viewModel.login(
                    context,
                    userEmail,
                    userPassword,
                )
            }) {
                Text(
                    text = "Login"
                )
            }

            OutlinedButton(
                onClick = {
                    viewModel.logout(context)
                }) {
                Text(
                    text = "Logout")
            }

            when (userState) {
                is UserState.Loading -> {
                    LoadingComponent()
                }

                is UserState.Success -> {
                    val message = (userState as UserState.Success).message
                    currentUserState = message
                }

                is UserState.Error -> {
                    val message = (userState as UserState.Error).message
                    currentUserState = message
                }
            }

            if (currentUserState.isNotEmpty()) {
                Text(
                    text = currentUserState)
            }
        }
    }
}