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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.bit.armylight.R
import com.bit.armylight.viewmodel.ConcertProgram
import com.bit.armylight.viewmodel.LightIntensity
import kotlinx.coroutines.delay
import kotlin.math.roundToInt


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
     * IMAGEN CENTRAL
     * ============================================================
     *
     * Archivo:
     *
     * res/drawable-nodpi/bit_premium_center_emblem.png
     *
     * Se carga como ImageBitmap para poder dibujarla dentro
     * del Canvas.
     */

    val premiumEmblem =
        ImageBitmap.imageResource(
            id =
                R.drawable.bit_premium_center_emblem
        )

    /*
     * ============================================================
     * ANIMACIONES
     * ============================================================
     */

    val transition =
        rememberInfiniteTransition(
            label = "BIT-Light"
        )

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

    /*
     * ============================================================
     * CONTROL DE LUZ
     * ============================================================
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
     * ALPHA GENERAL
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
     * ESCALA
     * ============================================================
     */

    val emblemScale =
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
     * INTERFAZ
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
             * FONDO BASE
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
             * HALO DETRÁS DEL EMBLEMA
             * ====================================================
             */

            val haloRadius =
                minDimension *
                    (
                        0.32f +
                            rhythmVisual *
                            0.18f
                    )

            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                Color.White.copy(
                                    alpha =
                                        0.12f *
                                            alpha
                                ),
                                Color(0xFFE0A0FF).copy(
                                    alpha =
                                        0.16f *
                                            alpha
                                ),
                                Color(0xFF8C24FF).copy(
                                    alpha =
                                        0.12f *
                                            alpha
                                ),
                                Color.Transparent
                            ),
                        center =
                            center,
                        radius =
                            haloRadius
                    ),
                center =
                    center,
                radius =
                    haloRadius
            )

            /*
             * ====================================================
             * EMBLEMA PREMIUM
             * ====================================================
             *
             * Screen hace que el fondo negro de la imagen no
             * tape los efectos que están detrás.
             */

            val baseEmblemWidth =
                minDimension *
                    0.76f

            val aspectRatio =
                premiumEmblem.height.toFloat() /
                    premiumEmblem.width.toFloat()

            val emblemWidth =
                baseEmblemWidth *
                    emblemScale

            val emblemHeight =
                emblemWidth *
                    aspectRatio

            val emblemLeft =
                (
                    size.width -
                        emblemWidth
                ) /
                    2f

            val emblemTop =
                center.y -
                    emblemHeight /
                    2f

            drawImage(
                image =
                    premiumEmblem,
                dstOffset =
                    IntOffset(
                        emblemLeft.roundToInt(),
                        emblemTop.roundToInt()
                    ),
                dstSize =
                    IntSize(
                        emblemWidth.roundToInt(),
                        emblemHeight.roundToInt()
                    ),
                alpha =
                    alpha,
                blendMode =
                    BlendMode.Screen
            )

            /*
             * ====================================================
             * DESTELLOS EXTRA DE LA APP
             * ====================================================
             */

            drawSparkle(
                center =
                    center +
                        Offset(
                            -minDimension *
                                0.27f,
                            -minDimension *
                                0.24f
                        ),
                radius =
                    minDimension *
                        0.028f,
                color =
                    Color.White.copy(
                        alpha =
                            0.78f *
                                alpha
                    )
            )

            drawSparkle(
                center =
                    center +
                        Offset(
                            minDimension *
                                0.28f,
                            minDimension *
                                0.25f
                        ),
                radius =
                    minDimension *
                        0.022f,
                color =
                    Color.White.copy(
                        alpha =
                            0.72f *
                                alpha
                    )
            )
        }
    }
}


/*
 * ================================================================
 * DESTELLO
 * ================================================================
 */

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSparkle(
    center: Offset,
    radius: Float,
    color: Color
) {

    val path =
        androidx.compose.ui.graphics.Path()

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
