package com.example.supabasedemo.compose.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import androidx.lifecycle.viewModelScope
import com.example.supabasedemo.compose.viewModels.MainViewModel
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.MyOutlinedButton
import com.example.supabasedemo.ui.theme.MyOutlinedTextField
import com.google.common.primitives.Shorts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.pow
import kotlin.math.sqrt

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun UwbScreen(
    getState: () -> MutableState<UserState>,
    setState: (state: UserState) -> Unit,
    activity: Activity
) {
    val context = LocalContext.current
    val viewModel = MainViewModel(context, setState = { setState(it) })

    var uwbManager = UwbManager.createInstance(context)
    var clientSessionScope: UwbClientSessionScope? by remember { mutableStateOf(null) }

    var isController by remember { mutableStateOf(true) }
    var isStarted by remember { mutableStateOf(false) }
    var address by remember { mutableStateOf("") }
    var preamble by remember { mutableStateOf("") }

    var distance by remember { mutableFloatStateOf(-1F) }
    var azimuth by remember { mutableFloatStateOf(-1F) }
    var elevation by remember { mutableFloatStateOf(-1F) }

    var distances = remember { mutableStateListOf<Float>() }
    var azimuths = remember { mutableStateListOf<Float>() }
    var elevations = remember { mutableStateListOf<Float>() }

    val permissionGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.UWB_RANGING
    ) == PackageManager.PERMISSION_GRANTED

    if (!permissionGranted) {
        LaunchedEffect(context) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.CAMERA),
                101
            )
        }
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
                isController = true
                isStarted = false
                clientSessionScope = null
                CoroutineScope(Dispatchers.Main.immediate).launch {
                    clientSessionScope = uwbManager.controllerSessionScope()
                }
            }) {
            Text(text = "Set controller")
        }
        Spacer(modifier = Modifier.padding(8.dp))
        MyOutlinedButton(
            onClick = {
                isController = false
                isStarted = false
                clientSessionScope = null
                CoroutineScope(Dispatchers.Main.immediate).launch {
                    clientSessionScope = uwbManager.controleeSessionScope()
                }
            }) {
            Text(text = "Set controlee")
        }
        Spacer(modifier = Modifier.padding(8.dp))
        MyOutlinedTextField(
            value = address,
            placeholder = {
                Text(text = "Enter address")
            },
            onValueChange = {
                address = it
            }
        )

        if (!isController) {
            Spacer(modifier = Modifier.padding(8.dp))
            MyOutlinedTextField(
                value = preamble,
                placeholder = {
                    Text(text = "Enter preamble")
                },
                onValueChange = {
                    preamble = it
                }
            )
        }

        if (clientSessionScope != null) {
            if (isController) {
                val controller = clientSessionScope as UwbControllerSessionScope
                val content = "address: ${Shorts.fromByteArray(controller.localAddress.address)} \n" +
                        "preamble: ${controller.uwbComplexChannel.preambleIndex}"
                Spacer(modifier = Modifier.padding(8.dp))
                Text(text = content)
            }
            else {
                val controller = clientSessionScope as UwbControleeSessionScope
                val content = "address: ${Shorts.fromByteArray(controller.localAddress.address)}"
                Spacer(modifier = Modifier.padding(8.dp))
                Text(text = content)
            }

            if (!isStarted) {
                Spacer(modifier = Modifier.padding(8.dp))
                MyOutlinedButton(
                    onClick = {
                        val partnerAddress: UwbAddress = UwbAddress(Shorts.toByteArray(address.toShort()))
                        var uwbComplexChannel: UwbComplexChannel? = null

                        var sessionScope: UwbClientSessionScope? = null
                        if (isController) {
                            sessionScope = (clientSessionScope as UwbControllerSessionScope)
                            uwbComplexChannel = sessionScope.uwbComplexChannel
                        }
                        else {
                            sessionScope = (clientSessionScope as UwbControleeSessionScope)
                            uwbComplexChannel = UwbComplexChannel(9, preamble.toInt())
                        }

                        val rangingParameters: RangingParameters = RangingParameters(
                            uwbConfigType = RangingParameters.CONFIG_UNICAST_DS_TWR,
                            sessionId = 12345,
                            subSessionId = 0,
                            sessionKeyInfo = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
                            subSessionKeyInfo = null,
                            complexChannel = uwbComplexChannel,
                            peerDevices = listOf(UwbDevice(partnerAddress)),
                            updateRateType = RangingParameters.RANGING_UPDATE_RATE_AUTOMATIC
                        )

                        val sessionFlow = sessionScope.prepareSession(rangingParameters)

                        isStarted = true
                        CoroutineScope(Dispatchers.Main.immediate).launch {
                            try {
                                sessionFlow.collect {
                                    when(it) {
                                        is RangingResultPosition -> {
                                            var distCalc = -1F
                                            if (distances.count() < 20) {
                                                distances.add(it.position.distance?.value ?: -1F)
                                            }
                                            else if (distances.count() == 20) {
                                                distances.removeAt(0)
                                                distances.add(it.position.distance?.value ?: -1F)

                                                distCalc = distances.subList(5, 14).average().toFloat()
                                            }

                                            var azCalc = -1F
                                            if (azimuths.count() < 20) {
                                                azimuths.add(it.position.azimuth?.value ?: -1F)
                                            }
                                            else if (azimuths.count() == 20) {
                                                azimuths.removeAt(0)
                                                azimuths.add(it.position.azimuth?.value ?: -1F)

                                                azCalc = azimuths.subList(5, 14).average().toFloat()
                                            }

                                            var elCalc = -1F
                                            if (elevations.count() < 20) {
                                                elevations.add(it.position.elevation?.value ?: -1F)
                                            }
                                            else if (elevations.count() == 20) {
                                                elevations.removeAt(0)
                                                elevations.add(it.position.elevation?.value ?: -1F)

                                                elCalc = elevations.average().toFloat()
                                            }

                                            distance = distCalc
                                            azimuth = azCalc
                                            elevation = elCalc
                                            Log.e("uwb", "dist: $distance az: $azimuth el: $elevation")
                                        }
                                        is RangingResultPeerDisconnected -> {
                                            clientSessionScope = if (isController) {
                                                uwbManager.controllerSessionScope()
                                            }
                                            else {
                                                uwbManager.controleeSessionScope()
                                            }
                                            isStarted = false
                                            this.cancel()
                                        }
                                    }
                                }
                            }
                            catch (e: Exception) {
                                isStarted = false
                                Log.e("uwb", e.toString())
                            }
                        }
                    }) {
                    Text(text = "Start")
                }
            }

        }

        Spacer(modifier = Modifier.padding(8.dp))
        Text(text = "distance: $distance")
        Spacer(modifier = Modifier.padding(8.dp))
        Text(text = "azimuth: $azimuth")
        Spacer(modifier = Modifier.padding(8.dp))
        Text(text = "elevation: $elevation")
    }
}

fun List<Double>.stDev(): Double {
    var result: Double = 0.0
    for (value in this) {
        result += (value - this.average()).pow(2)
    }
    result /= this.count()
    result = sqrt(result)
    return result
}

fun List<Double>.between(lowInclusive: Double, highInclusive: Double): List<Double> {
    var result: ArrayList<Double> = ArrayList<Double>()
    for (value in this) {
        if (value >= lowInclusive && value <= highInclusive) result.add(value)
    }
    return result
}