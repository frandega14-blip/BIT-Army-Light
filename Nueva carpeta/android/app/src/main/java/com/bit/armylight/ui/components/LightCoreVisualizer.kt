/*
 * ================================================================
 * SÍMBOLO CENTRAL PREMIUM — ORIGINAL
 * ================================================================
 *
 * Diseño abstracto de energía/cristal:
 * - 4 puntas curvas
 * - núcleo brillante
 * - doble contorno
 * - degradado blanco/lila/violeta
 * - brillo exterior
 *
 * No utiliza símbolos ni logotipos de BTS.
 */

private fun DrawScope.drawMainFigure(
    center: Offset,
    radius: Float,
    alpha: Float
) {

    val outerPath = Path()

    /*
     * ------------------------------------------------------------
     * PUNTA SUPERIOR
     * ------------------------------------------------------------
     */

    outerPath.moveTo(
        center.x,
        center.y - radius
    )

    outerPath.cubicTo(
        center.x + radius * 0.07f,
        center.y - radius * 0.72f,
        center.x + radius * 0.22f,
        center.y - radius * 0.30f,
        center.x + radius,
        center.y
    )

    /*
     * ------------------------------------------------------------
     * PUNTA DERECHA → INFERIOR
     * ------------------------------------------------------------
     */

    outerPath.cubicTo(
        center.x + radius * 0.30f,
        center.y + radius * 0.22f,
        center.x + radius * 0.10f,
        center.y + radius * 0.72f,
        center.x,
        center.y + radius
    )

    /*
     * ------------------------------------------------------------
     * PUNTA INFERIOR → IZQUIERDA
     * ------------------------------------------------------------
     */

    outerPath.cubicTo(
        center.x - radius * 0.10f,
        center.y + radius * 0.72f,
        center.x - radius * 0.30f,
        center.y + radius * 0.22f,
        center.x - radius,
        center.y
    )

    /*
     * ------------------------------------------------------------
     * PUNTA IZQUIERDA → SUPERIOR
     * ------------------------------------------------------------
     */

    outerPath.cubicTo(
        center.x - radius * 0.22f,
        center.y - radius * 0.30f,
        center.x - radius * 0.07f,
        center.y - radius * 0.72f,
        center.x,
        center.y - radius
    )

    outerPath.close()


    /*
     * ============================================================
     * 1. GLOW EXTERIOR
     * ============================================================
     */

    drawPath(
        path = outerPath,
        color = Color(0xFFB65CFF).copy(
            alpha = 0.22f * alpha
        ),
        style = Stroke(
            width = radius * 0.24f,
            join = StrokeJoin.Round
        )
    )


    /*
     * ============================================================
     * 2. SEGUNDO HALO
     * ============================================================
     */

    drawPath(
        path = outerPath,
        color = Color.White.copy(
            alpha = 0.22f * alpha
        ),
        style = Stroke(
            width = radius * 0.12f,
            join = StrokeJoin.Round
        )
    )


    /*
     * ============================================================
     * 3. CUERPO PRINCIPAL
     * ============================================================
     */

    drawPath(
        path = outerPath,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(
                    alpha = 1f * alpha
                ),
                Color(0xFFF9E9FF).copy(
                    alpha = 1f * alpha
                ),
                Color(0xFFE0A8FF).copy(
                    alpha = 0.99f * alpha
                ),
                Color(0xFFB25CFF).copy(
                    alpha = 0.98f * alpha
                ),
                Color(0xFF7028D9).copy(
                    alpha = 0.98f * alpha
                )
            ),
            start = Offset(
                center.x - radius,
                center.y - radius
            ),
            end = Offset(
                center.x + radius,
                center.y + radius
            )
        )
    )


    /*
     * ============================================================
     * 4. REFLEJO INTERNO SUPERIOR
     * ============================================================
     */

    val innerHighlight = Path()

    innerHighlight.moveTo(
        center.x,
        center.y - radius * 0.82f
    )

    innerHighlight.cubicTo(
        center.x + radius * 0.05f,
        center.y - radius * 0.57f,
        center.x + radius * 0.10f,
        center.y - radius * 0.30f,
        center.x + radius * 0.64f,
        center.y - radius * 0.06f
    )

    innerHighlight.cubicTo(
        center.x + radius * 0.36f,
        center.y - radius * 0.10f,
        center.x + radius * 0.17f,
        center.y - radius * 0.07f,
        center.x,
        center.y
    )

    innerHighlight.cubicTo(
        center.x - radius * 0.17f,
        center.y - radius * 0.07f,
        center.x - radius * 0.36f,
        center.y - radius * 0.10f,
        center.x - radius * 0.64f,
        center.y - radius * 0.06f
    )

    innerHighlight.cubicTo(
        center.x - radius * 0.10f,
        center.y - radius * 0.30f,
        center.x - radius * 0.05f,
        center.y - radius * 0.57f,
        center.x,
        center.y - radius * 0.82f
    )

    innerHighlight.close()

    drawPath(
        path = innerHighlight,
        color = Color.White.copy(
            alpha = 0.30f * alpha
        )
    )


    /*
     * ============================================================
     * 5. NÚCLEO CENTRAL
     * ============================================================
     */

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(
                    alpha = 1f * alpha
                ),
                Color.White.copy(
                    alpha = 0.92f * alpha
                ),
                Color(0xFFF0C9FF).copy(
                    alpha = 0.65f * alpha
                ),
                Color(0xFFC56AFF).copy(
                    alpha = 0.22f * alpha
                ),
                Color.Transparent
            ),
            center = center,
            radius = radius * 0.48f
        ),
        center = center,
        radius = radius * 0.48f
    )


    /*
     * ============================================================
     * 6. CRUZ DE ENERGÍA
     * ============================================================
     */

    drawLine(
        color = Color.White.copy(
            alpha = 0.72f * alpha
        ),
        start = Offset(
            center.x,
            center.y - radius * 0.60f
        ),
        end = Offset(
            center.x,
            center.y + radius * 0.60f
        ),
        strokeWidth = radius * 0.022f,
        cap = StrokeCap.Round
    )

    drawLine(
        color = Color.White.copy(
            alpha = 0.65f * alpha
        ),
        start = Offset(
            center.x - radius * 0.60f,
            center.y
        ),
        end = Offset(
            center.x + radius * 0.60f,
            center.y
        ),
        strokeWidth = radius * 0.018f,
        cap = StrokeCap.Round
    )


    /*
     * ============================================================
     * 7. BORDE PREMIUM
     * ============================================================
     */

    drawPath(
        path = outerPath,
        color = Color.White.copy(
            alpha = 0.88f * alpha
        ),
        style = Stroke(
            width = radius * 0.018f,
            join = StrokeJoin.Round
        )
    )


    /*
     * ============================================================
     * 8. CUATRO MICRODESTELLOS EN EL NÚCLEO
     * ============================================================
     */

    drawSparkle(
        center = center + Offset(
            0f,
            -radius * 0.92f
        ),
        radius = radius * 0.055f,
        color = Color.White.copy(
            alpha = 0.95f * alpha
        )
    )

    drawSparkle(
        center = center + Offset(
            radius * 0.92f,
            0f
        ),
        radius = radius * 0.045f,
        color = Color.White.copy(
            alpha = 0.90f * alpha
        )
    )

    drawSparkle(
        center = center + Offset(
            0f,
            radius * 0.92f
        ),
        radius = radius * 0.040f,
        color = Color.White.copy(
            alpha = 0.85f * alpha
        )
    )

    drawSparkle(
        center = center + Offset(
            -radius * 0.92f,
            0f
        ),
        radius = radius * 0.045f,
        color = Color.White.copy(
            alpha = 0.90f * alpha
        )
    )
}
