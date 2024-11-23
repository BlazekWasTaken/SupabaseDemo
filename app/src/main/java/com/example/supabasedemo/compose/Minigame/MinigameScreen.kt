package com.example.supabasedemo.compose.Minigame

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

data class RedCircle(
    val id: Int,
    val offsetX: Animatable<Float, *> = Animatable(0f),
    val offsetY: Animatable<Float, *> = Animatable(0f),
    var isVisible: Boolean = false
)

@Composable
fun Screen(onNavigateTo: () -> Unit) {
    var score by remember { mutableStateOf(0) }

    val circleSize = 50.dp
    val density = LocalDensity.current
    val animationDuration = 1000
    val delayDuration = 1500
    val fadeoutDuration = 500

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {
        val maxWidthPx = with(density) { (maxWidth - circleSize).toPx() }
        val maxHeightPx = with(density) { (maxHeight - circleSize - 50.dp).toPx() }

        val greenOffsetX = remember { Animatable(Random.nextFloat() * maxWidthPx) }
        val greenOffsetY = remember { Animatable(Random.nextFloat() * maxHeightPx) }

        var isGreenVisible by remember { mutableStateOf(true) }

        val redCircles = remember { mutableStateListOf<RedCircle>() }
        var redCircleId by remember { mutableStateOf(0) }

        LaunchedEffect(Unit) {
            redCircles.add(
                RedCircle(
                    id = redCircleId++,
                    offsetX = Animatable(Random.nextFloat() * maxWidthPx),
                    offsetY = Animatable(Random.nextFloat() * maxHeightPx)
                )
            )
        }

        var cycleCount by remember { mutableStateOf(0) }

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
                            score++
                            redCircle.isVisible = false
                        }
                )
            }
        }
    }
}
