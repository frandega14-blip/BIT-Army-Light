package com.bit.armylight.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.bit.armylight.ui.theme.PurpleAmbient
import com.bit.armylight.ui.theme.PurpleBright
import com.bit.armylight.ui.theme.PurpleCore
import com.bit.armylight.ui.theme.PurpleDeep
import com.bit.armylight.ui.theme.PurpleGlow
import com.bit.armylight.ui.theme.PurplePrimary
import com.bit.armylight.ui.theme.PurpleVibrant
import com.bit.armylight.viewmodel.ConcertProgram
import com.bit.armylight.viewmodel.LightIntensity

@Composable
fun LightCoreVisualizer(
    modifier: Modifier = Modifier,
    isPulseActive: Boolean,
    isConcertActive: Boolean,
    concertProgram: ConcertProgram,
    intensity: LightIntensity,
    isBatterySaver: Boolean,
    onPeakPulse: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LightEngine")

    // Animación suave de respiración / pulso base
    val pulseDuration = if (isConcertActive) {
        when (concertProgram) {
            ConcertProgram.STROBE -> 450
            ConcertProgram.WAVE -> 1200
            ConcertProgram.SUPERNOVA -> 800
            ConcertProgram.AURORA -> 2000
        }
    } else {
        2400 // Pulso suave estándar
    }

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = if (isPulseActive) 0.82f else 1.0f,
        targetValue = if (isPulseActive) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = pulseDuration,
                easing = if (isConcertActive && concertProgram == ConcertProgram.STROBE) LinearEasing else FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val pulseGlowAlpha by infiniteTransition.animateFloat(
        initialValue = if (isPulseActive) 0.45f else 0.85f,
        targetValue = if (isPulseActive) 1.0f else 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = pulseDuration,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    // Trigger de vibración en el pico del pulso cuando se alcanza la cima
    LaunchedEffect(pulseScale) {
        if (isPulseActive && pulseScale > 1.23f) {
            onPeakPulse()
        }
    }

    val factor = intensity.factor

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val minDim = minOf(size.width, size.height)
        val baseRadius = (minDim * 0.40f) * pulseScale

        // Fondo y resplandor ambiental
        if (!isBatterySaver) {
            val ambientRadius = (minDim * 0.95f) * pulseScale
            val ambientBrush = Brush.radialGradient(
                colors = listOf(
                    PurpleDeep.copy(alpha = 0.55f * factor * pulseGlowAlpha),
                    PurpleAmbient.copy(alpha = 0.25f * factor),
                    Color.Transparent
                ),
                center = center,
                radius = ambientRadius
            )
            drawCircle(brush = ambientBrush, radius = ambientRadius, center = center)
        }

        // Corona de brillo exterior (Bloom)
        val coronaRadius = baseRadius * 1.55f
        val coronaBrush = Brush.radialGradient(
            colors = listOf(
                PurpleGlow.copy(alpha = 0.75f * factor * pulseGlowAlpha),
                PurplePrimary.copy(alpha = 0.40f * factor * pulseGlowAlpha),
                Color.Transparent
            ),
            center = center,
            radius = coronaRadius
        )
        drawCircle(brush = coronaBrush, radius = coronaRadius, center = center)

        // Orbe púrpura principal de alta saturación
        val bodyBrush = Brush.radialGradient(
            colors = listOf(
                PurpleBright.copy(alpha = 0.95f * factor),
                PurpleVibrant.copy(alpha = 0.90f * factor),
                PurplePrimary.copy(alpha = 0.70f * factor),
                Color.Transparent
            ),
            center = center,
            radius = baseRadius
        )
        drawCircle(brush = bodyBrush, radius = baseRadius, center = center)

        // Núcleo blanco-violeta ultra brillante
        val coreRadius = baseRadius * 0.38f
        val coreBrush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.98f * factor),
                PurpleCore.copy(alpha = 0.90f * factor),
                PurpleBright.copy(alpha = 0.50f * factor),
                Color.Transparent
            ),
            center = center,
            radius = coreRadius
        )
        drawCircle(brush = coreBrush, radius = coreRadius, center = center)
    }
}
