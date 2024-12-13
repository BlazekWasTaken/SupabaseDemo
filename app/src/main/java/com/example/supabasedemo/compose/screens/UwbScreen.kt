package com.example.supabasedemo.compose.screens

import UwbManagerSingleton
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.uwb.RangingParameters
import androidx.core.uwb.RangingResult.RangingResultPeerDisconnected
import androidx.core.uwb.RangingResult.RangingResultPosition
import androidx.core.uwb.UwbAddress
import androidx.core.uwb.UwbClientSessionScope
import androidx.core.uwb.UwbComplexChannel
import androidx.core.uwb.UwbControleeSessionScope
import androidx.core.uwb.UwbControllerSessionScope
import androidx.core.uwb.UwbDevice
import androidx.core.uwb.UwbManager
import com.example.supabasedemo.compose.viewModels.MainViewModel
import com.example.supabasedemo.compose.views.AccelerometerView
import com.example.supabasedemo.compose.views.GyroscopeView
import com.example.supabasedemo.compose.views.RotationView
import com.example.supabasedemo.compose.views.UwbDataView
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.AppTheme
import com.example.supabasedemo.ui.theme.MyOutlinedButton
import com.example.supabasedemo.ui.theme.MyOutlinedTextField
import com.google.common.primitives.Shorts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun UwbScreen(
    onNavigateToMainMenu: () -> Unit,
    getState: () -> MutableState<UserState>,
    setState: (state: UserState) -> Unit
) {
    LaunchedEffect(Unit) {
        setState(UserState.InDemo)
    }

    val context = LocalContext.current
    val viewModel = MainViewModel(context, setState = { setState(it) })

    var isController by remember { mutableStateOf(true) }

    val isStarted by UwbManagerSingleton.isStartedFlow.collectAsState(initial = false)
    var address by remember { mutableStateOf("") }
    var preamble by remember { mutableStateOf("") }
    val deviceAddress by UwbManagerSingleton.address.collectAsState(initial = "-1")
    val devicePreamble by UwbManagerSingleton.preamble.collectAsState(initial = "-1")

    var permissionGranted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        UwbManagerSingleton.initialize(context, isController)

        permissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.UWB_RANGING
        ) == PackageManager.PERMISSION_GRANTED
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(
                context as Activity, arrayOf(Manifest.permission.UWB_RANGING), 101
            )
        }

        UwbManagerSingleton.fetchDeviceDetails()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "Controller:")
            Spacer(modifier = Modifier.padding(8.dp))
            Switch(checked = isController, onCheckedChange = {
                isController = it
                UwbManagerSingleton.setRoleAsController(it, context)
                UwbManagerSingleton.stopSession()
            })
        }

        MyOutlinedTextField(
            value = address,
            onValueChange = { address = it },
            placeholder = { Text(text = "Enter Partner Address") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        if (!isController) {
            Spacer(modifier = Modifier.padding(8.dp))
            MyOutlinedTextField(
                value = preamble,
                onValueChange = { preamble = it },
                placeholder = { Text(text = "Enter Preamble Value") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.padding(8.dp))

        Text(text = "Your Device Address: $deviceAddress")
        if (isController) {
            Text(text = "Your Preamble: $devicePreamble")
        }

        Spacer(modifier = Modifier.padding(8.dp))

        Spacer(modifier = Modifier.padding(8.dp))
        if (!isStarted) {
            MyOutlinedButton(onClick = {
                if (address.isNotBlank()) {
                    if (isController) {
                        UwbManagerSingleton.startSession(address, "0")
                    } else {
                        UwbManagerSingleton.startSession(address, preamble)
                    }
                }
            }) {
                Text(text = "Start")
            }
        } else {
            MyOutlinedButton(onClick = {
                UwbManagerSingleton.stopSession()
            }) {
                Text(text = "Stop")
            }
        }

        Spacer(modifier = Modifier.padding(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            UwbDataView()
            Spacer(modifier = Modifier.padding(8.dp))
            GyroscopeView(context)
        }

        Spacer(modifier = Modifier.padding(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AccelerometerView(context)
            Spacer(modifier = Modifier.padding(8.dp))
            RotationView(context)
        }

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
}
fun List<Float>.stDev(): Double {
    var result = 0.0
    for (value in this) {
        result += (value - this.average()).pow(2)
    }
    result /= this.count()
    result = sqrt(result)
    return result
}

fun List<Float>.between(lowInclusive: Double, highInclusive: Double): List<Double> {
    val result: ArrayList<Double> = ArrayList()
    for (value in this) {
        if (value in lowInclusive..highInclusive) result.add(value.toDouble())
    }
    return result
}