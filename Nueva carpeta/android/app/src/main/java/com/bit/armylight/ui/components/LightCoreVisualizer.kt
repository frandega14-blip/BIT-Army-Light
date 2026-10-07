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
     * ANIMACIONES GENERALES
     * =========================================================
     */

    val infiniteTransition =
        rememberInfiniteTransition(
            label = "BIT-Light"
        )

    /*
     * Respiración normal.
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
     * Pulso.
     */
    val pulseAnimation by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.16f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 650,
                        easing = FastOutSlowInEasing
                    ),
                repeatMode = RepeatMode.Reverse
            ),
        label = "pulse"
    )

    /*
     * Rotación de la órbita.
     */
    val orbitRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 6500,
                        easing = FastOutSlowInEasing
                    ),
                repeatMode = RepeatMode.Restart
            ),
        label = "orbit"
    )

    /*
     * =========================================================
     * ANIMACIÓN MULTICOLOR
     * =========================================================
     */

    val colorDuration =
        when {
            !isConcertActive -> 5000

            concertProgram.title
                .equals(
                    "Strobe Beat",
                    ignoreCase = true
                ) -> 500

            concertProgram.title
                .equals(
                    "Supernova",
                    ignoreCase = true
                ) -> 1100

            concertProgram.title
                .equals(
                    "Aurora",
                    ignoreCase = true
                ) -> 4200

            else -> 2600
        }

    val colorProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis =
                            colorDuration,
                        easing =
                            FastOutSlowInEasing
                    ),
                repeatMode =
                    RepeatMode.Restart
            ),
        label = "concert-colors"
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
     * El ritmo aumenta ligeramente el tamaño.
     */
    val rhythmScale =
        if (isRhythmActive) {
            1f +
                rhythm *
                0.24f
        } else {
            1f
        }

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
                0.94f +
                    intensityFactor *
                    0.06f
            )

    /*
     * =========================================================
     * PALETA MULTICOLOR
     * =========================================================
     */

    val palette =
        listOf(
            Color(0xFF9C27FF),
            Color(0xFFFF2DAA),
            Color(0xFFFF4FD8),
            Color(0xFF6A5CFF),
            Color(0xFF3FA9FF),
            Color(0xFF00D9FF),
            Color(0xFF9C27FF)
        )

    /*
     * Índice de color.
     */
    val scaledProgress =
        colorProgress *
            (palette.size - 1)

    val colorIndex =
        scaledProgress
            .toInt()
            .coerceIn(
                0,
                palette.size - 2
            )

    val localProgress =
        scaledProgress -
            colorIndex

    val animatedConcertColor =
        lerpColor(
            palette[colorIndex],
            palette[colorIndex + 1],
            localProgress
        )

    /*
     * Color principal.
     */
    val mainColor =
        when {

            /*
             * Ritmo:
             * reacciona al sonido.
             */
            isRhythmActive -> {

                Color.hsv(
                    hue =
                        270f +
                            rhythm * 70f,
                    saturation = 0.72f,
                    value = 1f
                )
            }

            /*
             * Concierto:
             * MULTICOLOR REAL.
             */
            isConcertActive -> {

                when {

                    concertProgram.title.equals(
                        "Strobe Beat",
                        ignoreCase = true
                    ) -> {

                        if (
                            colorProgress > 0.82f
                        ) {
                            Color.White
                        } else {
                            animatedConcertColor
                        }
                    }

                    concertProgram.title.equals(
                        "Supernova",
                        ignoreCase = true
                    ) -> {

                        animatedConcertColor
                    }

                    concertProgram.title.equals(
                        "Aurora",
                        ignoreCase = true
                    ) -> {

                        animatedConcertColor
                    }

                    else -> {

                        animatedConcertColor
                    }
                }
            }

            /*
             * Luz normal.
             */
            else -> {

                Color(0xFFE8C5FF)
            }
        }

    /*
     * =========================================================
     * BRILLO
     * =========================================================
     */

    val baseAlpha =
        if (isBatterySaver) {
            0.52f
        } else {
            0.96f
        }

    /*
     * Flash del programa Strobe.
     */
    val strobeFlash =
        if (
            isConcertActive &&
            concertProgram.title.equals(
                "Strobe Beat",
                ignoreCase = true
            )
        ) {

            if (
                colorProgress > 0.82f
            ) {
                1f
            } else {
                0f
            }

        } else {
            0f
        }

    /*
     * Explosión del programa Supernova.
     */
    val supernovaFlash =
        if (
            isConcertActive &&
            concertProgram.title.equals(
                "Supernova",
                ignoreCase = true
            )
        ) {

            val wave =
                sin(
                    colorProgress *
                        Math.PI *
                        2.0
                )
                    .toFloat()

            (
                wave
                    .coerceAtLeast(0f)
                    *
                    0.75f
            )

        } else {
            0f
        }

    /*
     * Brillo final.
     */
    val glowBoost =
        (
            1f +
                strobeFlash *
                0.75f +
                supernovaFlash *
                0.65f +
                rhythm *
                0.35f
            )
            .coerceIn(
                1f,
                2.5f
            )

    /*
     * =========================================================
     * PICOS DE RITMO
     * =========================================================
     */

    var lastPeak by remember {
        mutableLongStateOf(0L)
    }

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

                lastPeak =
                    now

                onPeakPulse()
            }
        }

        delay(30L)
    }

    /*
     * =========================================================
     * CANVAS
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
             * Posición central.
             */
            val center =
                Offset(
                    x =
                        size.width / 2f,
                    y =
                        size.height * 0.43f
                )

            /*
             * Tamaño del símbolo.
             */
            val symbolRadius =
                size.minDimension *
                    0.20f

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
                                Color.White.copy(
                                    alpha =
                                        0.22f *
                                            baseAlpha *
                                            glowBoost
                                ),
                                mainColor.copy(
                                    alpha =
                                        0.28f *
                                            baseAlpha *
                                            glowBoost
                                ),
                                mainColor.copy(
                                    alpha =
                                        0.08f *
                                            baseAlpha
                                ),
                                Color.Transparent
                            ),
                        center =
                            center,
                        radius =
                            symbolRadius *
                                2.65f *
                                finalScale
                    ),
                center =
                    center,
                radius =
                    symbolRadius *
                        2.65f *
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
                                        0.35f *
                                            baseAlpha *
                                            glowBoost
                                ),
                                mainColor.copy(
                                    alpha =
                                        0.24f *
                                            baseAlpha
                                ),
                                Color.Transparent
                            ),
                        center =
                            center,
                        radius =
                            symbolRadius *
                                1.55f *
                                finalScale
                    ),
                center =
                    center,
                radius =
                    symbolRadius *
                        1.55f *
                        finalScale
            )

            /*
             * =================================================
             * ÓRBITA PRINCIPAL
             * =================================================
             */

            drawOrbit(
                center =
                    center,
                radiusX =
                    symbolRadius *
                        1.72f *
                        finalScale,
                radiusY =
                    symbolRadius *
                        0.62f *
                        finalScale,
                rotationDegrees =
                    -18f +
                        orbitRotation *
                        0.08f,
                color =
                    Color.White.copy(
                        alpha =
                            0.90f *
                                baseAlpha
                    ),
                width =
                    symbolRadius *
                        0.075f
            )

            /*
             * =================================================
             * ÓRBITA DE COLOR
             * =================================================
             */

            drawOrbit(
                center =
                    center,
                radiusX =
                    symbolRadius *
                        1.52f *
                        finalScale,
                radiusY =
                    symbolRadius *
                        0.48f *
                        finalScale,
                rotationDegrees =
                    145f -
                        orbitRotation *
                        0.06f,
                color =
                    mainColor.copy(
                        alpha =
                            0.78f *
                                baseAlpha
                    ),
                width =
                    symbolRadius *
                        0.035f
            )

            /*
             * =================================================
             * ESTRELLA PRINCIPAL
             * =================================================
             */

            drawPremiumStar(
                center =
                    center,
                radius =
                    symbolRadius *
                        finalScale,
                color =
                    mainColor,
                alpha =
                    baseAlpha *
                        glowBoost.coerceAtMost(
                            1.8f
                        )
            )

            /*
             * =================================================
             * CENTRO LUMINOSO
             * =================================================
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
                        0.09f *
                        finalScale
            )

            /*
             * =================================================
             * DESTELLOS GRANDES
             * =================================================
             */

            drawSparkle(
                center =
                    center +
                        Offset(
                            x =
                                -symbolRadius *
                                    1.30f *
                                    finalScale,
                            y =
                                -symbolRadius *
                                    0.92f *
                                    finalScale
                        ),
                radius =
                    symbolRadius *
                        0.22f *
                        finalScale,
                color =
                    Color.White.copy(
                        alpha =
                            0.96f *
                                baseAlpha
                    )
            )

            drawSparkle(
                center =
                    center +
                        Offset(
                            x =
                                symbolRadius *
                                    1.28f *
                                    finalScale,
                            y =
                                -symbolRadius *
                                    0.72f *
                                    finalScale
                        ),
                radius =
                    symbolRadius *
                        0.18f *
                        finalScale,
                color =
                    Color.White.copy(
                        alpha =
                            0.94f *
                                baseAlpha
                    )
            )

            drawSparkle(
                center =
                    center +
                        Offset(
                            x =
                                symbolRadius *
                                    1.30f *
                                    finalScale,
                            y =
                                symbolRadius *
                                    0.92f *
                                    finalScale
                        ),
                radius =
                    symbolRadius *
                        0.16f *
                        finalScale,
                color =
                    mainColor.copy(
                        alpha =
                            0.92f *
                                baseAlpha
                    )
            )

            drawSparkle(
                center =
                    center +
                        Offset(
                            x =
                                -symbolRadius *
                                    1.18f *
                                    finalScale,
                            y =
                                symbolRadius *
                                    0.82f *
                                    finalScale
                        ),
                radius =
                    symbolRadius *
                        0.13f *
                        finalScale,
                color =
                    Color.White.copy(
                        alpha =
                            0.90f *
                                baseAlpha
                    )
            )

            /*
             * =================================================
             * DESTELLOS EXTRA EN RITMO
             * =================================================
             */

            if (
                isRhythmActive &&
                rhythm > 0.30f
            ) {

                val extraAlpha =
                    (
                        rhythm *
                            0.90f
                        )
                        .coerceIn(
                            0f,
                            0.90f
                        )

                drawSparkle(
                    center =
                        center +
                            Offset(
                                x = 0f,
                                y =
                                    -symbolRadius *
                                        1.48f *
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
                                        1.48f *
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

            /*
             * =================================================
             * FLASH SUPER NOVA
             * =================================================
             */

            if (
                supernovaFlash >
                0.05f
            ) {

                drawCircle(
                    color =
                        Color.White.copy(
                            alpha =
                                supernovaFlash *
                                    0.30f *
                                    baseAlpha
                        ),
                    center =
                        center,
                    radius =
                        symbolRadius *
                            (
                                2.2f +
                                    supernovaFlash
                            ) *
                            finalScale
                )
            }
        }
    }
}


