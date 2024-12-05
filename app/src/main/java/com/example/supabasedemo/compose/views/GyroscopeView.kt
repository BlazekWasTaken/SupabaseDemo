package com.example.supabasedemo.compose.views

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getSystemService
import com.example.supabasedemo.ui.theme.AppTheme
import java.util.Locale

@Composable
fun GyroscopeView(
    context: Context,
    setGyroscope: (reading: Reading) -> Unit
) {
    var gyroscope by remember { mutableStateOf(Gyroscope(0F, 0F, 0F)) }

    var sensorManager: SensorManager
    LaunchedEffect(Unit) {
        sensorManager = getSystemService(context, SensorManager::class.java) as SensorManager
        val gyroscopeSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null) {
                    gyroscope = Gyroscope(event.values[0], event.values[1], event.values[2])
                    setGyroscope(Reading(event.values[0], event.values[1], event.values[2]))
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }

        sensorManager.registerListener(
            sensorEventListener,
            gyroscopeSensor,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    Box(
        modifier = Modifier.border(1.dp, AppTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .padding(4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            Log.e("gyroscope", "gyroscope :) ${gyroscope.x} ${gyroscope.y} ${gyroscope.z}")
            Text(text = "GYROSCOPE")
            Text(text = "x: " + gyroscope.x.fixForScreen())
            Text(text = "y: " + gyroscope.y.fixForScreen())
            Text(text = "z: " + gyroscope.z.fixForScreen())
        }
    }
}

private fun Float.fixForScreen(): String {
    return if (this < 0) {
        String.format(Locale.getDefault(), "%.3f", this)
    }
    else {
        " " + String.format(Locale.getDefault(), "%.3f", this)
    }
}

private class Gyroscope(
    var x: Float,
    var y: Float,
    var z: Float
)