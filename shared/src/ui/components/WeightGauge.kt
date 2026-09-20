package wastetrack.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import wastetrack.ui.theme.Palette

/**
 * A horizontal bar comparing [currentKg] against [tareKg] ± [toleranceKg],
 * with a dashed tare line and a shaded tolerance band — the single-value
 * analogue of the load-over-time chart described in CLAUDE.md §13.
 */
@Composable
fun WeightGauge(
    currentKg: Double,
    tareKg: Double,
    toleranceKg: Double,
    maxScaleKg: Double,
    palette: Palette,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.height(32.dp).clipToBounds()) {
        // See ZoneCanvas.kt: Canvas paints its own opaque (white) backing on
        // this target rather than showing the parent Surface through, and
        // does not clip drawing to its own bounds by default.
        drawRect(color = palette.surface)

        val scale = size.width / maxScaleKg.toFloat()
        fun x(kg: Double): Float = (kg.toFloat() * scale).coerceIn(0f, size.width)

        val bandLeft = x(tareKg - toleranceKg)
        val bandRight = x(tareKg + toleranceKg)
        drawRect(
            color = palette.compliantFg.copy(alpha = 0.15f),
            topLeft = Offset(bandLeft, 0f),
            size = Size(bandRight - bandLeft, size.height)
        )

        drawRect(
            color = palette.inProgressFg,
            topLeft = Offset(0f, size.height * 0.25f),
            size = Size(x(currentKg), size.height * 0.5f)
        )

        val tareX = x(tareKg)
        drawLine(
            color = palette.compliantFg,
            start = Offset(tareX, 0f),
            end = Offset(tareX, size.height),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
        )
    }
}
