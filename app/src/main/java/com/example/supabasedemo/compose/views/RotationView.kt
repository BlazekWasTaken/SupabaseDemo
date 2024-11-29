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
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2

@Composable
fun RotationView(
    context: Context
) {
    var rotation by remember { mutableStateOf(Rotation(0F, 0F, 0F)) }

    var sensorManager: SensorManager
    LaunchedEffect(Unit) {
        sensorManager = getSystemService(context, SensorManager::class.java) as SensorManager
        val rotationSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null) {
                    val rotationMatrix = FloatArray(9)

                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                    val orientationAngles = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)

                    rotation = Rotation(orientationAngles[0] * -57.2958F, orientationAngles[1] * -57.2958F, orientationAngles[2] * -57.2958F)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }

        sensorManager.registerListener(
            sensorEventListener,
            rotationSensor,
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
            Log.e("rotation", "rotation :) ${rotation.x} ${rotation.y} ${rotation.z}")
            Text(text = "ROTATION")
            Text(text = "azimuth: " + rotation.x.fixForScreen())
            Text(text = "pitch: " + rotation.y.fixForScreen())
            Text(text = "roll: " + rotation.z.fixForScreen())
        }
    }
}

private fun Float.fixForScreen(): String {
    return if (this < 0) {
        String.format(Locale.getDefault(), "%.0f", this) + "\u00B0"
    }
    else {
        " " + String.format(Locale.getDefault(), "%.0f", this) + "\u00B0"
    }
}

private class Rotation(
    var x: Float,
    var y: Float,
    var z: Float,
)