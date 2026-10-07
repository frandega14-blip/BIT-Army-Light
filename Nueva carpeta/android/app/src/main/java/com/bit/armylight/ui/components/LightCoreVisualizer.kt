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

    val transition =
        rememberInfiniteTransition(
            label = "BIT Light"
        )

    /*
     * Respiración normal.
     */
    val breathing by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 1600,
                        easing = FastOutSlowInEasing
                    ),
                repeatMode = RepeatMode.Reverse
            ),
        label = "breathing"
    )

    /*
     * Pulso.
     */
    val pulse by transition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.12f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 620,
                        easing = FastOutSlowInEasing
                    ),
                repeatMode = RepeatMode.Reverse
            ),
        label = "pulse"
    )

    /*
     * Movimiento de la órbita.
     */
    val orbitRotation by transition.animateFloat(
        initialValue = -18f,
        targetValue = 342f,
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
     * =========================================================
     * CAMBIO DE COLOR
     * =========================================================
     */

    val colorSpeed =
        when {

            !isConcertActive ->
                5000

            concertProgram.title.equals(
                "Strobe Beat",
                ignoreCase = true
            ) ->
                450

            concertProgram.title.equals(
                "Supernova",
                ignoreCase = true
            ) ->
                1000

            concertProgram.title.equals(
                "Aurora",
                ignoreCase = true
            ) ->
                4200

            else ->
                2300
        }

    val colorAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis =
                            colorSpeed,
                        easing =
                            FastOutSlowInEasing
                    ),
                repeatMode = RepeatMode.Reverse
            ),
        label = "concert colors"
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
     * Tamaño.
     */
    val rhythmScale =
        1f +
            rhythm *
            0.14f

    val scale =
        (
            if (isPulseActive) {
                pulse
            } else {
                breathing
            }
            ) *
            rhythmScale *
            (
                0.96f +
                    intensityFactor *
                    0.04f
                )

    /*
     * =========================================================
     * PALETA
     * =========================================================
     */

    val purple =
        Color(0xFF9B4DFF)

    val pink =
        Color(0xFFFF3FA4)

    val magenta =
        Color(0xFFFF4DDE)

    val blue =
        Color(0xFF596BFF)

    val cyan =
        Color(0xFF36D9FF)

    /*
     * Interpolación circular de colores.
     */
    val color1 =
        when {
            colorAnimation < 0.20f ->
                lerpColor(
                    purple,
                    pink,
                    colorAnimation / 0.20f
                )

            colorAnimation < 0.40f ->
                lerpColor(
                    pink,
                    magenta,
                    (colorAnimation - 0.20f) / 0.20f
                )

            colorAnimation < 0.60f ->
                lerpColor(
                    magenta,
                    blue,
                    (colorAnimation - 0.40f) / 0.20f
                )

            colorAnimation < 0.80f ->
                lerpColor(
                    blue,
                    cyan,
                    (colorAnimation - 0.60f) / 0.20f
                )

            else ->
                lerpColor(
                    cyan,
                    purple,
                    (colorAnimation - 0.80f) / 0.20f
                )
        }

    /*
     * Color principal.
     */
    val mainColor =
        when {

            /*
             * Ritmo.
             */
            isRhythmActive -> {

                Color.hsv(
                    hue =
                        270f +
                            rhythm *
                            70f,
                    saturation = 0.70f,
                    value = 1f
                )
            }

            /*
             * Concierto.
             */
            isConcertActive -> {

                color1
            }

            /*
             * Normal.
             */
            else -> {

                Color(0xFFD9B3FF)
            }
        }

    /*
     * =========================================================
     * BRILLO
     * =========================================================
     */

    val alpha =
        if (isBatterySaver) {
            0.52f
        } else {
            0.96f
        }

    /*
     * Flash rápido para Strobe Beat.
     */
    val strobeFlash =
        if (
            isConcertActive &&
            concertProgram.title.equals(
                "Strobe Beat",
                ignoreCase = true
            )
        ) {

            if (colorAnimation > 0.78f) {
                1f
            } else {
                0f
            }

        } else {
            0f
        }

    /*
     * Explosión de Supernova.
     */
    val supernovaFlash =
        if (
            isConcertActive &&
            concertProgram.title.equals(
                "Supernova",
                ignoreCase = true
            )
        ) {

            if (colorAnimation > 0.82f) {
                (
                    (colorAnimation - 0.82f) /
                        0.18f
                    )
                    .coerceIn(
                        0f,
                        1f
                    )
            } else {
                0f
            }

        } else {
            0f
        }

    /*
     * =========================================================
     * VIBRACIÓN POR RITMO
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
     * DIBUJO
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
             * POSICIÓN DEL SÍMBOLO
             *
             * Lo dejamos en la zona que señalaste.
             */
            val center =
                Offset(
                    x =
                        size.width / 2f,
                    y =
                        size.height * 0.43f
                )

            /*
             * Tamaño.
             */
            val radius =
                size.minDimension *
                    0.19f

            /*
             * =================================================
             * HALO
             * =================================================
             */

            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                Color.White.copy(
                                    alpha =
                                        0.18f *
                                            alpha
                                ),
                                mainColor.copy(
                                    alpha =
                                        0.30f *
                                            alpha
                                ),
                                mainColor.copy(
                                    alpha =
                                        0.08f *
                                            alpha
                                ),
                                Color.Transparent
                            ),
                        center =
                            center,
                        radius =
                            radius *
                                2.65f *
                                scale
                    ),
                center =
                    center,
                radius =
                    radius *
                        2.65f *
                        scale
            )

            /*
             * =================================================
             * HALO CENTRAL
             * =================================================
             */

            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                Color.White.copy(
                                    alpha =
                                        0.38f *
                                            alpha
                                ),
                                mainColor.copy(
                                    alpha =
                                        0.24f *
                                            alpha
                                ),
                                Color.Transparent
                            ),
                        center =
                            center,
                        radius =
                            radius *
                                1.55f *
                                scale
                    ),
                center =
                    center,
                radius =
                    radius *
                        1.55f *
                        scale
            )

            /*
             * =================================================
             * UNA SOLA ÓRBITA
             * =================================================
             *
             * Esta es la órbita principal del símbolo.
             */
            drawOrbit(
                center =
                    center,
                radiusX =
                    radius *
                        1.72f *
                        scale,
                radiusY =
                    radius *
                        0.58f *
                        scale,
                rotation =
                    orbitRotation,
                color =
                    Color.White.copy(
                        alpha =
                            0.94f *
                                alpha
                    ),
                width =
                    radius *
                        0.065f
            )

            /*
             * =================================================
             * ESTRELLA DE 4 PUNTAS
             * =================================================
             */
            drawFourPointStar(
                center =
                    center,
                radius =
                    radius *
                        scale,
                color =
                    mainColor,
                alpha =
                    alpha
            )

            /*
             * =================================================
             * CENTRO
             * =================================================
             */

            drawCircle(
                color =
                    Color.White.copy(
                        alpha =
                            0.95f *
                                alpha
                    ),
                center =
                    center,
                radius =
                    radius *
                        0.075f *
                        scale
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
                                -radius *
                                    1.25f *
                                    scale,
                            y =
                                -radius *
                                    0.85f *
                                    scale
                        ),
                radius =
                    radius *
                        0.20f *
                        scale,
                color =
                    Color.White.copy(
                        alpha =
                            0.95f *
                                alpha
                    )
            )

            drawSparkle(
                center =
                    center +
                        Offset(
                            x =
                                radius *
                                    1.25f *
                                    scale,
                            y =
                                -radius *
                                    0.65f *
                                    scale
                        ),
                radius =
                    radius *
                        0.15f *
                        scale,
                color =
                    Color.White.copy(
                        alpha =
                            0.90f *
                                alpha
                    )
            )

            drawSparkle(
                center =
                    center +
                        Offset(
                            x =
                                radius *
                                    1.15f *
                                    scale,
                            y =
                                radius *
                                    0.92f *
                                    scale
                        ),
                radius =
                    radius *
                        0.13f *
                        scale,
                color =
                    mainColor.copy(
                        alpha =
                            0.90f *
                                alpha
                    )
            )

            drawSparkle(
                center =
                    center +
                        Offset(
                            x =
                                -radius *
                                    1.05f *
                                    scale,
                            y =
                                radius *
                                    0.82f *
                                    scale
                        ),
                radius =
                    radius *
                        0.10f *
                        scale,
                color =
                    Color.White.copy(
                        alpha =
                            0.88f *
                                alpha
                    )
            )

            /*
             * =================================================
             * RITMO: DESTELLOS ADICIONALES
             * =================================================
             */

            if (
                isRhythmActive &&
                rhythm > 0.30f
            ) {

                drawSparkle(
                    center =
                        center +
                            Offset(
                                x = 0f,
                                y =
                                    -radius *
                                        1.42f *
                                        scale
                            ),
                    radius =
                        radius *
                            (
                                0.08f +
                                    rhythm *
                                    0.12f
                                ) *
                            scale,
                    color =
                        Color.White.copy(
                            alpha =
                                rhythm *
                                    0.85f
                        )
                )

                drawSparkle(
                    center =
                        center +
                            Offset(
                                x = 0f,
                                y =
                                    radius *
                                        1.42f *
                                        scale
                            ),
                    radius =
                        radius *
                            (
                                0.07f +
                                    rhythm *
                                    0.10f
                                ) *
                            scale,
                    color =
                        mainColor.copy(
                            alpha =
                                rhythm *
                                    0.85f
                        )
                )
            }

            /*
             * =================================================
             * FLASH STROBE
             * =================================================
             */

            if (
                strobeFlash > 0f
            ) {

                drawCircle(
                    color =
                        Color.White.copy(
                            alpha =
                                0.35f *
                                    alpha
                        ),
                    center =
                        center,
                    radius =
                        radius *
                            2.15f *
                            scale
                )
            }

            /*
             * =================================================
             * EXPLOSIÓN SUPERNOVA
             * =================================================
             */

            if (
                supernovaFlash > 0f
            ) {

                drawCircle(
                    color =
                        Color.White.copy(
                            alpha =
                                supernovaFlash *
                                    0.40f *
                                    alpha
                        ),
                    center =
                        center,
                    radius =
                        radius *
                            (
                                1.9f +
                                    supernovaFlash *
                                    1.2f
                                ) *
                            scale
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
 * Esta es la figura principal.
 *
 * NO es una estrella irregular.
 * Son cuatro puntas claras y simétricas.
 */
private fun DrawScope.drawFourPointStar(
    center: Offset,
    radius: Float,
    color: Color,
    alpha: Float
) {

    val path =
        Path()

    val outer =
        radius

    val inner =
        radius *
            0.20f

    /*
     * 8 puntos:
     *
     *       ▲
     *
     * ◀     ●     ▶
     *
     *       ▼
     */

    for (i in 0 until 8) {

        val angle =
            (
                -90f +
                    i *
                    45f
                )
                .toRadians()

        val currentRadius =
            if (i % 2 == 0) {
                outer
            } else {
                inner
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
     * Glow exterior.
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
                        0.15f,
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
                                0.98f *
                                    alpha
                        ),
                        color.copy(
                            alpha =
                                0.98f *
                                    alpha
                        ),
                        color.copy(
                            red =
                                color.red *
                                    0.72f,
                            green =
                                color.green *
                                    0.72f,
                            blue =
                                color.blue *
                                    0.90f,
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
     * Brillo vertical central.
     */
    drawLine(
        color =
            Color.White.copy(
                alpha =
                    0.30f *
                        alpha
            ),
        start =
            Offset(
                center.x,
                center.y -
                    radius *
                    0.70f
            ),
        end =
            Offset(
                center.x,
                center.y +
                    radius *
                    0.70f
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
    rotation: Float,
    color: Color,
    width: Float
) {

    rotate(
        degrees =
            rotation,
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
            0.17f

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
 * INTERPOLACIÓN DE COLOR
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
