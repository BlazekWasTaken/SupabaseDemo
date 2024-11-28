package com.example.supabasedemo.compose.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.supabasedemo.compose.viewModels.MainViewModel
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.MyOutlinedButton

@Composable
fun SettingsScreen(
    onNavigateToMainMenu: () -> Unit,
    onNavigateToAccountInfo: () -> Unit,
    onNavigateToThemeChoice: () -> Unit,
    onNavigateToDemo: () -> Unit,
    getState: () -> MutableState<UserState>,
    setState: (state: UserState) -> Unit
){
    LaunchedEffect(Unit) {
        setState(UserState.InSettings)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        MyOutlinedButton(
            onClick = {
                setState(UserState.InAccountInfo)
            }) {
            Text(text = "Account Info")
        }
        Spacer(modifier = Modifier.padding(8.dp))
        MyOutlinedButton(
            onClick = {
                setState(UserState.InThemeChoice)
            }) {
            Text(text = "Theme choice")
        }
        Spacer(modifier = Modifier.padding(8.dp))
        MyOutlinedButton(
            onClick = {
                setState(UserState.InDemo)
            }) {
            Text(text = "Demo")
        }
        Spacer(modifier = Modifier.padding(8.dp))
        MyOutlinedButton(
            onClick = {
                setState(UserState.InMainMenu)
            }) {
            Text(text = "Back to Main Menu")
        }

        //TODO: add everywhere going a page back
        BackHandler {
            setState(UserState.InMainMenu)
        }

        val userState = getState().value
        when (userState) {
            is UserState.InMainMenu -> {
                LaunchedEffect(Unit) {
                    onNavigateToMainMenu()
                }
            }
            is UserState.InAccountInfo -> {
                LaunchedEffect(Unit) {
                    onNavigateToAccountInfo()
                }
            }
            is UserState.InThemeChoice -> {
                LaunchedEffect(Unit) {
                    onNavigateToThemeChoice()
                }
            }
            is UserState.InDemo -> {
                LaunchedEffect(Unit) {
                    onNavigateToDemo()
                }
            }
            else -> {}
        }

    }
}