/*
 * =============================================================
 * ESTRELLA PREMIUM
 * =============================================================
 *
 * Estrella de cuatro puntas con curvas suaves.
 */
private fun DrawScope.drawPremiumStar(
    center: Offset,
    radius: Float,
    color: Color,
    alpha: Float
) {

    val path =
        Path()

    val inner =
        radius * 0.18f

    val curve =
        radius * 0.38f

    /*
     * Punta superior.
     */
    path.moveTo(
        center.x,
        center.y - radius
    )

    /*
     * Lado superior derecho.
     */
    path.cubicTo(
        center.x + radius * 0.20f,
        center.y - radius * 0.54f,
        center.x + radius * 0.32f,
        center.y - radius * 0.30f,
        center.x + inner,
        center.y - inner
    )

    /*
     * Punta derecha.
     */
    path.cubicTo(
        center.x + radius * 0.30f,
        center.y - radius * 0.08f,
        center.x + radius * 0.58f,
        center.y + radius * 0.20f,
        center.x + radius,
        center.y
    )

    /*
     * Lado inferior derecho.
     */
    path.cubicTo(
        center.x + radius * 0.58f,
        center.y + radius * 0.20f,
        center.x + radius * 0.30f,
        center.y + radius * 0.38f,
        center.x + inner,
        center.y + inner
    )

    /*
     * Punta inferior.
     */
    path.cubicTo(
        center.x + radius * 0.16f,
        center.y + radius * 0.54f,
        center.x + radius * 0.08f,
        center.y + radius * 0.74f,
        center.x,
        center.y + radius
    )

    /*
     * Lado inferior izquierdo.
     */
    path.cubicTo(
        center.x - radius * 0.08f,
        center.y + radius * 0.74f,
        center.x - radius * 0.16f,
        center.y + radius * 0.54f,
        center.x - inner,
        center.y + inner
    )

    /*
     * Punta izquierda.
     */
    path.cubicTo(
        center.x - radius * 0.30f,
        center.y + radius * 0.38f,
        center.x - radius * 0.58f,
        center.y + radius * 0.20f,
        center.x - radius,
        center.y
    )

    /*
     * Lado superior izquierdo.
     */
    path.cubicTo(
        center.x - radius * 0.58f,
        center.y - radius * 0.20f,
        center.x - radius * 0.30f,
        center.y - radius * 0.08f,
        center.x - inner,
        center.y - inner
    )

    path.cubicTo(
        center.x - radius * 0.32f,
        center.y - radius * 0.30f,
        center.x - radius * 0.20f,
        center.y - radius * 0.54f,
        center.x,
        center.y - radius
    )

    path.close()

    /*
     * Glow.
     */
    drawPath(
        path =
            path,
        color =
            color.copy(
                alpha =
                    0.30f *
                        alpha
            ),
        style =
            Stroke(
                width =
                    radius *
                        0.18f,
                join =
                    StrokeJoin.Round
            )
    )

    /*
     * Cuerpo degradado.
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
                                0.98f *
                                    alpha
                        ),
                        color.copy(
                            alpha =
                                0.98f *
                                    alpha
                        ),
                        Color(
                            red = 0.38f,
                            green = 0.10f,
                            blue = 0.95f,
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

    /*
     * Línea luminosa vertical.
     */
    drawLine(
        color =
            Color.White.copy(
                alpha =
                    0.35f *
                        alpha
            ),
        start =
            Offset(
                center.x,
                center.y -
                    radius *
                    0.68f
            ),
        end =
            Offset(
                center.x,
                center.y +
                    radius *
                    0.68f
            ),
        strokeWidth =
            radius *
                0.018f,
        cap =
            StrokeCap.Round
    )
}


/*
 * =============================================================
 * ÓRBITA
 * =============================================================
 */
private fun DrawScope.drawOrbit(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
    rotationDegrees: Float,
    color: Color,
    width: Float
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
                        width,
                    cap =
                        StrokeCap.Round
                )
        )
    }
}


/*
 * =============================================================
 * DESTELLO
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
        radius *
            0.16f

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
 * =============================================================
 * INTERPOLACIÓN DE COLORES
 * =============================================================
 */
private fun lerpColor(
    start: Color,
    end: Color,
    fraction: Float
): Color {

    val t =
        fraction.coerceIn(
            0f,
            1f
        )

    return Color(
        red =
            start.red +
                (
                    end.red -
                        start.red
                    ) *
                t,
        green =
            start.green +
                (
                    end.green -
                        start.green
                    ) *
                t,
        blue =
            start.blue +
                (
                    end.blue -
                        start.blue
                    ) *
                t,
        alpha =
            start.alpha +
                (
                    end.alpha -
                        start.alpha
                    ) *
                t
    )
}
