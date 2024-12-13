package com.example.supabasedemo.compose.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.supabasedemo.compose.viewModels.MainViewModel
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.AppTheme
import com.example.supabasedemo.ui.theme.MyOutlinedButton

@Composable
fun AccountInfoScreen(
    onNavigateToSettings: () -> Unit,
    getState: () -> MutableState<UserState>,
    setState: (state: UserState) -> Unit
){
    LaunchedEffect(Unit) {
        setState(UserState.InAccountInfo)
    }

    val viewModel = MainViewModel(LocalContext.current, setState = { setState(it) })
    val userEmail = viewModel.supabaseAuth.getCurrentUserInfo().email.toString()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            text = "E-mail:"
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Box(modifier = Modifier
            .border(1.dp, AppTheme.colorScheme.outlineVariant, RectangleShape)
            .padding(4.dp)
        ){
            Text(text = userEmail)
        }
        Spacer(modifier = Modifier.padding(8.dp))
        MyOutlinedButton(
            onClick = {
                setState(UserState.InSettings)
            }) {
            Text(text = "Back to Settings")
        }

        BackHandler {
            setState(UserState.InSettings)
        }

        val userState = getState().value
        when (userState) {
            is UserState.InSettings -> {
                LaunchedEffect(Unit) {
                    onNavigateToSettings()
                }
            }
            else -> {}
        }
    }
}