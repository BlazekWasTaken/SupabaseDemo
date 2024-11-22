package com.example.supabasedemo.compose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.supabasedemo.compose.viewModels.MainViewModel
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.MyOutlinedButton

@Composable
fun ChoiceScreen(
    onNavigateToLogIn: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    getState: () -> MutableState<UserState>,
    setState: (state: UserState) -> Unit
) {
    val viewModel = MainViewModel(LocalContext.current, setState = { setState(it) })
//    val userState by remember { getState() }

    LaunchedEffect(Unit) {
        setState(UserState.InLoginChoice)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyOutlinedButton(
            onClick = {
                setState(UserState.ChoseLogin)
            }) {
            Text(text = "Log In")
        }
        Spacer(modifier = Modifier.padding(8.dp))
        MyOutlinedButton(
            onClick = {
                setState(UserState.ChoseSignup)
            }) {
            Text(text = "Sign Up")
        }
    }

    when (getState().value) {
        is UserState.ChoseLogin -> {
            LaunchedEffect(Unit) {
                onNavigateToLogIn()
            }
        }
        is UserState.ChoseSignup -> {
            LaunchedEffect(Unit) {
                onNavigateToSignUp()
            }
        }
        else -> {

        }
    }
}
