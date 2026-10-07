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
     * VELOCIDAD DEL PULSO
     */
    val pulseDuration = if (isConcertActive) {
        when (concertProgram) {
            ConcertProgram.STROBE -> 260
            ConcertProgram.WAVE -> 900
            ConcertProgram.SUPERNOVA -> 550
            ConcertProgram.AURORA -> 1800
        }
    } else {
        2400
    }

    /*
     * PULSO
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
     * BRILLO DEL PULSO
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
     * CICLO PRINCIPAL DE COLOR
     */
    val colorStep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (concertProgram) {
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

    /*
     * HSV -> COLOR
     */
    fun hsvColor(
        hue: Float,
        saturation: Float = 0.9f
    ): Color {
        val normalizedHue = ((hue % 360f) + 360f) % 360f

        return Color.hsv(
            hue = normalizedHue,
            saturation = saturation,
            value = 1f
        )
    }

    /*
     * COLOR BASE
     */
    val concertColor = if (!isConcertActive) {
        Color(0xFF8B5CF6)
    } else {
        when (concertProgram) {

            /*
             * ONDA PÚRPURA
             */
            ConcertProgram.WAVE -> {
                hsvColor(
                    hue = 260f + (colorStep * 0.55f),
                    saturation = 0.90f
                )
            }

            /*
             * STROBE
             */
            ConcertProgram.STROBE -> {
                val step = (colorStep / 60f).toInt() * 60f

                hsvColor(
                    hue = step,
                    saturation = 1f
                )
            }

            /*
             * SUPERNOVA
             */
            ConcertProgram.SUPERNOVA -> {
                hsvColor(
                    hue = 200f + colorStep,
                    saturation = 0.95f
                )
            }

            /*
             * AURORA
             */
            ConcertProgram.AURORA -> {
                hsvColor(
                    hue = colorStep,
                    saturation = 0.82f
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
                    hue = colorStep + 90f,
                    saturation = 0.90f
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
                    hue = colorStep + 120f,
                    saturation = 0.95f
                )
            }

            ConcertProgram.AURORA -> {
                hsvColor(
                    hue = colorStep + 120f,
                    saturation = 0.78f
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
                    hue = colorStep + 180f,
                    saturation = 0.88f
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
                    hue = colorStep + 240f,
                    saturation = 0.95f
                )
            }

            ConcertProgram.AURORA -> {
                hsvColor(
                    hue = colorStep + 240f,
                    saturation = 0.80f
                )
            }
        }
    }

    /*
     * PULSO HÁPTICO
     */
    if (isPulseActive && pulseScale > 1.23f) {
        onPeakPulse()
    }

    /*
     * PANTALLA COMPLETA
     */
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {

        /*
         * BRILLO GENERAL
         */
        val screenAlpha = factor * (
            0.78f + (0.22f * glowAlpha)
        )

        /*
         * =========================================================
         * MODO NORMAL
         * =========================================================
         */
        if (!isConcertActive) {

            drawRect(
                color = concertColor.copy(
                    alpha = screenAlpha
                )
            )

        } else {

            /*
             * =====================================================
             * CONCIERTO
             * =====================================================
             */

            when (concertProgram) {

                /*
                 * ONDA PÚRPURA
                 *
                 * Mezcla tres colores mediante franjas verticales
                 * que se desplazan continuamente.
                 */
                ConcertProgram.WAVE -> {

                    val wavePosition =
                        (colorStep / 360f) * size.width * 2f

                    val bandWidth = size.width * 0.65f

                    drawRect(
                        color = concertColor.copy(
                            alpha = screenAlpha
                        )
                    )

                    drawRect(
                        color = secondColor.copy(
                            alpha = 0.42f * factor
                        ),
                        topLeft = androidx.compose.ui.geometry.Offset(
                            x = wavePosition - bandWidth,
                            y = 0f
                        ),
                        size = androidx.compose.ui.geometry.Size(
                            width = bandWidth,
                            height = size.height
                        )
                    )

                    drawRect(
                        color = thirdColor.copy(
                            alpha = 0.32f * factor
                        ),
                        topLeft = androidx.compose.ui.geometry.Offset(
                            x = wavePosition - bandWidth * 2f,
                            y = 0f
                        ),
                        size = androidx.compose.ui.geometry.Size(
                            width = bandWidth,
                            height = size.height
                        )
                    )
                }

                /*
                 * STROBE BEAT
                 *
                 * Cada etapa ocupa la pantalla completa.
                 */
                ConcertProgram.STROBE -> {

                    drawRect(
                        color = concertColor.copy(
                            alpha = screenAlpha
                        )
                    )

                    /*
                     * Flash blanco periódico.
                     */
                    if (colorStep % 90f < 18f) {
                        drawRect(
                            color = Color.White.copy(
                                alpha = 0.75f * factor
                            )
                        )
                    }
                }

                /*
                 * SUPERNOVA
                 *
                 * Color intenso + explosiones blancas.
                 */
                ConcertProgram.SUPERNOVA -> {

                    drawRect(
                        color = concertColor.copy(
                            alpha = screenAlpha
                        )
                    )

                    /*
                     * Segunda capa de color.
                     */
                    if (pulseScale > 1.05f) {
                        drawRect(
                            color = secondColor.copy(
                                alpha = 0.28f * factor
                            )
                        )
                    }

                    /*
                     * Explosión blanca.
                     */
                    if (pulseScale > 1.18f) {
                        drawRect(
                            color = Color.White.copy(
                                alpha = 0.72f * factor
                            )
                        )
                    }
                }

                /*
                 * AURORA
                 *
                 * Tres capas de color que cambian lentamente.
                 */
                ConcertProgram.AURORA -> {

                    drawRect(
                        color = concertColor.copy(
                            alpha = screenAlpha
                        )
                    )

                    drawRect(
                        color = secondColor.copy(
                            alpha = 0.30f * factor
                        )
                    )

                    drawRect(
                        color = thirdColor.copy(
                            alpha = 0.22f * factor
                        )
                    )
                }
            }
        }

        /*
         * FLASH EXTRA PARA SUPERNOVA
         */
        if (
            isConcertActive &&
            concertProgram == ConcertProgram.SUPERNOVA &&
            pulseScale > 1.20f
        ) {
            drawRect(
                color = Color.White.copy(
                    alpha = 0.30f * factor
                )
            )
        }
    }
}
