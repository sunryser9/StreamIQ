package com.streamiq.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin
import kotlin.random.Random

data class ConfettiParticle(
    val x: Float,
    val startY: Float,
    val color: Color,
    val size: Float,
    val speed: Float,
    val wobble: Float,
    val rotation: Float
)

val confettiColors = listOf(
    Color(0xFF00E5FF), Color(0xFFFFD700), Color(0xFF00E676),
    Color(0xFFFF5252), Color(0xFFFF69B4), Color(0xFFCE93D8)
)

@Composable
fun ConfettiOverlay(active: Boolean, onDone: () -> Unit) {
    if (!active) return

    val particles = remember {
        List(60) {
            ConfettiParticle(
                x = Random.nextFloat(),
                startY = -Random.nextFloat() * 0.3f,
                color = confettiColors.random(),
                size = Random.nextFloat() * 12f + 6f,
                speed = Random.nextFloat() * 0.4f + 0.3f,
                wobble = Random.nextFloat() * 4f - 2f,
                rotation = Random.nextFloat() * 360f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        onDone()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val y = (p.startY + progress * p.speed * 1.5f)
            if (y > 1.1f) return@forEach
            val x = p.x + sin(progress * 6f + p.wobble) * 0.03f
            rotate(degrees = p.rotation + progress * 180f, pivot = Offset(x * size.width, y * size.height)) {
                drawRect(
                    color = p.color.copy(alpha = if (y > 0.8f) (1f - y) * 5f else 1f),
                    topLeft = Offset(x * size.width - p.size / 2, y * size.height - p.size / 2),
                    size = androidx.compose.ui.geometry.Size(p.size, p.size * 0.6f)
                )
            }
        }
    }
}
