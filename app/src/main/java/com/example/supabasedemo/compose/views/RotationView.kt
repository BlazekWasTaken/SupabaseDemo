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

@Composable
fun RotationView(
    context: Context,
    setCompass: (reading: Reading) -> Unit
) {
    val sensorManager: SensorManager = getSystemService(context, SensorManager::class.java) as SensorManager

    val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    var magnetometerReading by remember { mutableStateOf(Reading(0F, 0F, 0F)) }
    var accelerometerReading by remember { mutableStateOf(Reading(0F, 0F, 0F)) }
    var rotationReading by remember { mutableStateOf(Reading(0F, 0F, 0F)) }

    LaunchedEffect(Unit) {
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    magnetometerReading = Reading(event.values[0], event.values[1], event.values[2])
                }
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    accelerometerReading = Reading(event.values[0], event.values[1], event.values[2])
                }

                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrix(
                    rotationMatrix,
                    null,
                    floatArrayOf(
                        accelerometerReading.x,
                        accelerometerReading.y,
                        accelerometerReading.z
                    ),
                    floatArrayOf(
                        magnetometerReading.x,
                        magnetometerReading.y,
                        magnetometerReading.z
                    )
                )

                val orientationAngles = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)

                rotationReading = Reading(
                    if (orientationAngles[0] * 57 >= 0)
                        orientationAngles[0] * 57
                    else
                        360 + (orientationAngles[0] * 57),
                    orientationAngles[1] * 57,
                    orientationAngles[2] * 57
                )
                setCompass(rotationReading)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }
        }

        sensorManager.registerListener(
            sensorEventListener,
            magnetometer,
            SensorManager.SENSOR_DELAY_UI
        )
        sensorManager.registerListener(
            sensorEventListener,
            accelerometer,
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
//            Log.e("rotation", "rotation :) ${rotationReading.x} ${rotationReading.y} ${rotationReading.z}")
            Text(text = "ROTATION")
            Text(text = "azimuth: " + rotationReading.x.fixForScreen())
            Text(text = "pitch: " + rotationReading.y.fixForScreen())
            Text(text = "roll: " + rotationReading.z.fixForScreen())
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

class Reading(
    val x: Float,
    val y: Float,
    val z: Float,
)