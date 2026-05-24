package com.example.sathiai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp
import com.example.sathiai.ui.theme.AccentPrimary
import com.example.sathiai.ui.theme.AccentSecond

@Composable
fun AICenterpiece(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer Glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AccentPrimary.copy(alpha = 0.15f * scale),
                        Color.Transparent
                    )
                ),
                radius = size.minDimension / 1.2f
            )
        }
        
        // Inner Core Orbs
        repeat(3) { i ->
            val phaseOffset = i * 120f
            Canvas(
                modifier = Modifier
                    .size(120.dp)
                    .offset(
                        x = (kotlin.math.cos(Math.toRadians((rotation + phaseOffset).toDouble())) * 15).dp,
                        y = (kotlin.math.sin(Math.toRadians((rotation + phaseOffset).toDouble())) * 15).dp
                    )
            ) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (i % 2 == 0) AccentPrimary.copy(0.4f) else AccentSecond.copy(0.4f),
                            Color.Transparent
                        )
                    ),
                    radius = (size.minDimension / 2.5f) * scale
                )
            }
        }
    }
}