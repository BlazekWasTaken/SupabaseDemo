package com.example.supabasedemo.compose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.supabasedemo.LoadingComponent
import com.example.supabasedemo.compose.viewModels.MainViewModel
import com.example.supabasedemo.data.model.UserState

@Composable
fun LoginScreen(
    onNavigateToMainMenu: () -> Unit,
    getState: () -> MutableState<UserState>,
    setState: (state: UserState) -> Unit
) {
    val viewModel = MainViewModel(LocalContext.current, setState = { setState(it) })
//    val userState by remember { getState() }

    var userEmail by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    var currentUserState by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        setState(UserState.ChoseLogin)
        viewModel.supabase.isUserLoggedIn()
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
                Text(text = "Enter email")
            },
            onValueChange = {
                userEmail = it
            }
        )
        Spacer(modifier = Modifier.padding(8.dp))
        OutlinedTextField(
            value = userPassword,
            placeholder = {
                Text(text = "Enter password")
            },
            onValueChange = {
                userPassword = it
            },
            visualTransformation =  PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )
        Spacer(modifier = Modifier.padding(8.dp))
        OutlinedButton(onClick = {
            viewModel.supabase.login(
                userEmail,
                userPassword
            )
        }) {
            Text(text = "Log in")
        }

        val userState = getState().value
        when (userState) {
            is UserState.LoginOrSignupLoading -> {
                LoadingComponent()
            }
            is UserState.LoginOrSignupSucceeded -> {
                val message = userState.message
                currentUserState = message
            }
            is UserState.LoginOrSignupFailed -> {
                val message = userState.message
                currentUserState = message
            }
            is UserState.CheckedLoginStatusSucceeded -> {
                val message = userState.message
                currentUserState = message
            }
            else -> {

            }
        }

        if (currentUserState.isNotEmpty()) {
            Text(text = currentUserState)
        }

        if (getState().value is UserState.LoginOrSignupSucceeded) {
            LaunchedEffect(Unit) {
                onNavigateToMainMenu()
            }
        }
    }
}