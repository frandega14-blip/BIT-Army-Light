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
    isRhythmActive: Boolean,
    rhythmLevel: Float,
    onPeakPulse: () -> Unit
) {

    val transition = rememberInfiniteTransition(
        label = "LightEngine"
    )

    val pulseDuration =
        if (isConcertActive) {

            when (concertProgram) {
                ConcertProgram.STROBE -> 260
                ConcertProgram.WAVE -> 900
                ConcertProgram.SUPERNOVA -> 550
                ConcertProgram.AURORA -> 1800
            }

        } else {
            2400
        }

    val pulseScale by transition.animateFloat(
        initialValue =
            if (isPulseActive) 0.82f else 1.0f,
        targetValue =
            if (isPulseActive) 1.25f else 1.0f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(
                    durationMillis = pulseDuration,
                    easing =
                        if (
                            isConcertActive &&
                            concertProgram ==
                            ConcertProgram.STROBE
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

    val glowAlpha by transition.animateFloat(
        initialValue =
            if (isPulseActive) 0.45f else 0.85f,
        targetValue =
            if (isPulseActive) 1.0f else 0.85f,
        animationSpec =
            infiniteRepeatable(
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
        animationSpec =
            infiniteRepeatable(
                animation = tween(
                    durationMillis =
                        when (concertProgram) {
                            ConcertProgram.STROBE -> 1500
                            ConcertProgram.WAVE -> 3600
                            ConcertProgram.SUPERNOVA -> 2100
                            ConcertProgram.AURORA -> 6500
                        },
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
        label = "ColorCycle"
    )

    val factor = intensity.factor

    fun hsvColor(
        hue: Float,
        saturation: Float = 0.9f
    ): Color {

        val normalizedHue =
            ((hue % 360f) + 360f) % 360f

        return Color.hsv(
            hue = normalizedHue,
            saturation = saturation,
            value = 1f
        )
    }

    val concertColor =
        if (!isConcertActive) {

            Color(0xFF8B5CF6)

        } else {

            when (concertProgram) {

                ConcertProgram.WAVE ->
                    hsvColor(
                        260f +
                                (colorStep * 0.55f),
                        0.90f
                    )

                ConcertProgram.STROBE -> {

                    val step =
                        (colorStep / 60f)
                            .toInt() * 60f

                    hsvColor(
                        step.toFloat(),
                        1f
                    )
                }

                ConcertProgram.SUPERNOVA ->
                    hsvColor(
                        200f + colorStep,
                        0.95f
                    )

                ConcertProgram.AURORA ->
                    hsvColor(
                        colorStep,
                        0.82f
                    )
            }
        }

    val secondColor =
        if (!isConcertActive) {

            Color(0xFF7C3AED)

        } else {

            when (concertProgram) {

                ConcertProgram.WAVE ->
                    hsvColor(
                        colorStep + 90f,
                        0.90f
                    )

                ConcertProgram.STROBE ->
                    hsvColor(
                        colorStep + 120f,
                        1f
                    )

                ConcertProgram.SUPERNOVA ->
                    hsvColor(
                        colorStep + 120f,
                        0.95f
                    )

                ConcertProgram.AURORA ->
                    hsvColor(
                        colorStep + 120f,
                        0.78f
                    )
            }
        }

    val thirdColor =
        if (!isConcertActive) {

            Color(0xFF4C1D95)

        } else {

            when (concertProgram) {

                ConcertProgram.WAVE ->
                    hsvColor(
                        colorStep + 180f,
                        0.88f
                    )

                ConcertProgram.STROBE ->
                    hsvColor(
                        colorStep + 240f,
                        1f
                    )

                ConcertProgram.SUPERNOVA ->
                    hsvColor(
                        colorStep + 240f,
                        0.95f
                    )

                ConcertProgram.AURORA ->
                    hsvColor(
                        colorStep + 240f,
                        0.80f
                    )
            }
        }

    /*
     * MODO RITMO
     *
     * El nivel del micrófono controla el brillo.
     */
    val rhythmBoost =
        if (isRhythmActive) {
            rhythmLevel.coerceIn(0f, 1f)
        } else {
            0f
        }

    val rhythmAlpha =
        if (isRhythmActive) {
            (0.72f + rhythmBoost * 0.28f)
                .coerceIn(0f, 1f)
        } else {
            0f
        }

    /*
     * Flash de ritmo cuando el sonido es fuerte.
     */
    val rhythmFlash =
        isRhythmActive &&
        rhythmLevel > 0.72f

    /*
     * Pulso háptico normal.
     */
    if (
        isPulseActive &&
        pulseScale > 1.23f
    ) {
        onPeakPulse()
    }

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {

        val screenAlpha =
            factor * (
                0.78f +
                        (0.22f * glowAlpha)
            )

        /*
         * =====================================================
         * MODO RITMO
         * =====================================================
         *
         * Tiene prioridad visual cuando está activo.
         */
        if (isRhythmActive) {

            /*
             * Color que cambia suavemente
             * según el nivel del sonido.
             */
            val rhythmColor =
                when {

                    rhythmLevel > 0.82f ->
                        Color.White

                    rhythmLevel > 0.60f ->
                        hsvColor(
                            colorStep + 180f,
                            0.95f
                        )

                    rhythmLevel > 0.35f ->
                        hsvColor(
                            colorStep + 70f,
                            0.90f
                        )

                    else ->
                        Color(0xFF8B5CF6)
                }

            drawRect(
                color = rhythmColor.copy(
                    alpha =
                        screenAlpha *
                                rhythmAlpha
                )
            )

            /*
             * Capa secundaria cuando sube el volumen.
             */
            if (rhythmLevel > 0.30f) {

                drawRect(
                    color = secondColor.copy(
                        alpha =
                            rhythmLevel *
                                    0.28f *
                                    factor
                    )
                )
            }

            /*
             * GOLPE FUERTE
             */
            if (rhythmFlash) {

                drawRect(
                    color = Color.White.copy(
                        alpha =
                            ((rhythmLevel - 0.72f) *
                                    1.8f)
                                .coerceIn(0f, 0.75f) *
                                factor
                    )
                )
            }

        } else if (!isConcertActive) {

            /*
             * MODO NORMAL
             */
            drawRect(
                color = concertColor.copy(
                    alpha = screenAlpha
                )
            )

        } else {

            /*
             * =================================================
             * CONCIERTO
             * =================================================
             */
            when (concertProgram) {

                ConcertProgram.WAVE -> {

                    val wavePosition =
                        (colorStep / 360f) *
                                size.width * 2f

                    val bandWidth =
                        size.width * 0.65f

                    drawRect(
                        color =
                            concertColor.copy(
                                alpha = screenAlpha
                            )
                    )

                    drawRect(
                        color =
                            secondColor.copy(
                                alpha =
                                    0.42f * factor
                            ),
                        topLeft =
                            androidx.compose.ui.geometry.Offset(
                                x =
                                    wavePosition -
                                            bandWidth,
                                y = 0f
                            ),
                        size =
                            androidx.compose.ui.geometry.Size(
                                width = bandWidth,
                                height = size.height
                            )
                    )

                    drawRect(
                        color =
                            thirdColor.copy(
                                alpha =
                                    0.32f * factor
                            ),
                        topLeft =
                            androidx.compose.ui.geometry.Offset(
                                x =
                                    wavePosition -
                                            bandWidth * 2f,
                                y = 0f
                            ),
                        size =
                            androidx.compose.ui.geometry.Size(
                                width = bandWidth,
                                height = size.height
                            )
                    )
                }

                ConcertProgram.STROBE -> {

                    drawRect(
                        color =
                            concertColor.copy(
                                alpha = screenAlpha
                            )
                    )

                    if (colorStep % 90f < 18f) {

                        drawRect(
                            color =
                                Color.White.copy(
                                    alpha =
                                        0.75f *
                                                factor
                                )
                        )
                    }
                }

                ConcertProgram.SUPERNOVA -> {

                    drawRect(
                        color =
                            concertColor.copy(
                                alpha = screenAlpha
                            )
                    )

                    if (pulseScale > 1.05f) {

                        drawRect(
                            color =
                                secondColor.copy(
                                    alpha =
                                        0.28f *
                                                factor
                                )
                        )
                    }

                    if (pulseScale > 1.18f) {

                        drawRect(
                            color =
                                Color.White.copy(
                                    alpha =
                                        0.72f *
                                                factor
                                )
                        )
                    }
                }

                ConcertProgram.AURORA -> {

                    drawRect(
                        color =
                            concertColor.copy(
                                alpha = screenAlpha
                            )
                    )

                    drawRect(
                        color =
                            secondColor.copy(
                                alpha =
                                    0.30f *
                                            factor
                            )
                    )

                    drawRect(
                        color =
                            thirdColor.copy(
                                alpha =
                                    0.22f *
                                            factor
                            )
                    )
                }
            }
        }

        /*
         * FLASH EXTRA DE SUPERNOVA
         */
        if (
            isConcertActive &&
            concertProgram ==
            ConcertProgram.SUPERNOVA &&
            pulseScale > 1.20f
        ) {

            drawRect(
                color =
                    Color.White.copy(
                        alpha =
                            0.30f *
                                    factor
                    )
            )
        }
    }
}
