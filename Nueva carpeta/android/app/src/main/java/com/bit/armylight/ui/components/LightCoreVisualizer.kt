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
     * ANIMACIONES GENERALES
     * ============================================================
     */

    val infiniteTransition =
        rememberInfiniteTransition(
            label = "BIT-Light"
        )

    /*
     * Respiración muy suave de la figura.
     */
    val breathing by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
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
        targetValue = 1.12f,
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
     * Movimiento lento para ondas.
     */
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 3600,
                        easing = FastOutSlowInEasing
                    ),
                repeatMode = RepeatMode.Restart
            ),
        label = "wave"
    )

    /*
     * Movimiento rápido para Strobe.
     */
    val strobePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 360,
                        easing = FastOutSlowInEasing
                    ),
                repeatMode = RepeatMode.Restart
            ),
        label = "strobe"
    )

    /*
     * Explosión de Supernova.
     */
    val supernovaPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 1700,
                        easing = FastOutSlowInEasing
                    ),
                repeatMode = RepeatMode.Restart
            ),
        label = "supernova"
    )

    /*
     * Movimiento de Aurora.
     */
    val auroraPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 6000,
                        easing = FastOutSlowInEasing
                    ),
                repeatMode = RepeatMode.Reverse
            ),
        label = "aurora"
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
                        durationMillis = 9000,
                        easing = FastOutSlowInEasing
                    ),
                repeatMode = RepeatMode.Restart
            ),
        label = "orbit"
    )


    /*
     * ============================================================
     * VALORES
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

    val baseAlpha =
        if (isBatterySaver) {
            0.68f
        } else {
            1f
        }


    /*
     * ============================================================
     * ESCALA DE LA FIGURA
     * ============================================================
     *
     * La figura es GRANDE, como en la imagen de referencia.
     */

    val figureScale =
        when {

            isRhythmActive ->
                1f +
                    rhythm * 0.10f

            isPulseActive ->
                pulseAnimation

            else ->
                breathing
        }


    /*
     * ============================================================
     * VIBRACIÓN
     * ============================================================
     */

    var lastPeak by remember {
        mutableLongStateOf(0L)
    }

    /*
     * RITMO:
     * vibra cuando aparece un pico real de audio.
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
     * vibración sincronizada con el efecto visual.
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


            /*
             * ====================================================
             * FONDO BASE
             * ====================================================
             */

            drawRect(
                color =
                    Color(0xFF070009)
            )


            /*
             * ====================================================
             * EFECTOS DE LOS MODOS
             * ====================================================
             *
             * Cada modo cambia la FORMA de emitir la luz.
             * No estamos limitando cada modo a un solo color.
             */

            when {

                /*
                 * =================================================
                 * ONDA PÚRPURA
                 * =================================================
                 *
                 * La luz se desplaza por la pantalla en ondas.
                 */

                isConcertActive &&
                    concertProgram.title.equals(
                        "Onda Púrpura",
                        ignoreCase = true
                    ) -> {

                    val x1 =
                        -size.width +
                            size.width *
                            3f *
                            wavePhase

                    val x2 =
                        x1 +
                            size.width * 0.70f

                    drawRect(
                        brush =
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        Color(0xFF24002F),
                                        Color(0xFF7B00FF),
                                        Color(0xFFFF20D4),
                                        Color(0xFF6A00FF),
                                        Color(0xFF16001F)
                                    ),
                                start =
                                    Offset(
                                        x = x1,
                                        y = 0f
                                    ),
                                end =
                                    Offset(
                                        x = x2,
                                        y =
                                            size.height
                                    )
                            )
                    )

                    /*
                     * Segunda onda.
                     */

                    drawRect(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        Color(0xFFFF4DE1).copy(
                                            alpha = 0.42f
                                        ),
                                        Color(0xFF8A00FF).copy(
                                            alpha = 0.25f
                                        ),
                                        Color.Transparent
                                    ),
                                center =
                                    Offset(
                                        x =
                                            size.width *
                                                (
                                                    1f -
                                                        wavePhase
                                                ),
                                        y =
                                            size.height *
                                                0.42f
                                    ),
                                radius =
                                    size.maxDimension *
                                        0.75f
                            )
                    )
                }


                /*
                 * =================================================
                 * STROBE BEAT
                 * =================================================
                 *
                 * La luz aparece y desaparece rápidamente.
                 */

                isConcertActive &&
                    concertProgram.title.equals(
                        "Strobe Beat",
                        ignoreCase = true
                    ) -> {

                    val flash =
                        strobePhase < 0.18f

                    if (flash) {

                        drawRect(
                            color =
                                Color.White
                        )

                    } else {

                        drawRect(
                            brush =
                                Brush.radialGradient(
                                    colors =
                                        listOf(
                                            Color(0xFFF0C8FF),
                                            Color(0xFF7D00FF),
                                            Color(0xFF15001F)
                                        ),
                                    center =
                                        center,
                                    radius =
                                        size.maxDimension *
                                            0.90f
                                )
                        )
                    }
                }


                /*
                 * =================================================
                 * SUPERNOVA
                 * =================================================
                 *
                 * La luz nace en el centro y explota hacia afuera.
                 */

                isConcertActive &&
                    concertProgram.title.equals(
                        "Supernova",
                        ignoreCase = true
                    ) -> {

                    val explosion =
                        supernovaPhase

                    val radius =
                        size.maxDimension *
                            (
                                0.12f +
                                    explosion *
                                    1.05f
                            )

                    drawCircle(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        Color.White.copy(
                                            alpha =
                                                0.85f
                                        ),
                                        Color(0xFFFFB7FF).copy(
                                            alpha =
                                                0.65f
                                        ),
                                        Color(0xFFB000FF).copy(
                                            alpha =
                                                0.45f
                                        ),
                                        Color(0xFF3A004F).copy(
                                            alpha =
                                                0.20f
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
                     * Segundo anillo de expansión.
                     */

                    val ringRadius =
                        size.maxDimension *
                            (
                                0.25f +
                                    explosion *
                                    0.90f
                            )

                    drawCircle(
                        color =
                            Color.White.copy(
                                alpha =
                                    (
                                        1f -
                                            explosion
                                    ).coerceIn(
                                        0f,
                                        1f
                                    ) *
                                    0.45f
                            ),
                        center =
                            center,
                        radius =
                            ringRadius,
                        style =
                            Stroke(
                                width =
                                    12.dp.toPx()
                            )
                    )
                }


                /*
                 * =================================================
                 * AURORA
                 * =================================================
                 *
                 * Capas de luz lentas y fluidas.
                 */

                isConcertActive &&
                    concertProgram.title.equals(
                        "Aurora",
                        ignoreCase = true
                    ) -> {

                    val phase =
                        auroraPhase

                    /*
                     * Primera cortina.
                     */

                    drawRect(
                        brush =
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        Color(0xFF17002C),
                                        Color(0xFF6C00A8),
                                        Color(0xFF00A6FF),
                                        Color(0xFF00E6B8),
                                        Color(0xFF6C00A8),
                                        Color(0xFF17002C)
                                    ),
                                start =
                                    Offset(
                                        x =
                                            -size.width +
                                                size.width *
                                                2f *
                                                phase,
                                        y = 0f
                                    ),
                                end =
                                    Offset(
                                        x =
                                            size.width *
                                                2f *
                                                phase,
                                        y =
                                            size.height
                                    )
                            )
                    )

                    /*
                     * Segunda cortina.
                     */

                    drawCircle(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        Color(0xFF00E5FF).copy(
                                            alpha =
                                                0.32f
                                        ),
                                        Color(0xFF7A00FF).copy(
                                            alpha =
                                                0.25f
                                        ),
                                        Color.Transparent
                                    ),
                                center =
                                    Offset(
                                        x =
                                            size.width *
                                                (
                                                    0.25f +
                                                        phase *
                                                        0.5f
                                                ),
                                        y =
                                            size.height *
                                                0.38f
                                    ),
                                radius =
                                    size.maxDimension *
                                        0.72f
                            ),
                        center =
                            Offset(
                                x =
                                    size.width *
                                        (
                                            0.25f +
                                                phase *
                                                0.5f
                                        ),
                                y =
                                    size.height *
                                        0.38f
                            ),
                        radius =
                            size.maxDimension *
                                0.72f
                    )
                }


                /*
                 * =================================================
                 * RITMO
                 * =================================================
                 *
                 * El fondo respira y reacciona al sonido.
                 */

                isRhythmActive -> {

                    val energy =
                        rhythm.coerceIn(
                            0f,
                            1f
                        )

                    drawRect(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        Color.White.copy(
                                            alpha =
                                                0.12f +
                                                    energy *
                                                    0.30f
                                        ),
                                        Color(0xFFD000FF).copy(
                                            alpha =
                                                0.35f +
                                                    energy *
                                                    0.25f
                                        ),
                                        Color(0xFF31004A).copy(
                                            alpha =
                                                0.75f
                                        ),
                                        Color(0xFF050008)
                                    ),
                                center =
                                    center,
                                radius =
                                    size.maxDimension *
                                        (
                                            0.40f +
                                                energy *
                                                0.80f
                                        )
                            )
                    )
                }


                /*
                 * =================================================
                 * PULSO
                 * =================================================
                 *
                 * Expansión y contracción desde el centro.
                 */

                isPulseActive -> {

                    val amount =
                        (
                            pulseAnimation -
                                0.88f
                        ) /
                        0.24f

                    drawRect(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        Color.White.copy(
                                            alpha =
                                                0.10f +
                                                    amount *
                                                    0.25f
                                        ),
                                        Color(0xFFD600FF).copy(
                                            alpha =
                                                0.40f +
                                                    amount *
                                                    0.25f
                                        ),
                                        Color(0xFF4A0066),
                                        Color(0xFF050008)
                                    ),
                                center =
                                    center,
                                radius =
                                    size.maxDimension *
                                        (
                                            0.45f +
                                                amount *
                                                0.50f
                                        )
                            )
                    )
                }
            }


            /*
             * ====================================================
             * HALO PRINCIPAL DE LA FIGURA
             * ====================================================
             */

            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                Color.White.copy(
                                    alpha =
                                        0.28f *
                                            baseAlpha
                                ),
                                Color(0xFFE5B5FF).copy(
                                    alpha =
                                        0.22f *
                                            baseAlpha
                                ),
                                Color(0xFFA000FF).copy(
                                    alpha =
                                        0.12f *
                                            baseAlpha
                                ),
                                Color.Transparent
                            ),
                        center =
                            center,
                        radius =
                            size.minDimension *
                                0.38f *
                                figureScale
                    ),
                center =
                    center,
                radius =
                    size.minDimension *
                        0.38f *
                        figureScale
            )


            /*
             * ====================================================
             * ÓRBITA
             * ====================================================
             *
             * La órbita de la imagen de referencia es grande,
             * gruesa y claramente visible.
             */

            drawOrbit(
                center =
                    center,
                radiusX =
                    size.minDimension *
                        0.40f *
                        figureScale,
                radiusY =
                    size.minDimension *
                        0.155f *
                        figureScale,
                rotationDegrees =
                    -18f +
                        orbitRotation *
                        0.015f,
                color =
                    Color.White.copy(
                        alpha =
                            0.96f *
                                baseAlpha
                    ),
                strokeWidth =
                    size.minDimension *
                        0.022f
            )


            /*
             * ====================================================
             * FIGURA PRINCIPAL
             * ====================================================
             *
             * ESTA ES LA PARTE IMPORTANTE.
             *
             * No usamos un simple rombo de líneas rectas.
             *
             * Se dibuja una estrella de cuatro puntas con
             * lados CURVOS para acercarse a la figura de la
             * imagen de referencia.
             */

            drawMainDiamond(
                center =
                    center,
                radius =
                    size.minDimension *
                        0.245f *
                        figureScale,
                alpha =
                    baseAlpha
            )


            /*
             * ====================================================
             * BRILLO CENTRAL
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
                    size.minDimension *
                        0.018f *
                        figureScale
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
                                -size.minDimension *
                                    0.205f *
                                    figureScale,
                            y =
                                -size.minDimension *
                                    0.17f *
                                    figureScale
                        ),
                radius =
                    size.minDimension *
                        0.035f *
                        figureScale,
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
                                size.minDimension *
                                    0.205f *
                                    figureScale,
                            y =
                                -size.minDimension *
                                    0.185f *
                                    figureScale
                        ),
                radius =
                    size.minDimension *
                        0.030f *
                        figureScale,
                color =
                    Color.White.copy(
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
                                size.minDimension *
                                    0.235f *
                                    figureScale,
                            y =
                                size.minDimension *
                                    0.18f *
                                    figureScale
                        ),
                radius =
                    size.minDimension *
                        0.027f *
                        figureScale,
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
                                -size.minDimension *
                                    0.20f *
                                    figureScale,
                            y =
                                size.minDimension *
                                    0.22f *
                                    figureScale
                        ),
                radius =
                    size.minDimension *
                        0.021f *
                        figureScale,
                color =
                    Color.White.copy(
                        alpha =
                            0.88f *
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

                val extra =
                    rhythm *
                        0.9f

                drawSparkle(
                    center =
                        center +
                            Offset(
                                x = 0f,
                                y =
                                    -size.minDimension *
                                        0.31f *
                                        figureScale
                            ),
                    radius =
                        size.minDimension *
                            (
                                0.018f +
                                    rhythm *
                                    0.025f
                            ),
                    color =
                        Color.White.copy(
                            alpha =
                                extra
                        )
                )

                drawSparkle(
                    center =
                        center +
                            Offset(
                                x = 0f,
                                y =
                                    size.minDimension *
                                        0.31f *
                                        figureScale
                            ),
                    radius =
                        size.minDimension *
                            (
                                0.015f +
                                    rhythm *
                                    0.022f
                            ),
                    color =
                        Color.White.copy(
                            alpha =
                                extra
                        )
                )
            }
        }
    }
}


/*
 * ================================================================
 * FIGURA PRINCIPAL
 * ================================================================
 *
 * ESTRELLA / ROMBO DE CUATRO PUNTAS.
 *
 * Los cuatro lados son CURVOS, no rectos.
 *
 * El degradado intenta reproducir la referencia:
 *
 *     blanco
 *       ↓
 *   lavanda
 *       ↓
 *    violeta
 *
 * El núcleo permanece luminoso.
 */

