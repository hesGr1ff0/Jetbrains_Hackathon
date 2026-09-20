package wastetrack.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import wastetrack.engine.Reading
import wastetrack.ui.theme.Palette

/**
 * Load-over-time line with a dashed tare line and shaded tolerance band —
 * CLAUDE.md §13's admin Vehicle Detail chart.
 */
@Composable
fun LoadOverTimeChart(
    history: List<Reading>,
    tareKg: Double,
    toleranceKg: Double,
    palette: Palette,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.height(180.dp).clipToBounds()) {
        // See ZoneCanvas.kt: Canvas paints its own opaque (white) backing on
        // this target rather than showing the parent Surface through, and
        // does not clip drawing to its own bounds by default.
        drawRect(color = palette.surface)

        if (history.size < 2) return@Canvas

        val minTime = history.first().timestampMs.toFloat()
        val maxTime = history.last().timestampMs.toFloat()
        val timeSpan = (maxTime - minTime).let { if (it > 0f) it else 1f }
        val maxLoad = maxOf(history.maxOf { it.loadKg }, tareKg + toleranceKg).toFloat()
        val loadSpan = maxLoad.let { if (it > 0f) it else 1f }

        fun x(timestampMs: Long): Float = ((timestampMs - minTime) / timeSpan) * size.width
        fun y(loadKg: Double): Float = size.height - (loadKg.toFloat() / loadSpan) * size.height

        val bandTop = y(tareKg + toleranceKg)
        val bandBottom = y(tareKg - toleranceKg)
        drawRect(
            color = palette.compliantFg.copy(alpha = 0.15f),
            topLeft = Offset(0f, bandTop),
            size = Size(size.width, bandBottom - bandTop)
        )

        val tareY = y(tareKg)
        drawLine(
            color = palette.compliantFg,
            start = Offset(0f, tareY),
            end = Offset(size.width, tareY),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
        )

        val loadPath = Path().apply {
            history.forEachIndexed { index, reading ->
                val px = x(reading.timestampMs)
                val py = y(reading.loadKg)
                if (index == 0) moveTo(px, py) else lineTo(px, py)
            }
        }
        drawPath(loadPath, color = palette.inProgressFg, style = Stroke(width = 2f))
    }
}
