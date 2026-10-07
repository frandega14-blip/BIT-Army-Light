package com.bit.armylight.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateInt
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val transition = rememberInfiniteTransition(label = "LightEngine")

    val pulseDuration = if (isConcertActive) {
        when (concertProgram) {
            ConcertProgram.STROBE -> 280
            ConcertProgram.WAVE -> 1000
            ConcertProgram.SUPERNOVA -> 650
            ConcertProgram.AURORA -> 1800
        }
    } else {
        2400
    }

    val pulseScale by transition.animateFloat(
        initialValue = if (isPulseActive) 0.82f else 1.0f,
        targetValue = if (isPulseActive) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = pulseDuration,
                easing = if (
                    isConcertActive &&
                    concertProgram == ConcertProgram.STROBE
                ) LinearEasing else FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val glowAlpha by transition.animateFloat(
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

    val colorStep by transition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
        animation = tween(
            durationMillis = when (concertProgram) {
                ConcertProgram.STROBE -> 1800
                ConcertProgram.WAVE -> 4000
                ConcertProgram.SUPERNOVA -> 2400
                ConcertProgram.AURORA -> 7000
            },
            easing = LinearEasing
        ),
        repeatMode = RepeatMode.Restart
    ),
    label = "ColorCycle"
)

    val factor = intensity.factor

    fun hsvColor(hue: Float, saturation: Float = 0.9f): Color {
        return Color.hsv(
            hue = hue % 360f,
            saturation = saturation,
            value = 1f
        )
    }

    val concertColor = if (!isConcertActive) {
        Color(0xFF8B5CF6)
    } else {
        when (concertProgram) {
            ConcertProgram.WAVE -> {
                val waveHue = 270f + (colorStep * 0.45f)
                hsvColor(waveHue, 0.85f)
            }

            ConcertProgram.STROBE -> {
                val strobeHue = ((colorStep / 45) * 45).toFloat()
                hsvColor(strobeHue, 1f)
            }

            ConcertProgram.SUPERNOVA -> {
                val novaHue = 210f + colorStep.toFloat()
                hsvColor(novaHue, 0.95f)
            }

            ConcertProgram.AURORA -> {
                hsvColor(colorStep.toFloat(), 0.8f)
            }
        }
    }

    val secondColor = if (!isConcertActive) {
        Color(0xFF7C3AED)
    } else {
        when (concertProgram) {
            ConcertProgram.WAVE -> hsvColor((colorStep + 70) % 360f, 0.9f)
            ConcertProgram.STROBE -> hsvColor((colorStep + 120) % 360f, 1f)
            ConcertProgram.SUPERNOVA -> hsvColor((colorStep + 90) % 360f, 0.9f)
            ConcertProgram.AURORA -> hsvColor((colorStep + 120) % 360f, 0.75f)
        }
    }

    val thirdColor = if (!isConcertActive) {
        Color(0xFF4C1D95)
    } else {
        when (concertProgram) {
            ConcertProgram.WAVE -> hsvColor((colorStep + 150) % 360f, 0.85f)
            ConcertProgram.STROBE -> hsvColor((colorStep + 240) % 360f, 1f)
            ConcertProgram.SUPERNOVA -> hsvColor((colorStep + 180) % 360f, 0.95f)
            ConcertProgram.AURORA -> hsvColor((colorStep + 240) % 360f, 0.8f)
        }
    }

    if (isPulseActive && pulseScale > 1.23f) {
        onPeakPulse()
    }

        Canvas(modifier = modifier.fillMaxSize()) {

        // Luz de pantalla completa.
        // El color ocupa todo el área disponible.
        val screenAlpha = factor * (
            0.82f + (0.18f * glowAlpha)
        )

        drawRect(
            color = concertColor.copy(alpha = screenAlpha)
        )

        // Supernova añade un destello blanco sobre toda la pantalla.
        if (
            isConcertActive &&
            concertProgram == ConcertProgram.SUPERNOVA &&
            pulseScale > 1.15f
        ) {
            drawRect(
                color = Color.White.copy(
                    alpha = 0.65f * factor
                )
            )
        }
    }
