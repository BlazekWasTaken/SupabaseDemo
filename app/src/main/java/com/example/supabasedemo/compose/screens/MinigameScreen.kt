package com.example.supabasedemo.compose.screens

import android.content.ContentValues.TAG
import android.content.Context
import android.content.res.Resources.getSystem
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import kotlin.random.Random


data class RedCircle(
    val id: Int,
    val offsetX: Animatable<Float, *> = Animatable(0f),
    val offsetY: Animatable<Float, *> = Animatable(0f),
    var isVisible: Boolean = false
)

val Int.px: Int get() = (this * getSystem().displayMetrics.density).toInt()

@Composable
fun MinigameScreen(
    onNavigateToMainMenu: () -> Unit,
    getState: () -> MutableState<UserState>,
    setState: (state: UserState) -> Unit
) {
    LaunchedEffect(Unit) {
        setState(UserState.InMiniGame)
    }

    var score by remember { mutableIntStateOf(0) }
    var isMoving by remember { mutableStateOf(false) }
    var latestSensorRead by remember { mutableStateOf(System.currentTimeMillis()) }

    val context = LocalContext.current

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val linearAccelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val acceleration = sqrt(x * x + y * y + z * z)
                    val movementThreshold = 3.0f
                    val movementTimeThreshold = 50
                    if (acceleration > movementThreshold) {
                        if (System.currentTimeMillis() - latestSensorRead <= movementTimeThreshold) {
                            isMoving = true;
                        } else {
                            isMoving = false;
                        }
                        latestSensorRead = System.currentTimeMillis()
                        Log.i(TAG, "$x $y $z")
                    } else {
                        isMoving = false
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }
        sensorManager.registerListener(
            sensorEventListener,
            linearAccelerationSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }


    LaunchedEffect(isMoving) {
        while (true) {
            if (isMoving) {
                if (score > 0) {
                    score--
                }
            }
            delay(1000)
        }
    }

    val circleSize = 50.dp
    val density = LocalDensity.current
    val animationDuration = 1000
    val delayDuration = 1500
    val fadeoutDuration = 500

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colorScheme.surface)
    ) {
        val maxWidthPx = (this.maxWidth - circleSize).value.toInt().px
        val maxHeightPx = (this.maxHeight - circleSize - 50.dp).value.toInt().px

        val greenOffsetX = remember { Animatable(Random.nextFloat() * maxWidthPx) }
        val greenOffsetY = remember { Animatable(Random.nextFloat() * maxHeightPx) }

        var isGreenVisible by remember { mutableStateOf(true) }

        val redCircles = remember { mutableStateListOf<RedCircle>() }
        var redCircleId by remember { mutableIntStateOf(0) }

        LaunchedEffect(Unit) {
            redCircles.add(
                RedCircle(
                    id = redCircleId++,
                    offsetX = Animatable(Random.nextFloat() * maxWidthPx),
                    offsetY = Animatable(Random.nextFloat() * maxHeightPx)
                )
            )
        }

        var cycleCount by remember { mutableIntStateOf(0) }

        LaunchedEffect(key1 = "AnimationLoop") {
            println("Animation loop started")
            while (true) {
                cycleCount++

                redCircles.forEach { it.isVisible = true }

                isGreenVisible = true

                val redAnimationsX = redCircles.map { redCircle ->
                    launch {
                        val targetRedX = Random.nextFloat() * maxWidthPx

                        redCircle.offsetX.animateTo(
                            targetValue = targetRedX,
                            animationSpec = tween(durationMillis = animationDuration)
                        )

                    }
                }

                val redAnimationsY = redCircles.map { redCircle ->
                    launch {
                        val targetRedY = Random.nextFloat() * maxHeightPx
                        redCircle.offsetY.animateTo(
                            targetValue = targetRedY,
                            animationSpec = tween(durationMillis = animationDuration)
                        )

                    }
                }

                val targetGreenX = Random.nextFloat() * maxWidthPx
                val targetGreenY = Random.nextFloat() * maxHeightPx

                val greenAnimX = launch {
                    greenOffsetX.animateTo(
                        targetValue = targetGreenX,
                        animationSpec = tween(durationMillis = animationDuration)
                    )
                }
                val greenAnimY = launch {
                    greenOffsetY.animateTo(
                        targetValue = targetGreenY,
                        animationSpec = tween(durationMillis = animationDuration)
                    )
                }

                redAnimationsX.forEach { it.join() }
                redAnimationsY.forEach { it.join() }

                greenAnimX.join()
                greenAnimY.join()

                isGreenVisible = false

                redCircles.forEach { it.isVisible = false }

                if (cycleCount % 2 == 0) {
                    redCircles.add(
                        RedCircle(
                            id = redCircleId++,
                            offsetX = Animatable(Random.nextFloat() * maxWidthPx),
                            offsetY = Animatable(Random.nextFloat() * maxHeightPx)
                        )
                    )
                }

                delay(fadeoutDuration.toLong())

                greenOffsetX.snapTo(Random.nextFloat() * maxWidthPx)
                greenOffsetY.snapTo(Random.nextFloat() * maxHeightPx)
                redCircles.forEach {
                    it.isVisible = false
                    it.offsetX.snapTo(Random.nextFloat() * maxWidthPx)
                }
                redCircles.forEach {
                    it.isVisible = false
                    it.offsetY.snapTo(Random.nextFloat() * maxHeightPx)
                }

                delay(delayDuration.toLong())
            }
        }

        Text(
            text = "Score: $score",
            fontSize = 24.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
        )

        val greenOffsetXDp = with(density) { greenOffsetX.value.toDp() }
        val greenOffsetYDp = with(density) { greenOffsetY.value.toDp() }

        if (isGreenVisible) {
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .offset(x = greenOffsetXDp, y = greenOffsetYDp)
                    .background(Color.Green, CircleShape)
                    .clickable {
                        score++
                        isGreenVisible = false
                    }
            )
        }

        redCircles.forEach { redCircle ->
            val redOffsetXDp = with(density) { redCircle.offsetX.value.toDp() }
            val redOffsetYDp = with(density) { redCircle.offsetY.value.toDp() }

            if (redCircle.isVisible) {
                Box(
                    modifier = Modifier
                        .size(circleSize)
                        .offset(x = redOffsetXDp, y = redOffsetYDp)
                        .background(Color.Red, CircleShape)
                        .clickable {
                            score--
                            redCircle.isVisible = false
                        }
                )
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
