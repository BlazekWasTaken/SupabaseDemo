package com.example.supabasedemo.compose.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.MyOutlinedButton
import com.example.supabasedemo.ui.theme.ThemeChoice

@Composable
fun StatsScreen(
    onNavigateToMainMenu: () -> Unit,
    getState: () -> MutableState<UserState>,
    setState: (state: UserState) -> Unit
) {
    LaunchedEffect(Unit) {
        setState(UserState.InStats)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Nothing yet")
        Spacer(modifier = Modifier.padding(8.dp))
        MyOutlinedButton(
            onClick = {
                setState(UserState.InMainMenu)
            }) {
            Text(text = "Back to Main Menu")
        }
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

        else -> {}
    }
}
