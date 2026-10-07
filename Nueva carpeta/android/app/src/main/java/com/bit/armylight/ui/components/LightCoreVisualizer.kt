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
import androidx.compose.ui.geometry.Size
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

    val transition =
        rememberInfiniteTransition(
            label = "BIT-Light"
        )

    /*
     * Respiración mínima.
     *
     * La figura NO debe parecer que simplemente palpita
     * en Onda Púrpura.
     */
    val breathing by transition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.015f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        2200,
                        easing = FastOutSlowInEasing
                    ),
                repeatMode =
                    RepeatMode.Reverse
            ),
        label = "breathing"
    )

    /*
     * Pulso.
     */
    val pulse by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        700,
                        easing = FastOutSlowInEasing
                    ),
                repeatMode =
                    RepeatMode.Reverse
            ),
        label = "pulse"
    )

    /*
     * Onda que atraviesa la pantalla.
     */
    val wave by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        3000,
                        easing = FastOutSlowInEasing
                    ),
                repeatMode =
                    RepeatMode.Restart
            ),
        label = "wave"
    )

    /*
     * Segunda onda.
     */
    val wave2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        4200,
                        easing = FastOutSlowInEasing
                    ),
                repeatMode =
                    RepeatMode.Restart
            ),
        label = "wave2"
    )

    /*
     * Strobe.
     */
    val strobe by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        520,
                        easing = FastOutSlowInEasing
                    ),
                repeatMode =
                    RepeatMode.Restart
            ),
        label = "strobe"
    )

    /*
     * Supernova.
     */
    val nova by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        1700,
                        easing = FastOutSlowInEasing
                    ),
                repeatMode =
                    RepeatMode.Restart
            ),
        label = "nova"
    )

    /*
     * Aurora.
     */
    val aurora by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        6500,
                        easing = FastOutSlowInEasing
                    ),
                repeatMode =
                    RepeatMode.Reverse
            ),
        label = "aurora"
    )

    /*
     * Órbita.
     */
    val orbit by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        9000,
                        easing = FastOutSlowInEasing
                    ),
                repeatMode =
                    RepeatMode.Restart
            ),
        label = "orbit"
    )


    /*
     * ============================================================
     * VALORES
     * ============================================================
     */

    val rhythm =
        rhythmLevel.coerceIn(
            0f,
            1f
        )

    val intensityFactor =
        intensity.factor.coerceIn(
            0.3f,
            1f
        )

    val alpha =
        if (isBatterySaver) {
            0.68f
        } else {
            1f
        }


    /*
     * La figura es casi estable.
     *
     * Solo Pulso tiene una expansión claramente perceptible.
     */
    val figureScale =
        when {

            isPulseActive ->
                pulse

            isRhythmActive ->
                1f +
                    rhythm * 0.035f

            else ->
                breathing
        }


    /*
     * ============================================================
     * VIBRACIÓN
     * ============================================================
     *
     * NO modificamos la lógica que ya funciona.
     */

    var lastPeak by remember {
        mutableLongStateOf(0L)
    }

    /*
     * RITMO:
     * vibra únicamente cuando aparece un pico.
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
     * patrón de vibración automático.
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
                    x =
                        size.width / 2f,
                    y =
                        size.height * 0.43f
                )

            val minDimension =
                size.minDimension


            /*
             * ====================================================
             * FONDO NEGRO
             * ====================================================
             */

            drawRect(
                color =
                    Color(0xFF040006)
            )


            /*
             * ====================================================
             * MODOS DE EMISIÓN
             * ====================================================
             *
             * Cada modo tiene un comportamiento diferente.
             */


            /*
             * ====================================================
             * ONDA PÚRPURA
             * ====================================================
             *
             * NO palpita la figura.
             *
             * Las ondas pasan detrás de ella.
             */

            if (
                isConcertActive &&
                concertProgram.title.equals(
                    "Onda Púrpura",
                    ignoreCase = true
                )
            ) {

                val travelX =
                    -size.width +
                        (
                            size.width * 3f
                        ) *
                        wave

                /*
                 * Primera banda.
                 */

                drawRect(
                    brush =
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    Color(0xFF08000D),
                                    Color(0xFF3A0060),
                                    Color(0xFFB100FF),
                                    Color(0xFFFF35DD),
                                    Color(0xFF5800A0),
                                    Color(0xFF08000D)
                                ),
                            start =
                                Offset(
                                    travelX,
                                    0f
                                ),
                            end =
                                Offset(
                                    travelX +
                                        size.width *
                                        0.65f,
                                    size.height
                                )
                        )
                )

                /*
                 * Segunda banda independiente.
                 */

                val secondX =
                    -size.width +
                        (
                            size.width * 3f
                        ) *
                        wave2

                drawRect(
                    brush =
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    Color.Transparent,
                                    Color(0xFF7D00FF).copy(
                                        alpha = 0.30f
                                    ),
                                    Color(0xFFFF80EA).copy(
                                        alpha = 0.35f
                                    ),
                                    Color.Transparent
                                ),
                            start =
                                Offset(
                                    secondX,
                                    size.height
                                ),
                            end =
                                Offset(
                                    secondX +
                                        size.width *
                                        0.45f,
                                    0f
                                )
                        )
                )

                /*
                 * Halo móvil.
                 */

                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    Color(0xFFFF6BE8).copy(
                                        alpha = 0.38f
                                    ),
                                    Color(0xFF8B00FF).copy(
                                        alpha = 0.20f
                                    ),
                                    Color.Transparent
                                ),
                            center =
                                Offset(
                                    travelX +
                                        size.width *
                                        0.20f,
                                    size.height *
                                        0.45f
                                ),
                            radius =
                                size.maxDimension *
                                    0.65f
                        ),
                    center =
                        Offset(
                            travelX +
                                size.width *
                                0.20f,
                            size.height *
                                0.45f
                        ),
                    radius =
                        size.maxDimension *
                            0.65f
                )
            }


            /*
             * ====================================================
             * STROBE BEAT
             * ====================================================
             *
             * Flash muy corto.
             * No es simplemente cambiar todo el fondo.
             */

            if (
                isConcertActive &&
                concertProgram.title.equals(
                    "Strobe Beat",
                    ignoreCase = true
                )
            ) {

                val phase =
                    strobe

                when {

                    phase < 0.10f -> {

                        drawRect(
                            color =
                                Color.White
                        )
                    }

                    phase < 0.16f -> {

                        drawRect(
                            color =
                                Color(0xFFEED8FF)
                        )
                    }

                    phase < 0.22f -> {

                        drawRect(
                            color =
                                Color(0xFF8A00FF)
                        )
                    }

                    phase < 0.55f -> {

                        drawRect(
                            brush =
                                Brush.radialGradient(
                                    colors =
                                        listOf(
                                            Color(0xFFB500FF).copy(
                                                alpha = 0.55f
                                            ),
                                            Color(0xFF28003D).copy(
                                                alpha = 0.70f
                                            ),
                                            Color(0xFF040006)
                                        ),
                                    center =
                                        center,
                                    radius =
                                        size.maxDimension *
                                            0.90f
                                )
                        )
                    }

                    else -> {

                        drawRect(
                            color =
                                Color(0xFF050008)
                        )
                    }
                }
            }


            /*
             * ====================================================
             * SUPERNOVA
             * ====================================================
             *
             * La emisión nace en la figura y explota.
             */

            if (
                isConcertActive &&
                concertProgram.title.equals(
                    "Supernova",
                    ignoreCase = true
                )
            ) {

                val p =
                    nova

                val radius =
                    minDimension *
                        (
                            0.12f +
                                p * 0.90f
                        )

                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    Color.White.copy(
                                        alpha = 0.85f
                                    ),
                                    Color(0xFFFFB8FF).copy(
                                        alpha = 0.65f
                                    ),
                                    Color(0xFFB000FF).copy(
                                        alpha = 0.42f
                                    ),
                                    Color.Transparent
                                ),
                            center =
                                center,
                            radius =
                                radius
                        ),
                    center =
                        center,
                    radius =
                        radius
                )

                /*
                 * Anillo de explosión.
                 */

                val ring =
                    minDimension *
                        (
                            0.16f +
                                p * 0.95f
                        )

                drawCircle(
                    color =
                        Color.White.copy(
                            alpha =
                                (
                                    1f - p
                                ) *
                                0.65f
                        ),
                    center =
                        center,
                    radius =
                        ring,
                    style =
                        Stroke(
                            width =
                                minDimension *
                                    0.018f
                        )
                )

                /*
                 * Segundo anillo.
                 */

                val ring2 =
                    minDimension *
                        (
                            0.32f +
                                p * 0.70f
                        )

                drawCircle(
                    color =
                        Color(0xFFE080FF).copy(
                            alpha =
                                (
                                    1f - p
                                ) *
                                0.38f
                        ),
                    center =
                        center,
                    radius =
                        ring2,
                    style =
                        Stroke(
                            width =
                                minDimension *
                                    0.010f
                        )
                )
            }


            /*
             * ====================================================
             * AURORA
             * ====================================================
             *
             * Cortinas de luz que fluyen.
             */

            if (
                isConcertActive &&
                concertProgram.title.equals(
                    "Aurora",
                    ignoreCase = true
                )
            ) {

                val p =
                    aurora

                /*
                 * Banda 1.
                 */

                drawRect(
                    brush =
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    Color(0xFF12001E),
                                    Color(0xFF6500A8),
                                    Color(0xFF00C8FF),
                                    Color(0xFF00E6B0),
                                    Color(0xFF7000C8),
                                    Color(0xFF12001E)
                                ),
                            start =
                                Offset(
                                    x =
                                        -size.width +
                                            size.width *
                                            2.5f *
                                            p,
                                    y = 0f
                                ),
                            end =
                                Offset(
                                    x =
                                        size.width *
                                            1.5f *
                                            p,
                                    y =
                                        size.height
                                )
                        )
                )

                /*
                 * Banda 2.
                 */

                drawRect(
                    brush =
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    Color.Transparent,
                                    Color(0xFF00F0FF).copy(
                                        alpha = 0.20f
                                    ),
                                    Color(0xFF9D00FF).copy(
                                        alpha = 0.32f
                                    ),
                                    Color.Transparent
                                ),
                            start =
                                Offset(
                                    x =
                                        size.width *
                                            p,
                                    y =
                                        size.height
                                ),
                            end =
                                Offset(
                                    x =
                                        size.width *
                                            (
                                                p +
                                                    0.45f
                                            ),
                                    y = 0f
                                )
                        )
                )

                /*
                 * Halo suave.
                 */

                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    Color(0xFF00E5FF).copy(
                                        alpha = 0.18f
                                    ),
                                    Color(0xFF8A00FF).copy(
                                        alpha = 0.14f
                                    ),
                                    Color.Transparent
                                ),
                            center =
                                Offset(
                                    x =
                                        size.width *
                                            (
                                                0.20f +
                                                    p *
                                                    0.60f
                                            ),
                                    y =
                                        size.height *
                                            0.40f
                                ),
                            radius =
                                size.maxDimension *
                                    0.75f
                        ),
                    center =
                        Offset(
                            x =
                                size.width *
                                    (
                                        0.20f +
                                            p *
                                            0.60f
                                    ),
                            y =
                                size.height *
                                    0.40f
                        ),
                    radius =
                        size.maxDimension *
                            0.75f
                )
            }


            /*
             * ====================================================
             * RITMO
             * ====================================================
             */

            if (isRhythmActive) {

                val energy =
                    rhythm

                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    Color.White.copy(
                                        alpha =
                                            0.08f +
                                                energy *
                                                0.28f
                                    ),
                                    Color(0xFFD000FF).copy(
                                        alpha =
                                            0.25f +
                                                energy *
                                                0.30f
                                    ),
                                    Color.Transparent
                                ),
                            center =
                                center,
                            radius =
                                minDimension *
                                    (
                                        0.30f +
                                            energy *
                                            0.75f
                                    )
                        ),
                    center =
                        center,
                    radius =
                        minDimension *
                            (
                                0.30f +
                                    energy *
                                    0.75f
                            )
                )
            }


            /*
             * ====================================================
             * PULSO
             * ====================================================
             */

            if (isPulseActive) {

                val p =
                    (
                        pulse -
                            0.88f
                    ) /
                    0.24f

                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    Color.White.copy(
                                        alpha =
                                            0.12f +
                                                p *
                                                0.22f
                                    ),
                                    Color(0xFFD000FF).copy(
                                        alpha =
                                            0.28f +
                                                p *
                                                0.25f
                                    ),
                                    Color.Transparent
                                ),
                            center =
                                center,
                            radius =
                                minDimension *
                                    (
                                        0.30f +
                                            p *
                                            0.55f
                                    )
                        ),
                    center =
                        center,
                    radius =
                        minDimension *
                            (
                                0.30f +
                                    p *
                                    0.55f
                            )
                )
            }


            /*
             * ====================================================
             * FIGURA CENTRAL
             * ====================================================
             *
             * Se dibuja DESPUÉS del fondo para que siempre
             * permanezca claramente visible.
             */

            val figureRadius =
                minDimension *
                    0.245f *
                    figureScale


            /*
             * Halo exterior.
             */

            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                Color.White.copy(
                                    alpha =
                                        0.30f *
                                            alpha
                                ),
                                Color(0xFFE8B8FF).copy(
                                    alpha =
                                        0.20f *
                                            alpha
                                ),
                                Color.Transparent
                            ),
                        center =
                            center,
                        radius =
                            figureRadius *
                                1.90f
                    ),
                center =
                    center,
                radius =
                    figureRadius *
                        1.90f
            )


            /*
             * ====================================================
             * ÓRBITA DETRÁS
             * ====================================================
             */

            drawOrbit(
                center =
                    center,
                radiusX =
                    figureRadius *
                        1.62f,
                radiusY =
                    figureRadius *
                        0.57f,
                rotationDegrees =
                    -17f +
                        orbit *
                        0.012f,
                color =
                    Color.White.copy(
                        alpha =
                            0.92f *
                                alpha
                    ),
                strokeWidth =
                    minDimension *
                        0.018f
            )


            /*
             * ====================================================
             * FIGURA
             * ====================================================
             */

            drawMainFigure(
                center =
                    center,
                radius =
                    figureRadius,
                alpha =
                    alpha
            )


            /*
             * ====================================================
             * ÓRBITA DELANTERA
             * ====================================================
             *
             * Una segunda sección fina da sensación de que
             * la órbita atraviesa la figura.
             */

            drawOrbit(
                center =
                    center,
                radiusX =
                    figureRadius *
                        1.62f,
                radiusY =
                    figureRadius *
                        0.57f,
                rotationDegrees =
                    -17f +
                        orbit *
                        0.012f,
                color =
                    Color.White.copy(
                        alpha =
                            0.70f *
                                alpha
                    ),
                strokeWidth =
                    minDimension *
                        0.006f
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
                            -figureRadius *
                                0.92f,
                            -figureRadius *
                                0.80f
                        ),
                radius =
                    figureRadius *
                        0.14f,
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
                            figureRadius *
                                0.95f,
                            -figureRadius *
                                0.68f
                        ),
                radius =
                    figureRadius *
                        0.105f,
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
                            figureRadius *
                                1.02f,
                            figureRadius *
                                0.82f
                        ),
                radius =
                    figureRadius *
                        0.09f,
                color =
                    Color.White.copy(
                        alpha =
                            0.88f *
                                alpha
                    )
            )

            drawSparkle(
                center =
                    center +
                        Offset(
                            -figureRadius *
                                0.90f,
                            figureRadius *
                                0.90f
                        ),
                radius =
                    figureRadius *
                        0.075f,
                color =
                    Color.White.copy(
                        alpha =
                            0.84f *
                                alpha
                    )
            )
        }
    }
}


