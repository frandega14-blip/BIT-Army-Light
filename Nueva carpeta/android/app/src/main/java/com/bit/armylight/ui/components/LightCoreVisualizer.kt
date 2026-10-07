package com.bit.armylight.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.bit.armylight.viewmodel.ConcertProgram
import com.bit.armylight.viewmodel.LightIntensity
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LightCoreVisualizer(
    isPulseActive: Boolean,
    isConcertActive: Boolean,
    concertProgram: ConcertProgram,
    intensity: LightIntensity,
    isBatterySaver: Boolean,
    isRhythmActive: Boolean = false,
    rhythmLevel: Float = 0f,
    onPeakPulse: () -> Unit
) {

    /*
     * =========================================================
     * ANIMACIONES
     * =========================================================
     */

    val infiniteTransition =
        rememberInfiniteTransition(
            label = "BIT-Light"
        )

    /*
     * Respiración suave de la estrella.
     */
    val breathing by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 1800,
                        easing = FastOutSlowInEasing
                    ),
                repeatMode = RepeatMode.Reverse
            ),
        label = "breathing"
    )

    /*
     * Rotación de las órbitas.
     */
    val orbitRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 7000,
                        easing = FastOutSlowInEasing
                    ),
                repeatMode = RepeatMode.Restart
            ),
        label = "orbit"
    )

    /*
     * Pulso visual.
     */
    val pulseAnimation by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.18f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 700,
                        easing = FastOutSlowInEasing
                    ),
                repeatMode = RepeatMode.Reverse
            ),
        label = "pulse"
    )

    /*
     * =========================================================
     * RITMO
     * =========================================================
     */

    val rhythm =
        if (isRhythmActive) {
            rhythmLevel.coerceIn(
                0f,
                1f
            )
        } else {
            0f
        }

    /*
     * =========================================================
     * INTENSIDAD
     * =========================================================
     */

    val intensityFactor =
        intensity.factor.coerceIn(
            0.3f,
            1f
        )

    /*
     * El ritmo hace que el símbolo crezca ligeramente
     * cuando detecta sonido.
     */
    val rhythmScale =
        if (isRhythmActive) {
            1f + rhythm * 0.20f
        } else {
            1f
        }

    /*
     * Si Pulso está activo usamos la animación rápida.
     * De lo contrario usamos una respiración suave.
     */
    val baseScale =
        if (isPulseActive) {
            pulseAnimation
        } else {
            breathing
        }

    val finalScale =
        baseScale *
            rhythmScale *
            (
                0.92f +
                    intensityFactor * 0.08f
            )

    /*
     * Ahorro OLED reduce el brillo.
     */
    val baseAlpha =
        if (isBatterySaver) {
            0.55f
        } else {
            0.95f
        }

    /*
     * =========================================================
     * COLOR DEL SÍMBOLO
     * =========================================================
     */

    val mainColor =
        when {

            /*
             * Ritmo:
             * el color responde ligeramente al nivel
             * del sonido.
             */
            isRhythmActive -> {

                Color(
                    red = 0.82f,
                    green =
                        0.30f +
                            rhythm * 0.35f,
                    blue = 1f
                )
            }

            /*
             * Luz normal.
             */
            !isConcertActive -> {

                Color(
                    0xFFE6B8FF
                )
            }

            /*
             * Programas de concierto.
             */
            else -> {

                when (
                    concertProgram.title.lowercase()
                ) {

                    "onda púrpura" ->
                        Color(
                            0xFFD58CFF
                        )

                    "strobe beat" ->
                        Color(
                            0xFFF4D9FF
                        )

                    "supernova" ->
                        Color(
                            0xFFE7C7FF
                        )

                    "aurora" ->
                        Color(
                            0xFFBFA7FF
                        )

                    else ->
                        Color(
                            0xFFD58CFF
                        )
                }
            }
        }

    /*
     * Intensidad del halo cuando hay ritmo.
     */
    val rhythmGlow =
        if (isRhythmActive) {
            rhythm * 0.45f
        } else {
            0f
        }

    /*
     * =========================================================
     * DETECCIÓN DE PICOS PARA VIBRACIÓN
     * =========================================================
     */

    var lastPeak by remember {
        mutableLongStateOf(0L)
    }

    LaunchedEffect(
        isRhythmActive,
        rhythmLevel,
        isPulseActive,
        isConcertActive
    ) {

        if (
            isRhythmActive &&
            rhythmLevel > 0.78f
        ) {

            val now =
                System.currentTimeMillis()

            if (
                now - lastPeak >
                350L
            ) {

                lastPeak = now

                onPeakPulse()
            }
        }

        delay(30L)
    }

    /*
     * =========================================================
     * VISUALIZADOR
     * =========================================================
     */

    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {

        Canvas(
            modifier =
                Modifier.fillMaxSize()
        ) {

            /*
             * Posición central del símbolo.
             *
             * 0.43f lo coloca en la zona central superior,
             * dejando espacio para el panel inferior.
             */
            val center =
                Offset(
                    x =
                        size.width / 2f,
                    y =
                        size.height * 0.43f
                )

            /*
             * Tamaño principal.
             */
            val symbolRadius =
                size.minDimension * 0.22f

            /*
             * =================================================
             * HALO EXTERIOR
             * =================================================
             */

            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                mainColor.copy(
                                    alpha =
                                        (
                                            0.28f +
                                                rhythmGlow
                                            )
                                            .coerceAtMost(
                                                0.55f
                                            )
                                ),
                                mainColor.copy(
                                    alpha = 0.12f
                                ),
                                Color.Transparent
                            ),
                        center = center,
                        radius =
                            symbolRadius *
                                2.35f *
                                finalScale
                    ),
                center = center,
                radius =
                    symbolRadius *
                        2.35f *
                        finalScale
            )

            /*
             * =================================================
             * HALO INTERIOR
             * =================================================
             */

            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                Color.White.copy(
                                    alpha =
                                        0.22f *
                                            baseAlpha
                                ),
                                mainColor.copy(
                                    alpha =
                                        0.20f *
                                            baseAlpha
                                ),
                                Color.Transparent
                            ),
                        center = center,
                        radius =
                            symbolRadius *
                                1.45f *
                                finalScale
                    ),
                center = center,
                radius =
                    symbolRadius *
                        1.45f *
                        finalScale
            )

            /*
             * =================================================
             * ÓRBITA PRINCIPAL
             * =================================================
             */

            drawOrbit(
                center = center,
                radiusX =
                    symbolRadius *
                        1.62f *
                        finalScale,
                radiusY =
                    symbolRadius *
                        0.62f *
                        finalScale,
                rotationDegrees =
                    -18f +
                        orbitRotation * 0.02f,
                color =
                    Color.White.copy(
                        alpha =
                            0.92f *
                                baseAlpha
                    )
            )

            /*
             * =================================================
             * SEGUNDA ÓRBITA
             * =================================================
             */

            drawOrbit(
                center = center,
                radiusX =
                    symbolRadius *
                        1.45f *
                        finalScale,
                radiusY =
                    symbolRadius *
                        0.48f *
                        finalScale,
                rotationDegrees =
                    155f +
                        orbitRotation * 0.015f,
                color =
                    mainColor.copy(
                        alpha =
                            0.42f *
                                baseAlpha
                    )
            )

            /*
             * =================================================
             * ESTRELLA CENTRAL
             * =================================================
             */

            drawFourPointStar(
                center = center,
                radius =
                    symbolRadius *
                        finalScale,
                color = mainColor,
                alpha = baseAlpha
            )

            /*
             * =================================================
             * CENTRO DE LA ESTRELLA
             * =================================================
             */

            drawCircle(
                color =
                    Color.White.copy(
                        alpha =
                            0.92f *
                                baseAlpha
                    ),
                center = center,
                radius =
                    symbolRadius *
                        0.12f *
                        finalScale
            )

            /*
             * =================================================
             * DESTELLOS
             * =================================================
             */

            drawSparkle(
                center =
                    center +
                        Offset(
                            x =
                                -symbolRadius *
                                    1.15f *
                                    finalScale,
                            y =
                                -symbolRadius *
                                    0.85f *
                                    finalScale
                        ),
                radius =
                    symbolRadius *
                        0.22f *
                        finalScale,
                color =
                    Color.White.copy(
                        alpha =
                            0.95f *
                                baseAlpha
                    )
            )

            drawSparkle(
                center =
                    center +
                        Offset(
                            x =
                                symbolRadius *
                                    1.15f *
                                    finalScale,
                            y =
                                -symbolRadius *
                                    0.65f *
                                    finalScale
                        ),
                radius =
                    symbolRadius *
                        0.17f *
                        finalScale,
                color =
                    Color.White.copy(
                        alpha =
                            0.90f *
                                baseAlpha
                    )
            )

            drawSparkle(
                center =
                    center +
                        Offset(
                            x =
                                symbolRadius *
                                    1.10f *
                                    finalScale,
                            y =
                                symbolRadius *
                                    0.95f *
                                    finalScale
                        ),
                radius =
                    symbolRadius *
                        0.15f *
                        finalScale,
                color =
                    mainColor.copy(
                        alpha =
                            0.90f *
                                baseAlpha
                    )
            )

            drawSparkle(
                center =
                    center +
                        Offset(
                            x =
                                -symbolRadius *
                                    1.05f *
                                    finalScale,
                            y =
                                symbolRadius *
                                    0.85f *
                                    finalScale
                        ),
                radius =
                    symbolRadius *
                        0.12f *
                        finalScale,
                color =
                    Color.White.copy(
                        alpha =
                            0.85f *
                                baseAlpha
                    )
            )

            /*
             * =================================================
             * DESTELLOS EXTRA CON RITMO
             * =================================================
             */

            if (
                isRhythmActive &&
                rhythm > 0.35f
            ) {

                val extraAlpha =
                    (
                        rhythm *
                            0.85f
                        )
                        .coerceIn(
                            0f,
                            0.85f
                        )

                drawSparkle(
                    center =
                        center +
                            Offset(
                                x = 0f,
                                y =
                                    -symbolRadius *
                                        1.45f *
                                        finalScale
                            ),
                    radius =
                        symbolRadius *
                            (
                                0.10f +
                                    rhythm *
                                        0.12f
                                ) *
                            finalScale,
                    color =
                        Color.White.copy(
                            alpha =
                                extraAlpha
                        )
                )

                drawSparkle(
                    center =
                        center +
                            Offset(
                                x = 0f,
                                y =
                                    symbolRadius *
                                        1.45f *
                                        finalScale
                            ),
                    radius =
                        symbolRadius *
                            (
                                0.08f +
                                    rhythm *
                                        0.10f
                                ) *
                            finalScale,
                    color =
                        mainColor.copy(
                            alpha =
                                extraAlpha
                        )
                )
            }
        }
    }
}


