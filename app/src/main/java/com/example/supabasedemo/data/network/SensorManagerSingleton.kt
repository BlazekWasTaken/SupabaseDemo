package com.example.supabasedemo.data.network

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.ContextCompat.getSystemService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

object SensorManagerSingleton {
    private var sensorManager: SensorManager? = null

    private var isStarted: Boolean = false
    private val _isStartedFlow = MutableStateFlow(false)

    private val _linearAccelerometerReadingsFlow = MutableStateFlow(listOf(Reading(0F, 0F, 0F)))
    val linearAccelerometerReadingsFlow: StateFlow<List<Reading>> get() = _linearAccelerometerReadingsFlow

    private val _accelerometerReadingsFlow = MutableStateFlow(listOf(Reading(0F, 0F, 0F)))
    val accelerometerReadingsFlow: StateFlow<List<Reading>> get() = _accelerometerReadingsFlow

    private val _gyroscopeReadingsFlow = MutableStateFlow(listOf(Reading(0F, 0F, 0F)))
    val gyroscopeReadingsFlow: StateFlow<List<Reading>> get() = _gyroscopeReadingsFlow

    private val _magnetometerReadingsFlow = MutableStateFlow(listOf(Reading(0F, 0F, 0F)))
    val magnetometerReadingsFlow: StateFlow<List<Reading>> get() = _magnetometerReadingsFlow

    private var initializationDeferred: CompletableDeferred<Unit>? = null

    fun initialize(context: Context) {
        if (isStarted || _isStartedFlow.value) return
        sensorManager = getSystemService(context, SensorManager::class.java) as SensorManager
        initializationDeferred = CompletableDeferred()

        try {
            sensorManager!!.registerLinearAccelerometer()
            sensorManager!!.registerAccelerometer()
            sensorManager!!.registerGyroscope()
            sensorManager!!.registerMagnetometer()

            initializationDeferred?.complete(Unit)
            isStarted = true
            _isStartedFlow.value = true
        } catch (e: Exception) {
            initializationDeferred?.completeExceptionally(e)
        }

    }

    private suspend fun waitForInitialization() {
        initializationDeferred?.await()
    }

    private fun SensorManager.registerLinearAccelerometer() {
        if (sensorManager == null) throw Exception()
        val linearAccelerometer: Sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) as Sensor

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val reading = Reading(event.values[0], event.values[1], event.values[2])
                _linearAccelerometerReadingsFlow.value += reading
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }
        this.registerListener(
            sensorEventListener,
            linearAccelerometer,
            50000
        )
    }

    private fun SensorManager.registerAccelerometer() {
        if (sensorManager == null) throw Exception()
        val accelerometer: Sensor? = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val reading = Reading(event.values[0], event.values[1], event.values[2])
                _accelerometerReadingsFlow.value += reading
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }
        this.registerListener(
            sensorEventListener,
            accelerometer,
            50000
        )
    }

    private fun SensorManager.registerGyroscope() {
        if (sensorManager == null) throw Exception()
        val gyroscope: Sensor? = sensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val reading = Reading(event.values[0], event.values[1], event.values[2])
                _gyroscopeReadingsFlow.value += reading
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }
        this.registerListener(
            sensorEventListener,
            gyroscope,
            50000
        )
    }

    private fun SensorManager.registerMagnetometer() {
        if (sensorManager == null) throw Exception()
        val magnetometer: Sensor? = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val reading = Reading(event.values[0], event.values[1], event.values[2])
                _magnetometerReadingsFlow.value += reading
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }
        this.registerListener(
            sensorEventListener,
            magnetometer,
            50000
        )
    }
}

class Reading(
    val x: Float,
    val y: Float,
    val z: Float,
)

fun Float.fixForScreen(): String {
    return if (this < 0) {
        String.format(Locale.getDefault(), "%.3f", this)
    }
    else {
        " " + String.format(Locale.getDefault(), "%.3f", this)
    }
}