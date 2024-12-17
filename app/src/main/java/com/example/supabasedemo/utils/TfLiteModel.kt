package com.example.supabasedemo.utils

import android.content.Context
import android.util.Log
import com.example.supabasedemo.data.network.Reading
import kotlinx.io.IOException
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

data class TfLiteModel(val context: Context) {
    private var interpreter: Interpreter? = null

    init {
        try {
            @Suppress("DEPRECATION")
            interpreter = Interpreter(loadModelFile(context))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun predict(
        distance: Float,
        angle: Float,
        stDev: Float,
        accReading: List<Reading>,
        gyrReading: List<Reading>
    ): Boolean {
        if (accReading.isEmpty() || gyrReading.isEmpty()) return true

        val displacement = calculateDisplacement(accReading, gyrReading)

        val input = floatArrayOf(
            distance,
            angle,
            stDev,
            displacement.linearDisplacementX,
            displacement.linearDisplacementY,
            displacement.linearDisplacementZ,
            displacement.angularDisplacementX,
            displacement.angularDisplacementY,
            displacement.angularDisplacementZ
        )

        val output = Array(1) {
            FloatArray(1)
        }

        interpreter?.run(input, output)
        Log.d("tensorflow",  "${output[0][0]}")
        return output[0][0] > 0.5
    }

    private fun calculateDisplacement(
        accelerometerReadings: List<Reading>,
        gyroscopeReadings: List<Reading>,
        dt: Float = 0.1f
    ): Displacement {
        val accX = accelerometerReadings.map {it.x}
        val accY = accelerometerReadings.map {it.y}
        val accZ = accelerometerReadings.map {it.z}

        val gyrX = gyroscopeReadings.map {it.x}
        val gyrY = gyroscopeReadings.map {it.y}
        val gyrZ = gyroscopeReadings.map {it.z}

        // Initialize velocity and displacement arrays
        val velocityX = MutableList(accX.size) { 0f }
        val velocityY = MutableList(accY.size) { 0f }
        val velocityZ = MutableList(accZ.size) { 0f }

        val linearDisplacementX = MutableList(accX.size) { 0f }
        val linearDisplacementY = MutableList(accY.size) { 0f }
        val linearDisplacementZ = MutableList(accZ.size) { 0f }

        val angularDisplacementX = MutableList(gyrX.size) { 0f }
        val angularDisplacementY = MutableList(gyrY.size) { 0f }
        val angularDisplacementZ = MutableList(gyrZ.size) { 0f }

        for (i in 1 until accX.size) {
            velocityX[i] = velocityX[i - 1] + 0.5f * (accX[i] + accX[i - 1]) * dt
            linearDisplacementX[i] = linearDisplacementX[i - 1] + 0.5f * (velocityX[i] + velocityX[i - 1]) * dt
        }

        for (i in 1 until accY.size) {
            velocityY[i] = velocityY[i - 1] + 0.5f * (accY[i] + accY[i - 1]) * dt
            linearDisplacementY[i] = linearDisplacementY[i - 1] + 0.5f * (velocityY[i] + velocityY[i - 1]) * dt
        }

        for (i in 1 until accZ.size) {
            velocityZ[i] = velocityZ[i - 1] + 0.5f * (accZ[i] + accZ[i - 1]) * dt
            linearDisplacementZ[i] = linearDisplacementZ[i - 1] + 0.5f * (velocityZ[i] + velocityZ[i - 1]) * dt
        }

        for (i in 1 until gyrX.size) {
            angularDisplacementX[i] = angularDisplacementX[i - 1] + 0.5f * (gyrX[i] + gyrX[i - 1]) * dt
        }

        for (i in 1 until gyrY.size) {
            angularDisplacementY[i] = angularDisplacementY[i - 1] + 0.5f * (gyrY[i] + gyrY[i - 1]) * dt
        }

        for (i in 1 until gyrZ.size) {
            angularDisplacementZ[i] = angularDisplacementZ[i - 1] + 0.5f * (gyrZ[i] + gyrZ[i - 1]) * dt
        }

        // Return the final displacements
        return Displacement(
            linearDisplacementX.last(),
            linearDisplacementY.last(),
            linearDisplacementZ.last(),
            angularDisplacementX.last(),
            angularDisplacementY.last(),
            angularDisplacementZ.last()
        )
    }
}

data class Displacement(
    val linearDisplacementX: Float,
    val linearDisplacementY: Float,
    val linearDisplacementZ: Float,
    val angularDisplacementX: Float,
    val angularDisplacementY: Float,
    val angularDisplacementZ: Float
)