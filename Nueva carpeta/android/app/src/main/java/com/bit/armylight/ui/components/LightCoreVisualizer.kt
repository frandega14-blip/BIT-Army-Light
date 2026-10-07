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
import com.bit.armylight.viewmodel.ConcertProgram
import com.bit.armylight.viewmodel.LightIntensity
import kotlinx.coroutines.delay


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

    val transition =
        rememberInfiniteTransition(
            label = "BIT-Light"
        )

    /*
     * ============================================================
     * ANIMACIONES
     * ============================================================
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
     * RITMO
     * ============================================================
     */

    val rhythm =
        rhythmLevel.coerceIn(
            0f,
            1f
        )

    val rhythmVisual =
        (
            rhythm * 2.8f
        ).coerceIn(
            0f,
            1f
        )

    val rhythmPeak =
        (
            (
                rhythmVisual -
                    0.35f
            ) /
                0.65f
        ).coerceIn(
            0f,
            1f
        )

    /*
     * ============================================================
     * ALPHA
     * ============================================================
     */

    val alpha =
        if (isBatterySaver) {
            0.68f
        } else {
            1f
        }

    /*
     * ============================================================
     * ESCALA DE FIGURA
     * ============================================================
     */

    val figureScale =
        when {

            isPulseActive ->
                pulse

            isRhythmActive ->
                0.96f +
                    rhythmVisual *
                    0.16f

            else ->
                breathing
        }

    /*
     * ============================================================
     * VIBRACIÓN — RITMO
     * ============================================================
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

                lastPeak = now

                onPeakPulse()
            }
        }
    }

    /*
     * ============================================================
     * VIBRACIÓN — PULSO / CONCIERTO
     * ============================================================
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
     * PANTALLA
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
             * FONDO
             * ====================================================
             */

            drawRect(
                color =
                    Color(0xFF040006)
            )

            /*
             * ====================================================
             * ONDA PÚRPURA
             * ====================================================
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

                val waveCenter =
                    Offset(
                        travelX +
                            size.width *
                            0.20f,
                        size.height *
                            0.45f
                    )

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
                                waveCenter,
                            radius =
                                size.maxDimension *
                                    0.65f
                        ),
                    center =
                        waveCenter,
                    radius =
                        size.maxDimension *
                            0.65f
                )
            }

            /*
             * ====================================================
             * STROBE BEAT
             * ====================================================
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

                val auroraCenter =
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
                    )

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
                                auroraCenter,
                            radius =
                                size.maxDimension *
                                    0.75f
                        ),
                    center =
                        auroraCenter,
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
                    rhythmVisual

                val rhythmRadius =
                    minDimension *
                        (
                            0.22f +
                                energy *
                                0.95f
                        )

                /*
                 * Halo principal.
                 */

                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    Color.White.copy(
                                        alpha =
                                            0.18f +
                                                energy *
                                                0.55f
                                    ),

                                    Color(0xFFF0C8FF).copy(
                                        alpha =
                                            0.20f +
                                                energy *
                                                0.35f
                                    ),

                                    Color(0xFFC000FF).copy(
                                        alpha =
                                            0.30f +
                                                energy *
                                                0.38f
                                    ),

                                    Color(0xFF7800FF).copy(
                                        alpha =
                                            0.16f +
                                                energy *
                                                0.28f
                                    ),

                                    Color.Transparent
                                ),
                            center =
                                center,
                            radius =
                                rhythmRadius
                        ),
                    center =
                        center,
                    radius =
                        rhythmRadius
                )

                /*
                 * Anillo exterior.
                 */

                val outerRadius =
                    minDimension *
                        (
                            0.42f +
                                energy *
                                1.20f
                        )

                drawCircle(
                    color =
                        Color(0xFFB020FF).copy(
                            alpha =
                                0.08f +
                                    energy *
                                    0.20f
                        ),
                    center =
                        center,
                    radius =
                        outerRadius,
                    style =
                        Stroke(
                            width =
                                minDimension *
                                    (
                                        0.008f +
                                            energy *
                                            0.012f
                                    )
                        )
                )

                /*
                 * Destello central.
                 */

                if (energy > 0.12f) {

                    val flashRadius =
                        minDimension *
                            (
                                0.06f +
                                    energy *
                                    0.22f
                            )

                    drawCircle(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        Color.White.copy(
                                            alpha =
                                                0.35f +
                                                    energy *
                                                    0.65f
                                        ),
                                        Color(0xFFE7A7FF).copy(
                                            alpha =
                                                0.30f +
                                                    energy *
                                                    0.35f
                                        ),
                                        Color.Transparent
                                    ),
                                center =
                                    center,
                                radius =
                                    flashRadius
                            ),
                        center =
                            center,
                        radius =
                            flashRadius
                    )
                }

                /*
                 * Golpe fuerte.
                 */

                if (rhythmPeak > 0.55f) {

                    val peakRadius =
                        minDimension *
                            (
                                0.50f +
                                    rhythmPeak *
                                    0.65f
                            )

                    drawCircle(
                        color =
                            Color.White.copy(
                                alpha =
                                    rhythmPeak *
                                    0.30f
                            ),
                        center =
                            center,
                        radius =
                            peakRadius,
                        style =
                            Stroke(
                                width =
                                    minDimension *
                                        0.012f
                            )
                    )
                }
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

                val pulseRadius =
                    minDimension *
                        (
                            0.30f +
                                p *
                                0.55f
                        )

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
                                pulseRadius
                        ),
                    center =
                        center,
                    radius =
                        pulseRadius
                )
            }

            /*
             * ====================================================
             * FIGURA CENTRAL
             * ====================================================
             */

            val figureRadius =
                minDimension *
                    0.245f *
                    figureScale

            /*
             * Halo principal.
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
                                Color(0xFF9E36FF).copy(
                                    alpha =
                                        0.08f *
                                            alpha
                                ),
                                Color.Transparent
                            ),
                        center =
                            center,
                        radius =
                            figureRadius *
                                1.95f
                    ),
                center =
                    center,
                radius =
                    figureRadius *
                        1.95f
            )

            /*
             * Halo secundario.
             */

            drawCircle(
                color =
                    Color(0xFF9D32FF).copy(
                        alpha =
                            0.12f *
                                alpha
                    ),
                center =
                    center,
                radius =
                    figureRadius *
                        1.42f
            )

            /*
             * ÓRBITA TRASERA
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
             * SEGUNDA ÓRBITA
             */

            drawOrbit(
                center =
                    center,
                radiusX =
                    figureRadius *
                        1.42f,
                radiusY =
                    figureRadius *
                        0.78f,
                rotationDegrees =
                    40f -
                        orbit *
                        0.009f,
                color =
                    Color(0xFFB96BFF).copy(
                        alpha =
                            0.34f *
                                alpha
                    ),
                strokeWidth =
                    minDimension *
                        0.006f
            )

            /*
             * FIGURA
             */

            val rhythmFigureAlpha =
                if (isRhythmActive) {

                    (
                        alpha *
                            (
                                0.72f +
                                    rhythmVisual *
                                    0.28f
                            )
                    ).coerceIn(
                        0f,
                        1f
                    )

                } else {
                    alpha
                }

            drawMainFigure(
                center =
                    center,
                radius =
                    figureRadius,
                alpha =
                    rhythmFigureAlpha
            )

            /*
             * ÓRBITA DELANTERA
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
                            0.72f *
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

            /*
             * Partículas pequeñas.
             */

            drawSparkle(
                center =
                    center +
                        Offset(
                            -figureRadius *
                                1.35f,
                            -figureRadius *
                                0.22f
                        ),
                radius =
                    figureRadius *
                        0.035f,
                color =
                    Color(0xFFE8C7FF).copy(
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
                                1.35f,
                            figureRadius *
                                0.18f
                        ),
                radius =
                    figureRadius *
                        0.030f,
                color =
                    Color(0xFFE8C7FF).copy(
                        alpha =
                            0.88f *
                                alpha
                    )
            )

            drawSparkle(
                center =
                    center +
                        Offset(
                            figureRadius *
                                0.30f,
                            -figureRadius *
                                1.34f
                        ),
                radius =
                    figureRadius *
                        0.025f,
                color =
                    Color.White.copy(
                        alpha =
                            0.82f *
                                alpha
                    )
            )

            drawSparkle(
                center =
                    center +
                        Offset(
                            -figureRadius *
                                0.42f,
                            figureRadius *
                                1.36f
                        ),
                radius =
                    figureRadius *
                        0.028f,
                color =
                    Color.White.copy(
                        alpha =
                            0.78f *
                                alpha
                    )
            )
        }
    }
}


