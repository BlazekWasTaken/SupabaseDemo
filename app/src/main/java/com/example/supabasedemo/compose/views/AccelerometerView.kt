package com.example.supabasedemo.compose.views

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
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
import kotlin.time.TimeSource

@Composable
fun AccelerometerView(
    context: Context,
    setAccelerometer: (reading: Reading) -> Unit
) {
    var acceleration by remember { mutableStateOf(Acceleration(0F, 0F, 0F)) }
    val timeSource = TimeSource.Monotonic
    var lastMark = timeSource.markNow()

    var sensorManager: SensorManager
    LaunchedEffect(Unit) {
        sensorManager = getSystemService(context, SensorManager::class.java) as SensorManager
        val linearAccelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null) {
                    acceleration = Acceleration(event.values[0], event.values[1], event.values[2])
                    val nowMark = timeSource.markNow()
                    val diff = nowMark - lastMark
                    lastMark = nowMark

                    Log.e("accelerometer", "accelerometer :) ${acceleration.x} ${acceleration.y} ${acceleration.z} ${diff.inWholeMilliseconds}ms")
                    setAccelerometer(Reading(event.values[0], event.values[1], event.values[2]))
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }

        sensorManager.registerListener(
            sensorEventListener,
            linearAccelerometer,
            100000
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
            Text(text = "ACCELEROMETER")
            Text(text = "x: " + acceleration.x.fixForScreen())
            Text(text = "y: " + acceleration.y.fixForScreen())
            Text(text = "z: " + acceleration.z.fixForScreen())
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

private class Acceleration(
    var x: Float,
    var y: Float,
    var z: Float
)