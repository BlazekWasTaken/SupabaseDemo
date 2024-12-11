package com.example.supabasedemo.utils

import android.content.Context
import com.example.supabasedemo.compose.views.Reading
import kotlinx.io.IOException
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TfLiteModel (context: Context){
    private var interpreter: Interpreter? = null

    init {
        try {
            @Suppress("DEPRECATION")
            interpreter = Interpreter(loadModelFile(context))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
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

        val output = FloatArray(1)

        interpreter?.run(input, output)
        return output[0] > 0.5
    }

    data class Displacement(
        val linearDisplacementX: Float,
        val linearDisplacementY: Float,
        val linearDisplacementZ: Float,
        val angularDisplacementX: Float,
        val angularDisplacementY: Float,
        val angularDisplacementZ: Float
    )

    fun calculateDisplacement(
        accelerometerReadings: List<Reading>,
        gyroscopeReadings: List<Reading>,
        dt: Float = 0.1f
    ): Displacement {
        val n1 = accelerometerReadings.size
        val n2 = gyroscopeReadings.size

        val accX = accelerometerReadings.map {it.x}
        val accY = accelerometerReadings.map {it.y}
        val accZ = accelerometerReadings.map {it.z}

        val gyrX = gyroscopeReadings.map {it.x}
        val gyrY = gyroscopeReadings.map {it.y}
        val gyrZ = gyroscopeReadings.map {it.z}

        // Initialize velocity and displacement arrays
        val velocityX = MutableList(n1) { 0f }
        val velocityY = MutableList(n1) { 0f }
        val velocityZ = MutableList(n1) { 0f }

        val linearDisplacementX = MutableList(n1) { 0f }
        val linearDisplacementY = MutableList(n1) { 0f }
        val linearDisplacementZ = MutableList(n1) { 0f }

        val angularDisplacementX = MutableList(n2) { 0f }
        val angularDisplacementY = MutableList(n2) { 0f }
        val angularDisplacementZ = MutableList(n2) { 0f }

        for (i in 1 until n1) {
            // Update velocity using trapezoidal rule
            velocityX[i] = velocityX[i - 1] + 0.5f * (accX[i] + accX[i - 1]) * dt
            velocityY[i] = velocityY[i - 1] + 0.5f * (accY[i] + accY[i - 1]) * dt
            velocityZ[i] = velocityZ[i - 1] + 0.5f * (accZ[i] + accZ[i - 1]) * dt

            // Update linear displacement using trapezoidal rule
            linearDisplacementX[i] = linearDisplacementX[i - 1] + 0.5f * (velocityX[i] + velocityX[i - 1]) * dt
            linearDisplacementY[i] = linearDisplacementY[i - 1] + 0.5f * (velocityY[i] + velocityY[i - 1]) * dt
            linearDisplacementZ[i] = linearDisplacementZ[i - 1] + 0.5f * (velocityZ[i] + velocityZ[i - 1]) * dt

            // Update angular displacement using trapezoidal rule
            angularDisplacementX[i] = angularDisplacementX[i - 1] + 0.5f * (gyrX[i] + gyrX[i - 1]) * dt
            angularDisplacementY[i] = angularDisplacementY[i - 1] + 0.5f * (gyrY[i] + gyrY[i - 1]) * dt
            angularDisplacementZ[i] = angularDisplacementZ[i - 1] + 0.5f * (gyrZ[i] + gyrZ[i - 1]) * dt
        }

        for (i in 1 until n2) {
            // Update velocity using trapezoidal rule
            velocityX[i] = velocityX[i - 1] + 0.5f * (accX[i] + accX[i - 1]) * dt
            velocityY[i] = velocityY[i - 1] + 0.5f * (accY[i] + accY[i - 1]) * dt
            velocityZ[i] = velocityZ[i - 1] + 0.5f * (accZ[i] + accZ[i - 1]) * dt

            // Update linear displacement using trapezoidal rule
            linearDisplacementX[i] = linearDisplacementX[i - 1] + 0.5f * (velocityX[i] + velocityX[i - 1]) * dt
            linearDisplacementY[i] = linearDisplacementY[i - 1] + 0.5f * (velocityY[i] + velocityY[i - 1]) * dt
            linearDisplacementZ[i] = linearDisplacementZ[i - 1] + 0.5f * (velocityZ[i] + velocityZ[i - 1]) * dt

            // Update angular displacement using trapezoidal rule
            angularDisplacementX[i] = angularDisplacementX[i - 1] + 0.5f * (gyrX[i] + gyrX[i - 1]) * dt
            angularDisplacementY[i] = angularDisplacementY[i - 1] + 0.5f * (gyrY[i] + gyrY[i - 1]) * dt
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