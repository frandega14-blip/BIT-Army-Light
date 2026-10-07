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
     * ============================================================
     * ANIMACIONES
     * ============================================================
     */

    val infiniteTransition =
        rememberInfiniteTransition(
            label = "BIT-Light"
        )

    /*
     * Respiración suave de la figura central.
     */
    val breathing by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
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
     * Pulso visual.
     */
    val pulseAnimation by infiniteTransition.animateFloat(
        initialValue = 0.82f,
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
     * Movimiento general del fondo.
     */
    val backgroundPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 4200,
                        easing = FastOutSlowInEasing
                    ),
                repeatMode = RepeatMode.Restart
            ),
        label = "backgroundPhase"
    )

    /*
     * Movimiento rápido para Strobe Beat.
     */
    val strobePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 420,
                        easing = FastOutSlowInEasing
                    ),
                repeatMode = RepeatMode.Restart
            ),
        label = "strobePhase"
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
     * ============================================================
     * VALORES BASE
     * ============================================================
     */

    val rhythm =
        if (isRhythmActive) {
            rhythmLevel.coerceIn(0f, 1f)
        } else {
            0f
        }

    val intensityFactor =
        intensity.factor.coerceIn(
            0.3f,
            1f
        )

    /*
     * La figura puede respirar o pulsar,
     * pero NO cambia de color.
     */
    val finalScale =
        (
            if (isPulseActive) {
                pulseAnimation
            } else {
                breathing
            }
        ) *
        (
            if (isRhythmActive) {
                1f + rhythm * 0.18f
            } else {
                1f
            }
        ) *
        (
            0.96f +
                intensityFactor * 0.04f
        )

    /*
     * Color FIJO de la figura.
     */
    val figureColor =
        Color(0xFFF8F2FF)

    val figureSecondary =
        Color(0xFFE7D2FF)

    val baseAlpha =
        if (isBatterySaver) {
            0.72f
        } else {
            1f
        }


    /*
     * ============================================================
     * VIBRACIÓN
     * ============================================================
     *
     * RITMO:
     *   Vibra cuando se detecta un pico de audio.
     *
     * PULSO:
     *   Vibra siguiendo el ciclo del pulso.
     *
     * CONCIERTO:
     *   Vibra siguiendo el patrón del espectáculo.
     *
     * El callback onPeakPulse() ya está protegido por
     * ArmyLightScreen mediante uiState.isVibrationEnabled.
     */

    var lastPeak by remember {
        mutableLongStateOf(0L)
    }

    /*
     * RITMO:
     * vibración por pico de audio.
     */
    LaunchedEffect(
        isRhythmActive,
        rhythmLevel
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
    }

    /*
     * PULSO / CONCIERTO:
     * vibración automática.
     *
     * No se ejecuta en Ritmo para evitar que el patrón
     * automático interfiera con la detección del micrófono.
     */
    LaunchedEffect(
        isRhythmActive,
        isPulseActive,
        isConcertActive,
        concertProgram
    ) {

        if (isRhythmActive) {
            return@LaunchedEffect
        }

        if (
            !isPulseActive &&
            !isConcertActive
        ) {
            return@LaunchedEffect
        }

        val interval =
            if (isConcertActive) {
                320L
            } else {
                700L
            }

        while (true) {

            onPeakPulse()

            delay(interval)
        }
    }


    /*
     * ============================================================
     * CANVAS
     * ============================================================
     */

    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {

        Canvas(
            modifier =
                Modifier.fillMaxSize()
        ) {

            val center =
                Offset(
                    x = size.width / 2f,
                    y = size.height * 0.43f
                )

            val symbolRadius =
                size.minDimension * 0.20f


            /*
             * ====================================================
             * FONDO DINÁMICO
             * ====================================================
             *
             * La figura central permanece estable.
             * El fondo cambia según el modo.
             */

            when {

                /*
                 * =================================================
                 * RITMO
                 * =================================================
                 */

                isRhythmActive -> {

                    val rhythmColor =
                        mixColor(
                            Color(0xFF12001F),
                            Color(0xFF9C27FF),
                            rhythm
                        )

                    val rhythmColor2 =
                        mixColor(
                            Color(0xFF210033),
                            Color(0xFFE100FF),
                            rhythm * 0.85f
                        )

                    drawRect(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        rhythmColor2.copy(
                                            alpha =
                                                (
                                                    0.35f +
                                                        rhythm * 0.45f
                                                ).coerceAtMost(
                                                    0.85f
                                                )
                                        ),
                                        rhythmColor,
                                        Color(0xFF050008)
                                    ),
                                center = center,
                                radius =
                                    size.maxDimension *
                                        (
                                            0.65f +
                                                rhythm * 0.45f
                                        )
                            )
                    )
                }


                /*
                 * =================================================
                 * CONCIERTO
                 * =================================================
                 */

                isConcertActive -> {

                    when (
                        concertProgram.title.lowercase()
                    ) {

                        /*
                         * ONDA PÚRPURA
                         */
                        "onda púrpura" -> {

                            val wave =
                                backgroundPhase

                            val purple =
                                Color(0xFF7B00FF)

                            val magenta =
                                Color(0xFFD000FF)

                            val blue =
                                Color(0xFF3B5BFF)

                            drawRect(
                                brush =
                                    Brush.linearGradient(
                                        colors =
                                            if (
                                                wave < 0.5f
                                            ) {
                                                listOf(
                                                    purple,
                                                    magenta,
                                                    blue,
                                                    purple
                                                )
                                            } else {
                                                listOf(
                                                    blue,
                                                    purple,
                                                    magenta,
                                                    blue
                                                )
                                            },
                                        start =
                                            Offset(
                                                x =
                                                    size.width *
                                                        wave,
                                                y = 0f
                                            ),
                                        end =
                                            Offset(
                                                x =
                                                    size.width *
                                                        (
                                                            1f -
                                                                wave
                                                        ),
                                                y =
                                                    size.height
                                            )
                                    )
                            )
                        }


                        /*
                         * STROBE BEAT
                         */
                        "strobe beat" -> {

                            val flash =
                                strobePhase < 0.22f

                            val color =
                                if (flash) {
                                    Color.White
                                } else {
                                    Color(0xFF6500FF)
                                }

                            drawRect(
                                color =
                                    color
                            )

                            if (!flash) {

                                drawRect(
                                    brush =
                                        Brush.radialGradient(
                                            colors =
                                                listOf(
                                                    Color(
                                                        0xFFE000FF
                                                    ).copy(
                                                        alpha =
                                                            0.55f
                                                    ),
                                                    Color.Transparent
                                                ),
                                            center =
                                                center,
                                            radius =
                                                size.maxDimension *
                                                    0.75f
                                        )
                                )
                            }
                        }


                        /*
                         * SUPERNOVA
                         */
                        "supernova" -> {

                            val phase =
                                backgroundPhase

                            val explosion =
                                (
                                    sin(
                                        phase *
                                            Math.PI *
                                            2.0
                                    ) *
                                    0.5 +
                                    0.5
                                ).toFloat()

                            drawRect(
                                brush =
                                    Brush.radialGradient(
                                        colors =
                                            listOf(
                                                Color.White.copy(
                                                    alpha =
                                                        0.35f +
                                                            explosion *
                                                            0.45f
                                                ),
                                                Color(0xFFB000FF).copy(
                                                    alpha =
                                                        0.65f
                                                ),
                                                Color(0xFF33005A),
                                                Color(0xFF050008)
                                            ),
                                        center =
                                            center,
                                        radius =
                                            size.maxDimension *
                                                (
                                                    0.45f +
                                                        explosion *
                                                        0.65f
                                                )
                                    )
                            )
                        }


                        /*
                         * AURORA
                         */
                        "aurora" -> {

                            val phase =
                                backgroundPhase

                            drawRect(
                                brush =
                                    Brush.linearGradient(
                                        colors =
                                            listOf(
                                                Color(0xFF24005A),
                                                Color(0xFF6A00A8),
                                                Color(0xFF008CFF),
                                                Color(0xFF00D4A8),
                                                Color(0xFF6A00A8),
                                                Color(0xFF24005A)
                                            ),
                                        start =
                                            Offset(
                                                x =
                                                    -size.width +
                                                        size.width *
                                                        3f *
                                                        phase,
                                                y = 0f
                                            ),
                                        end =
                                            Offset(
                                                x =
                                                    size.width *
                                                        3f *
                                                        phase,
                                                y =
                                                    size.height
                                            )
                                    )
                            )
                        }


                        else -> {

                            drawRect(
                                color =
                                    Color(0xFF250035)
                            )
                        }
                    }
                }


                /*
                 * =================================================
                 * PULSO
                 * =================================================
                 */

                isPulseActive -> {

                    val pulseAmount =
                        (
                            pulseAnimation -
                                0.82f
                        ) /
                        (
                            1.18f -
                                0.82f
                        )

                    val pulseColor =
                        mixColor(
                            Color(0xFF26003D),
                            Color(0xFFB000FF),
                            pulseAmount
                        )

                    drawRect(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        Color(0xFFE9B8FF).copy(
                                            alpha =
                                                0.20f +
                                                    pulseAmount *
                                                    0.45f
                                        ),
                                        pulseColor,
                                        Color(0xFF050008)
                                    ),
                                center =
                                    center,
                                radius =
                                    size.maxDimension *
                                        (
                                            0.60f +
                                                pulseAmount *
                                                0.35f
                                        )
                            )
                    )
                }


                /*
                 * =================================================
                 * ESTADO NORMAL
                 * =================================================
                 */

                else -> {

                    drawRect(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        Color(0xFF2A003D),
                                        Color(0xFF100018),
                                        Color(0xFF050008)
                                    ),
                                center =
                                    center,
                                radius =
                                    size.maxDimension *
                                        0.72f
                            )
                    )
                }
            }


            /*
             * ====================================================
             * HALO DE LA FIGURA
             * ====================================================
             */

            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                figureColor.copy(
                                    alpha =
                                        0.30f *
                                            baseAlpha
                                ),
                                figureSecondary.copy(
                                    alpha =
                                        0.16f *
                                            baseAlpha
                                ),
                                Color.Transparent
                            ),
                        center =
                            center,
                        radius =
                            symbolRadius *
                                2.35f *
                                finalScale
                    ),
                center =
                    center,
                radius =
                    symbolRadius *
                        2.35f *
                        finalScale
            )


            /*
             * ====================================================
             * HALO INTERIOR
             * ====================================================
             */

            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                Color.White.copy(
                                    alpha =
                                        0.25f *
                                            baseAlpha
                                ),
                                figureSecondary.copy(
                                    alpha =
                                        0.18f *
                                            baseAlpha
                                ),
                                Color.Transparent
                            ),
                        center =
                            center,
                        radius =
                            symbolRadius *
                                1.45f *
                                finalScale
                    ),
                center =
                    center,
                radius =
                    symbolRadius *
                        1.45f *
                        finalScale
            )


            /*
             * ====================================================
             * ÓRBITA PRINCIPAL
             * ====================================================
             */

            drawOrbit(
                center =
                    center,
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
                            0.90f *
                                baseAlpha
                    )
            )


            /*
             * ====================================================
             * SEGUNDA ÓRBITA
             * ====================================================
             */

            drawOrbit(
                center =
                    center,
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
                    figureSecondary.copy(
                        alpha =
                            0.48f *
                                baseAlpha
                    )
            )


            /*
             * ====================================================
             * FIGURA CENTRAL
             * ====================================================
             *
             * BLANCA/LAVANDA.
             * NO CAMBIA CON EL COLOR DEL FONDO.
             */

            drawFourPointStar(
                center =
                    center,
                radius =
                    symbolRadius *
                        finalScale,
                color =
                    figureColor,
                alpha =
                    baseAlpha
            )


            /*
             * ====================================================
             * CENTRO
             * ====================================================
             */

            drawCircle(
                color =
                    Color.White.copy(
                        alpha =
                            0.96f *
                                baseAlpha
                    ),
                center =
                    center,
                radius =
                    symbolRadius *
                        0.12f *
                        finalScale
            )


            /*
             * ====================================================
             * DESTELLOS
             * ====================================================
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
                    figureSecondary.copy(
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
             * ====================================================
             * DESTELLOS EXTRA EN RITMO
             * ====================================================
             */

            if (
                isRhythmActive &&
                rhythm > 0.35f
            ) {

                val extraAlpha =
                    (
                        rhythm *
                            0.85f
                    ).coerceIn(
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
                                    rhythm * 0.12f
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
                                    rhythm * 0.10f
                            ) *
                            finalScale,
                    color =
                        figureSecondary.copy(
                            alpha =
                                extraAlpha
                        )
                )
            }
        }
    }
}