/*
 * ================================================================
 * FIGURA CENTRAL ORIGINAL
 * ================================================================
 */

private fun DrawScope.drawMainFigure(
    center: Offset,
    radius: Float,
    alpha: Float
) {

    val outerPath =
        Path()

    /*
     * Superior.
     */

    outerPath.moveTo(
        center.x,
        center.y -
            radius
    )

    /*
     * Superior → derecha.
     */

    outerPath.cubicTo(
        center.x +
            radius * 0.07f,
        center.y -
            radius * 0.72f,

        center.x +
            radius * 0.22f,
        center.y -
            radius * 0.30f,

        center.x +
            radius,
        center.y
    )

    /*
     * Derecha → inferior.
     */

    outerPath.cubicTo(
        center.x +
            radius * 0.30f,
        center.y +
            radius * 0.22f,

        center.x +
            radius * 0.10f,
        center.y +
            radius * 0.72f,

        center.x,
        center.y +
            radius
    )

    /*
     * Inferior → izquierda.
     */

    outerPath.cubicTo(
        center.x -
            radius * 0.10f,
        center.y +
            radius * 0.72f,

        center.x -
            radius * 0.30f,
        center.y +
            radius * 0.22f,

        center.x -
            radius,
        center.y
    )

    /*
     * Izquierda → superior.
     */

    outerPath.cubicTo(
        center.x -
            radius * 0.22f,
        center.y -
            radius * 0.30f,

        center.x -
            radius * 0.07f,
        center.y -
            radius * 0.72f,

        center.x,
        center.y -
            radius
    )

    outerPath.close()

    /*
     * Glow exterior.
     */

    drawPath(
        path =
            outerPath,
        color =
            Color(0xFFB65CFF).copy(
                alpha =
                    0.22f *
                        alpha
            ),
        style =
            Stroke(
                width =
                    radius *
                        0.24f,
                join =
                    StrokeJoin.Round
            )
    )

    /*
     * Segundo halo.
     */

    drawPath(
        path =
            outerPath,
        color =
            Color.White.copy(
                alpha =
                    0.22f *
                        alpha
            ),
        style =
            Stroke(
                width =
                    radius *
                        0.12f,
                join =
                    StrokeJoin.Round
            )
    )

    /*
     * Cuerpo.
     */

    drawPath(
        path =
            outerPath,
        brush =
            Brush.linearGradient(
                colors =
                    listOf(
                        Color.White.copy(
                            alpha =
                                alpha
                        ),

                        Color(0xFFF9E9FF).copy(
                            alpha =
                                alpha
                        ),

                        Color(0xFFE0A8FF).copy(
                            alpha =
                                0.99f *
                                    alpha
                        ),

                        Color(0xFFB25CFF).copy(
                            alpha =
                                0.98f *
                                    alpha
                        ),

                        Color(0xFF7028D9).copy(
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
     * Reflejo interior.
     */

    val innerHighlight =
        Path()

    innerHighlight.moveTo(
        center.x,
        center.y -
            radius *
            0.82f
    )

    innerHighlight.cubicTo(
        center.x +
            radius * 0.05f,
        center.y -
            radius * 0.57f,

        center.x +
            radius * 0.10f,
        center.y -
            radius * 0.30f,

        center.x +
            radius * 0.64f,
        center.y -
            radius * 0.06f
    )

    innerHighlight.cubicTo(
        center.x +
            radius * 0.36f,
        center.y -
            radius * 0.10f,

        center.x +
            radius * 0.17f,
        center.y -
            radius * 0.07f,

        center.x,
        center.y
    )

    innerHighlight.cubicTo(
        center.x -
            radius * 0.17f,
        center.y -
            radius * 0.07f,

        center.x -
            radius * 0.36f,
        center.y -
            radius * 0.10f,

        center.x -
            radius * 0.64f,
        center.y -
            radius * 0.06f
    )

    innerHighlight.cubicTo(
        center.x -
            radius * 0.10f,
        center.y -
            radius * 0.30f,

        center.x -
            radius * 0.05f,
        center.y -
            radius * 0.57f,

        center.x,
        center.y -
            radius *
            0.82f
    )

    innerHighlight.close()

    drawPath(
        path =
            innerHighlight,
        color =
            Color.White.copy(
                alpha =
                    0.30f *
                        alpha
            )
    )

    /*
     * Núcleo.
     */

    drawCircle(
        brush =
            Brush.radialGradient(
                colors =
                    listOf(
                        Color.White.copy(
                            alpha =
                                alpha
                        ),
                        Color.White.copy(
                            alpha =
                                0.92f *
                                    alpha
                        ),
                        Color(0xFFF0C9FF).copy(
                            alpha =
                                0.65f *
                                    alpha
                        ),
                        Color(0xFFC56AFF).copy(
                            alpha =
                                0.22f *
                                    alpha
                        ),
                        Color.Transparent
                    ),
                center =
                    center,
                radius =
                    radius *
                        0.48f
            ),
        center =
            center,
        radius =
            radius *
                0.48f
    )

    /*
     * Línea vertical.
     */

    drawLine(
        color =
            Color.White.copy(
                alpha =
                    0.72f *
                        alpha
            ),
        start =
            Offset(
                center.x,
                center.y -
                    radius *
                    0.60f
            ),
        end =
            Offset(
                center.x,
                center.y +
                    radius *
                    0.60f
            ),
        strokeWidth =
            radius *
                0.022f,
        cap =
            StrokeCap.Round
    )

    /*
     * Línea horizontal.
     */

    drawLine(
        color =
            Color.White.copy(
                alpha =
                    0.65f *
                        alpha
            ),
        start =
            Offset(
                center.x -
                    radius *
                    0.60f,
                center.y
            ),
        end =
            Offset(
                center.x +
                    radius *
                    0.60f,
                center.y
            ),
        strokeWidth =
            radius *
                0.018f,
        cap =
            StrokeCap.Round
    )

    /*
     * Borde.
     */

    drawPath(
        path =
            outerPath,
        color =
            Color.White.copy(
                alpha =
                    0.88f *
                        alpha
            ),
        style =
            Stroke(
                width =
                    radius *
                        0.018f,
                join =
                    StrokeJoin.Round
            )
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