private fun DrawScope.drawMainDiamond(
    center: Offset,
    radius: Float,
    alpha: Float
) {

    val path =
        Path()

    val top =
        Offset(
            center.x,
            center.y -
                radius
        )

    val right =
        Offset(
            center.x +
                radius,
            center.y
        )

    val bottom =
        Offset(
            center.x,
            center.y +
                radius
        )

    val left =
        Offset(
            center.x -
                radius,
            center.y
        )


    /*
     * ============================================================
     * PUNTA SUPERIOR → DERECHA
     * ============================================================
     *
     * Curva hacia el centro.
     */

    path.moveTo(
        top.x,
        top.y
    )

    path.cubicTo(
        center.x +
            radius * 0.18f,
        center.y -
            radius * 0.46f,

        center.x +
            radius * 0.22f,
        center.y -
            radius * 0.12f,

        right.x,
        right.y
    )


    /*
     * ============================================================
     * DERECHA → ABAJO
     * ============================================================
     */

    path.cubicTo(
        center.x +
            radius * 0.46f,
        center.y +
            radius * 0.18f,

        center.x +
            radius * 0.18f,
        center.y +
            radius * 0.22f,

        bottom.x,
        bottom.y
    )


    /*
     * ============================================================
     * ABAJO → IZQUIERDA
     * ============================================================
     */

    path.cubicTo(
        center.x -
            radius * 0.18f,
        center.y +
            radius * 0.46f,

        center.x -
            radius * 0.22f,
        center.y +
            radius * 0.12f,

        left.x,
        left.y
    )


    /*
     * ============================================================
     * IZQUIERDA → ARRIBA
     * ============================================================
     */

    path.cubicTo(
        center.x -
            radius * 0.46f,
        center.y -
            radius * 0.18f,

        center.x -
            radius * 0.18f,
        center.y -
            radius * 0.22f,

        top.x,
        top.y
    )

    path.close()


    /*
     * ============================================================
     * RESPLANDOR EXTERIOR
     * ============================================================
     */

    drawPath(
        path =
            path,
        color =
            Color.White.copy(
                alpha =
                    0.32f *
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
     * ============================================================
     * CUERPO PRINCIPAL
     * ============================================================
     *
     * Degradado diagonal:
     *
     * arriba-izquierda = blanco
     * centro            = lavanda
     * abajo-derecha     = violeta
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

                        Color(0xFFF0DFFF).copy(
                            alpha =
                                0.99f *
                                    alpha
                        ),

                        Color(0xFFB97CFF).copy(
                            alpha =
                                0.98f *
                                    alpha
                        ),

                        Color(0xFF6E35FF).copy(
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
     * CRUZ LUMINOSA INTERIOR
     * ============================================================
     *
     * Reproduce la división luminosa que se ve en la referencia.
     */

    drawLine(
        color =
            Color.White.copy(
                alpha =
                    0.82f *
                        alpha
            ),
        start =
            Offset(
                center.x,
                center.y -
                    radius *
                    0.72f
            ),
        end =
            Offset(
                center.x,
                center.y +
                    radius *
                    0.72f
            ),
        strokeWidth =
            radius *
                0.025f
    )

    drawLine(
        color =
            Color.White.copy(
                alpha =
                    0.68f *
                        alpha
            ),
        start =
            Offset(
                center.x -
                    radius *
                    0.72f,
                center.y
            ),
        end =
            Offset(
                center.x +
                    radius *
                    0.72f,
                center.y
            ),
        strokeWidth =
            radius *
                0.020f
    )


    /*
     * ============================================================
     * BRILLO DEL CENTRO
     * ============================================================
     */

    drawCircle(
        brush =
            Brush.radialGradient(
                colors =
                    listOf(
                        Color.White.copy(
                            alpha =
                                0.75f *
                                    alpha
                        ),
                        Color.White.copy(
                            alpha =
                                0.18f *
                                    alpha
                        ),
                        Color.Transparent
                    ),
                center =
                    center,
                radius =
                    radius *
                        0.32f
            ),
        center =
            center,
        radius =
            radius *
                0.32f
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
                androidx.compose.ui.geometry.Size(
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
