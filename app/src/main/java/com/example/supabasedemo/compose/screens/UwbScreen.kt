package com.example.supabasedemo.compose.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
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
import com.example.supabasedemo.compose.views.ArrowView
import com.example.supabasedemo.compose.views.GyroscopeView
import com.example.supabasedemo.compose.views.Reading
import com.example.supabasedemo.compose.views.RotationView
import com.example.supabasedemo.compose.views.UwbDataView
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.MyOutlinedButton
import com.example.supabasedemo.ui.theme.MyOutlinedTextField
import com.google.common.primitives.Shorts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
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

    val uwbManager = UwbManager.createInstance(context)
    var clientSessionScope: UwbClientSessionScope? by remember { mutableStateOf(null) }

    var isController by remember { mutableStateOf(true) }
    var isStarted by remember { mutableStateOf(false) }
    var address by remember { mutableStateOf("") }
    var preamble by remember { mutableStateOf("") }

    var distance by remember { mutableDoubleStateOf(-1.0) }
    var azimuth by remember { mutableDoubleStateOf(-1.0) }

    val distances = remember { mutableStateListOf<Float>() }
    val azimuths = remember { mutableStateListOf<Float>() }

    var permissionGranted by remember { mutableStateOf(false) }

    var isFront by remember { mutableStateOf(true) }

    var accelerometerReadings = remember { mutableStateListOf<Reading>() }
    var gyroscopeReadings = remember { mutableStateListOf<Reading>() }
    var compassReadings = remember { mutableStateListOf<Reading>() }

    LaunchedEffect(Unit) {
         permissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.UWB_RANGING
        ) == PackageManager.PERMISSION_GRANTED
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.UWB_RANGING),
                101
            )
        }
    }

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            clientSessionScope = if (isController) {
                uwbManager.controllerSessionScope()
            } else {
                uwbManager.controleeSessionScope()
            }
        }
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
            Switch(
                checked = isController,
                onCheckedChange = {
                    isController = it
                    isStarted = false
                    clientSessionScope = null
                    CoroutineScope(Dispatchers.Main.immediate).launch {
                        clientSessionScope = if (isController) {
                            uwbManager.controllerSessionScope()
                        } else {
                            uwbManager.controleeSessionScope()
                        }
                    }
                }
            )
        }
        if (clientSessionScope != null) {
            Spacer(modifier = Modifier.padding(8.dp))
            MyOutlinedTextField(
                value = address,
                placeholder = {
                    Text(text = "Enter address")
                },
                onValueChange = {
                    address = it
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )
            if (isController) {
                val controller = clientSessionScope as UwbControllerSessionScope
                val content = "address: ${Shorts.fromByteArray(controller.localAddress.address)} \n" +
                        "preamble: ${controller.uwbComplexChannel.preambleIndex}"
                Spacer(modifier = Modifier.padding(8.dp))
                Text(text = content)
            }
            else {
                Spacer(modifier = Modifier.padding(8.dp))
                MyOutlinedTextField(
                    value = preamble,
                    placeholder = {
                        Text(text = "Enter preamble")
                    },
                    onValueChange = {
                        preamble = it
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
                val controller = clientSessionScope as UwbControleeSessionScope
                val content = "address: ${Shorts.fromByteArray(controller.localAddress.address)}"
                Spacer(modifier = Modifier.padding(8.dp))
                Text(text = content)
            }
            if (!isStarted) {
                Spacer(modifier = Modifier.padding(8.dp))
                MyOutlinedButton(
                    onClick = {
                        val partnerAddress = UwbAddress(Shorts.toByteArray(address.toShort()))
                        val uwbComplexChannel: UwbComplexChannel?

                        val sessionScope: UwbClientSessionScope?
                        if (isController) {
                            sessionScope = (clientSessionScope as UwbControllerSessionScope)
                            uwbComplexChannel = sessionScope.uwbComplexChannel
                        }
                        else {
                            sessionScope = (clientSessionScope as UwbControleeSessionScope)
                            uwbComplexChannel = UwbComplexChannel(9, preamble.toInt())
                        }
                        val rangingParameters = RangingParameters(
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
                                            var distCalc = -1.0
                                            if (distances.count() < 20) {
                                                distances.add(it.position.distance?.value ?: -1F)
                                                accelerometerReadings.clear()
                                                gyroscopeReadings.clear()
                                                return@collect
                                            }
                                            else if (distances.count() == 20) {
                                                distances.removeAt(0)
                                                distances.add(it.position.distance?.value ?: -1F)

                                                val avg = distances.average()
                                                val stDev = distances.stDev()

                                                distCalc = distances.between(
                                                    avg - stDev,
                                                    avg + stDev).average()
                                            }
                                            var azCalc = -1.0
                                            if (azimuths.count() < 20) {
                                                azimuths.add(it.position.azimuth?.value ?: -1F)
                                                accelerometerReadings.clear()
                                                gyroscopeReadings.clear()
                                                return@collect
                                            }
                                            else if (azimuths.count() == 20) {
                                                azimuths.removeAt(0)
                                                azimuths.add(it.position.azimuth?.value ?: -1F)

                                                val avg = azimuths.average()
                                                val stDev = azimuths.stDev()

                                                azCalc = azimuths.between(
                                                    avg - stDev,
                                                    avg + stDev).average()
                                            }
                                            distance = distCalc

//                                            if (isFront){
                                                azCalc = if (azCalc < 0) -azCalc
                                                else 360 - azCalc
//                                            } else {
//                                                azCalc += 180
//                                            }
                                            azimuth = azCalc

                                            viewModel.supabaseDb.sendReadingToDb(
                                                distance = distance,
                                                angle = azimuth,
                                                stDev = azimuths.stDev(),
                                                accelerometer = accelerometerReadings.toList(),
                                                gyroscope = gyroscopeReadings.toList(),
//                                                compass = compassReadings.toList(),
                                                isFront = isFront
                                            )

                                            accelerometerReadings.clear()
                                            gyroscopeReadings.clear()
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            UwbDataView(
                getDistance = {
                    return@UwbDataView distance
                },
                getAzimuth = {
                    return@UwbDataView azimuth
                },
                getStDev = {
                    return@UwbDataView azimuths.stDev()
//                    return@UwbDataView -1.0
                }
            )
            Spacer(modifier = Modifier.padding(8.dp))
            GyroscopeView(
                context,
                setGyroscope = {
                    gyroscopeReadings.add(it)
                }
            )
        }
        Spacer(modifier = Modifier.padding(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AccelerometerView(
                context,
                setAccelerometer = {
                    accelerometerReadings.add(it)
                }
            )
            Spacer(modifier = Modifier.padding(8.dp))
            RotationView(
                context,
                setCompass = {
                    compassReadings.add(it)
                }
            )
        }
        Spacer(modifier = Modifier.padding(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            ArrowView(getAz = {
                azimuth
            })
            Spacer(modifier = Modifier.padding(8.dp))
            MyOutlinedButton(
                onClick = {
                    isFront = !isFront
                }
            ) {
                Text("is front: $isFront")
            }
        }
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