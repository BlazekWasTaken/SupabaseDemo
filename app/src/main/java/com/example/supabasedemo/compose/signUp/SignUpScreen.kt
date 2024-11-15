package com.example.supabasedemo.compose.signUp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.supabasedemo.LoadingComponent
import com.example.supabasedemo.compose.logIn.LogInUserState
import com.example.supabasedemo.ui.theme.Black
import com.example.supabasedemo.ui.theme.GreyblueButton
import com.example.supabasedemo.ui.theme.LightblueButton
import com.example.supabasedemo.ui.theme.Typography

@Composable
fun Screen(
    viewModel: SignUpViewModel = viewModel(),
    onNavigateTo: () -> Unit //TODO: change parameter name
) {
    val context = LocalContext.current
    val userState by viewModel.userState

    var userEmail by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }

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
            colors = TextFieldDefaults.colors(focusedLabelColor = GreyblueButton),
            shape = RectangleShape,
            value = userEmail,
            textStyle = Typography.bodyLarge,
            placeholder = {
                Text(
                    style = Typography.bodyLarge,
                    text = "Enter email")
            },
            onValueChange = {
                userEmail = it
            })
        Spacer(modifier = Modifier.padding(8.dp))
        OutlinedTextField(
            colors = TextFieldDefaults.colors(focusedLabelColor = GreyblueButton),
            shape = RectangleShape,
            value = username,
            textStyle = Typography.bodyLarge,
            placeholder = {
                Text(
                    style = Typography.bodyLarge,
                    text = "Enter username")
            },
            onValueChange = {
                username = it
            })
        Spacer(modifier = Modifier.padding(8.dp))
        OutlinedTextField(
            colors = TextFieldDefaults.colors(focusedLabelColor = GreyblueButton),
            shape = RectangleShape,
            value = userPassword,
            textStyle = Typography.bodyLarge,
            placeholder = {
                Text(
                    style = Typography.bodyLarge,
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
            shape = RectangleShape,
            border = BorderStroke(2.dp, Black),
            colors = ButtonDefaults.buttonColors(containerColor = LightblueButton),
            onClick = {
                viewModel.signUp(
                    context,
                    userEmail,
                    userPassword,
                    username
                )
            }) {

            Text(
                style = Typography.bodyLarge,
                text = "Sign Up")
        }

        when (userState) {
            is SignUpUserState.Loading -> {
                LoadingComponent()
            }
            is SignUpUserState.Success -> {
                //onNavigateTo()
                //viewModel.moveOn()
                val message = (userState as SignUpUserState.Success).message
                currentUserState = message
            }
            is SignUpUserState.Error -> {
                val message = (userState as SignUpUserState.Error).message
                currentUserState = message
            }
            is SignUpUserState.MovedOn -> { }
        }

        if (currentUserState.isNotEmpty()) {
            Text(
                style = Typography.bodyLarge,
                text = currentUserState)
        }
    }
}