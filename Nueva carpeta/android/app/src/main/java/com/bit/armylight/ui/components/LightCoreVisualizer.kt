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
    val transition = rememberInfiniteTransition(
        label = "LightEngine"
    )

    /*
     * Velocidad del pulso.
     */
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

    /*
     * Animación del pulso.
     */
    val pulseScale by transition.animateFloat(
        initialValue = if (isPulseActive) 0.82f else 1.0f,
        targetValue = if (isPulseActive) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = pulseDuration,
                easing = if (
                    isConcertActive &&
                    concertProgram == ConcertProgram.STROBE
                ) {
                    LinearEasing
                } else {
                    FastOutSlowInEasing
                }
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    /*
     * Intensidad visual del pulso.
     */
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

    /*
     * Ciclo continuo de colores.
     */
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

    /*
     * Convierte un tono HSV en Color.
     */
    fun hsvColor(
        hue: Float,
        saturation: Float = 0.9f
    ): Color {
        return Color.hsv(
            hue = hue % 360f,
            saturation = saturation,
            value = 1f
        )
    }

    /*
     * COLOR PRINCIPAL
     */
    val concertColor = if (!isConcertActive) {
        Color(0xFF8B5CF6)
    } else {
        when (concertProgram) {

            /*
             * Púrpura, magenta y azul.
             */
            ConcertProgram.WAVE -> {
                val waveHue = 270f + (colorStep * 0.45f)
                hsvColor(
                    hue = waveHue,
                    saturation = 0.85f
                )
            }

            /*
             * Cambios rápidos y fuertes.
             */
            ConcertProgram.STROBE -> {
                val strobeHue =
                    ((colorStep / 45f).toInt() * 45).toFloat()

                hsvColor(
                    hue = strobeHue,
                    saturation = 1f
                )
            }

            /*
             * Explosión multicolor.
             */
            ConcertProgram.SUPERNOVA -> {
                val novaHue = 210f + colorStep

                hsvColor(
                    hue = novaHue,
                    saturation = 0.95f
                )
            }

            /*
             * Arcoíris suave.
             */
            ConcertProgram.AURORA -> {
                hsvColor(
                    hue = colorStep,
                    saturation = 0.8f
                )
            }
        }
    }

    /*
     * SEGUNDO COLOR
     */
    val secondColor = if (!isConcertActive) {
        Color(0xFF7C3AED)
    } else {
        when (concertProgram) {

            ConcertProgram.WAVE -> {
                hsvColor(
                    hue = colorStep + 70f,
                    saturation = 0.9f
                )
            }

            ConcertProgram.STROBE -> {
                hsvColor(
                    hue = colorStep + 120f,
                    saturation = 1f
                )
            }

            ConcertProgram.SUPERNOVA -> {
                hsvColor(
                    hue = colorStep + 90f,
                    saturation = 0.9f
                )
            }

            ConcertProgram.AURORA -> {
                hsvColor(
                    hue = colorStep + 120f,
                    saturation = 0.75f
                )
            }
        }
    }

    /*
     * TERCER COLOR
     */
    val thirdColor = if (!isConcertActive) {
        Color(0xFF4C1D95)
    } else {
        when (concertProgram) {

            ConcertProgram.WAVE -> {
                hsvColor(
                    hue = colorStep + 150f,
                    saturation = 0.85f
                )
            }

            ConcertProgram.STROBE -> {
                hsvColor(
                    hue = colorStep + 240f,
                    saturation = 1f
                )
            }

            ConcertProgram.SUPERNOVA -> {
                hsvColor(
                    hue = colorStep + 180f,
                    saturation = 0.95f
                )
            }

            ConcertProgram.AURORA -> {
                hsvColor(
                    hue = colorStep + 240f,
                    saturation = 0.8f
                )
            }
        }
    }

    /*
     * Pulso háptico.
     */
    if (isPulseActive && pulseScale > 1.23f) {
        onPeakPulse()
    }

    /*
     * LUZ DE PANTALLA COMPLETA
     */
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {

        /*
         * La intensidad controla el brillo general.
         *
         * El pulso hace que la pantalla respire
         * entre un nivel más bajo y uno más alto.
         */
        val screenAlpha = factor * (
            0.82f + (0.18f * glowAlpha)
        )

        /*
         * Color principal ocupando TODA la pantalla.
         */
        drawRect(
            color = concertColor.copy(
                alpha = screenAlpha
            )
        )

        /*
         * Supernova:
         * destello blanco sobre toda la pantalla.
         */
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
}