/*
 * =============================================================
 * ESTRELLA DE CUATRO PUNTAS
 * =============================================================
 *
 * Este es el nuevo símbolo propio de BIT.
 */
private fun DrawScope.drawFourPointStar(
    center: Offset,
    radius: Float,
    color: Color,
    alpha: Float
) {

    val outerRadius =
        radius

    val innerRadius =
        radius * 0.16f

    val path =
        Path()

    for (i in 0 until 8) {

        val angle =
            (
                -90f +
                    i * 45f
                )
                .toRadians()

        val currentRadius =
            if (i % 2 == 0) {
                outerRadius
            } else {
                innerRadius
            }

        val x =
            center.x +
                cos(angle) *
                    currentRadius

        val y =
            center.y +
                sin(angle) *
                    currentRadius

        if (i == 0) {

            path.moveTo(
                x.toFloat(),
                y.toFloat()
            )

        } else {

            path.lineTo(
                x.toFloat(),
                y.toFloat()
            )
        }
    }

    path.close()

    /*
     * Resplandor externo.
     */
    drawPath(
        path = path,
        color =
            color.copy(
                alpha =
                    0.24f *
                        alpha
            ),
        style =
            Stroke(
                width =
                    radius * 0.16f,
                join =
                    StrokeJoin.Round
            )
    )

    /*
     * Cuerpo principal con degradado.
     */
    drawPath(
        path = path,
        brush =
            Brush.linearGradient(
                colors =
                    listOf(
                        Color.White.copy(
                            alpha =
                                0.98f *
                                    alpha
                        ),
                        color.copy(
                            alpha =
                                0.98f *
                                    alpha
                        ),
                        Color(
                            red = 0.45f,
                            green = 0.12f,
                            blue = 0.95f,
                            alpha =
                                0.98f *
                                    alpha
                        )
                    ),
                start =
                    Offset(
                        center.x - radius,
                        center.y - radius
                    ),
                end =
                    Offset(
                        center.x + radius,
                        center.y + radius
                    )
            )
    )
}