/*
 * ================================================================
 * FIGURA PRINCIPAL
 * ================================================================
 *
 * Cuatro puntas largas.
 * Cuatro curvas internas.
 * Centro luminoso.
 *
 * No es un rombo geométrico simple.
 */

private fun DrawScope.drawMainFigure(
    center: Offset,
    radius: Float,
    alpha: Float
) {

    val path =
        Path()


    /*
     * PUNTA SUPERIOR
     */

    path.moveTo(
        center.x,
        center.y -
            radius
    )


    /*
     * SUPERIOR → DERECHA
     *
     * La curva primero se abre y luego se estrecha.
     */

    path.cubicTo(
        center.x +
            radius * 0.10f,
        center.y -
            radius * 0.72f,

        center.x +
            radius * 0.27f,
        center.y -
            radius * 0.28f,

        center.x +
            radius,
        center.y
    )


    /*
     * DERECHA → INFERIOR
     */

    path.cubicTo(
        center.x +
            radius * 0.34f,
        center.y +
            radius * 0.10f,

        center.x +
            radius * 0.12f,
        center.y +
            radius * 0.73f,

        center.x,
        center.y +
            radius
    )


    /*
     * INFERIOR → IZQUIERDA
     */

    path.cubicTo(
        center.x -
            radius * 0.12f,
        center.y +
            radius * 0.73f,

        center.x -
            radius * 0.34f,
        center.y +
            radius * 0.10f,

        center.x -
            radius,
        center.y
    )


    /*
     * IZQUIERDA → SUPERIOR
     */

    path.cubicTo(
        center.x -
            radius * 0.27f,
        center.y -
            radius * 0.28f,

        center.x -
            radius * 0.10f,
        center.y -
            radius * 0.72f,

        center.x,
        center.y -
            radius
    )

    path.close()


    /*
     * ============================================================
     * RESPLANDOR
     * ============================================================
     */

    drawPath(
        path =
            path,
        color =
            Color.White.copy(
                alpha =
                    0.34f *
                        alpha
            ),
        style =
            Stroke(
                width =
                    radius *
                        0.16f,
                join =
                    StrokeJoin.Round
            )
    )


    /*
     * ============================================================
     * CUERPO
     * ============================================================
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

                        Color(0xFFF7E8FF).copy(
                            alpha =
                                0.99f *
                                    alpha
                        ),

                        Color(0xFFD09BFF).copy(
                            alpha =
                                0.98f *
                                    alpha
                        ),

                        Color(0xFF8C4DFF).copy(
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
     * ============================================================
     * LUZ CENTRAL
     * ============================================================
     */

    drawCircle(
        brush =
            Brush.radialGradient(
                colors =
                    listOf(
                        Color.White.copy(
                            alpha =
                                0.90f *
                                    alpha
                        ),
                        Color.White.copy(
                            alpha =
                                0.35f *
                                    alpha
                        ),
                        Color.Transparent
                    ),
                center =
                    center,
                radius =
                    radius *
                        0.38f
            ),
        center =
            center,
        radius =
            radius *
                0.38f
    )


    /*
     * ============================================================
     * LÍNEAS INTERNAS
     * ============================================================
     */

    drawLine(
        color =
            Color.White.copy(
                alpha =
                    0.68f *
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
                0.018f
    )

    drawLine(
        color =
            Color.White.copy(
                alpha =
                    0.54f *
                        alpha
            ),
        start =
            Offset(
                center.x -
                    radius *
                    0.68f,
                center.y
            ),
        end =
            Offset(
                center.x +
                    radius *
                    0.68f,
                center.y
            ),
        strokeWidth =
            radius *
                0.014f
    )
}


/*
 * ================================================================
 * ÓRBITA
 * ================================================================
 */

private fun DrawScope.drawOrbit(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
    rotationDegrees: Float,
    color: Color,
    strokeWidth: Float
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
                Size(
                    radiusX * 2f,
                    radiusY * 2f
                ),
            style =
                Stroke(
                    width =
                        strokeWidth,
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
        radius *
            0.18f

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