/*
 * ================================================================
 * ESTRELLA CENTRAL
 * ================================================================
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
            (-90f + i * 45f)
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
     * Resplandor exterior.
     */

    drawPath(
        path =
            path,
        color =
            Color.White.copy(
                alpha =
                    0.28f *
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
     * Cuerpo principal.
     */

    drawPath(
        path =
            path,
        brush =
            Brush.linearGradient(
                colors =
                    listOf(
                        Color.White.copy(
                            alpha =
                                0.99f *
                                    alpha
                        ),
                        color.copy(
                            alpha =
                                0.98f *
                                    alpha
                        ),
                        Color(0xFFD9B8FF).copy(
                            alpha =
                                0.98f *
                                    alpha
                        )
                    ),
                start =
                    Offset(
                        center.x -
                            radius,
                        center.y -
                            radius
                    ),
                end =
                    Offset(
                        center.x +
                            radius,
                        center.y +
                            radius
                    )
            )
    )
}


/*
 * ================================================================
 * ÓRBITA
 * ================================================================
 *
 * CORRECCIÓN:
 * No se utilizan rotation/center dentro de drawOval().
 *
 * Se utiliza rotate { drawOval(...) }, que sí es compatible
 * con la API de Compose utilizada por el proyecto.
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
        pivot =
            center
    ) {

        drawOval(
            color =
                color,
            topLeft =
                Offset(
                    center.x -
                        radiusX,
                    center.y -
                        radiusY
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
 * ================================================================
 * DESTELLO
 * ================================================================
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
        center.y -
            longRadius
    )

    path.lineTo(
        center.x +
            shortRadius,
        center.y -
            shortRadius
    )

    path.lineTo(
        center.x +
            longRadius,
        center.y
    )

    path.lineTo(
        center.x +
            shortRadius,
        center.y +
            shortRadius
    )

    path.lineTo(
        center.x,
        center.y +
            longRadius
    )

    path.lineTo(
        center.x -
            shortRadius,
        center.y +
            shortRadius
    )

    path.lineTo(
        center.x -
            longRadius,
        center.y
    )

    path.lineTo(
        center.x -
            shortRadius,
        center.y -
            shortRadius
    )

    path.close()

    drawPath(
        path =
            path,
        color =
            color
    )
}


/*
 * ================================================================
 * MEZCLA DE COLORES
 * ================================================================
 */

private fun mixColor(
    start: Color,
    end: Color,
    amount: Float
): Color {

    val t =
        amount.coerceIn(
            0f,
            1f
        )

    return Color(
        red =
            start.red +
                (
                    end.red -
                        start.red
                ) * t,

        green =
            start.green +
                (
                    end.green -
                        start.green
                ) * t,

        blue =
            start.blue +
                (
                    end.blue -
                        start.blue
                ) * t,

        alpha =
            start.alpha +
                (
                    end.alpha -
                        start.alpha
                ) * t
    )
}


/*
 * ================================================================
 * GRADOS → RADIANES
 * ================================================================
 */

private fun Float.toRadians(): Double {

    return this *
        Math.PI /
        180.0
}