/*
 * =============================================================
 * ÓRBITA ELÍPTICA
 * =============================================================
 */
private fun DrawScope.drawOrbit(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
    rotationDegrees: Float,
    color: Color
) {

    rotate(
        degrees =
            rotationDegrees,
        pivot = center
    ) {

        drawOval(
            color = color,
            topLeft =
                Offset(
                    center.x - radiusX,
                    center.y - radiusY
                ),
            size =
                androidx.compose.ui.geometry.Size(
                    radiusX * 2f,
                    radiusY * 2f
                ),
            style =
                Stroke(
                    width =
                        5.dp.toPx(),
                    cap =
                        StrokeCap.Round
                )
        )
    }
}


/*
 * =============================================================
 * DESTELLO DE CUATRO PUNTAS
 * =============================================================
 */
private fun DrawScope.drawSparkle(
    center: Offset,
    radius: Float,
    color: Color
) {

    val path =
        Path()

    val longRadius =
        radius

    val shortRadius =
        radius * 0.18f

    path.moveTo(
        center.x,
        center.y - longRadius
    )

    path.lineTo(
        center.x + shortRadius,
        center.y - shortRadius
    )

    path.lineTo(
        center.x + longRadius,
        center.y
    )

    path.lineTo(
        center.x + shortRadius,
        center.y + shortRadius
    )

    path.lineTo(
        center.x,
        center.y + longRadius
    )

    path.lineTo(
        center.x - shortRadius,
        center.y + shortRadius
    )

    path.lineTo(
        center.x - longRadius,
        center.y
    )

    path.lineTo(
        center.x - shortRadius,
        center.y - shortRadius
    )

    path.close()

    drawPath(
        path = path,
        color = color
    )
}


/*
 * =============================================================
 * GRADOS → RADIANES
 * =============================================================
 */
private fun Float.toRadians(): Double {
    return this *
        Math.PI /
        180.0
}
