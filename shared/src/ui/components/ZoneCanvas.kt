package wastetrack.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import wastetrack.engine.Coordinate
import wastetrack.engine.Zone

// How far past the zone's own edge the view extends, as a multiple of the
// zone's own span — 0.8 means the zone fills roughly the middle third of
// the canvas, with just enough surrounding route visible for context.
private const val ZONE_VIEW_MARGIN_FACTOR = 0.8

/**
 * Draws a zone boundary, a route polyline, and a moving dot for the current
 * position, zoomed to the zone itself (not the whole route — a 30-point
 * route spans kilometers while the zone is ~200m, so fitting both would
 * shrink the zone to a speck). Route/position points outside this zoomed
 * view are simply clipped by the Canvas, which is the intended effect.
 * Latitude increases upward but screen y increases downward, so the y axis
 * is inverted.
 */
@Composable
fun ZoneCanvas(
    zone: Zone,
    route: List<Coordinate>,
    currentPosition: Coordinate?,
    modifier: Modifier = Modifier
) {
    val zoneLats = zone.boundary.map { it.lat }
    val zoneLngs = zone.boundary.map { it.lng }
    val zoneMinLat = zoneLats.min()
    val zoneMaxLat = zoneLats.max()
    val zoneMinLng = zoneLngs.min()
    val zoneMaxLng = zoneLngs.max()
    val latMargin = (zoneMaxLat - zoneMinLat) * ZONE_VIEW_MARGIN_FACTOR
    val lngMargin = (zoneMaxLng - zoneMinLng) * ZONE_VIEW_MARGIN_FACTOR
    val minLat = zoneMinLat - latMargin
    val maxLat = zoneMaxLat + latMargin
    val minLng = zoneMinLng - lngMargin
    val maxLng = zoneMaxLng + lngMargin
    val latSpan = (maxLat - minLat).let { if (it > 0.0) it else 1.0 }
    val lngSpan = (maxLng - minLng).let { if (it > 0.0) it else 1.0 }

    Canvas(modifier = modifier) {
        val paddingPx = 24f

        fun project(c: Coordinate): Offset {
            val x = paddingPx + ((c.lng - minLng) / lngSpan) * (size.width - 2 * paddingPx)
            val y = paddingPx + (1.0 - (c.lat - minLat) / latSpan) * (size.height - 2 * paddingPx)
            return Offset(x.toFloat(), y.toFloat())
        }

        val zonePath = Path().apply {
            zone.boundary.forEachIndexed { index, coordinate ->
                val point = project(coordinate)
                if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
            }
            close()
        }
        drawPath(zonePath, color = Color(0xFF2E7D32), style = Stroke(width = 3f))

        if (route.size >= 2) {
            val routePath = Path().apply {
                route.forEachIndexed { index, coordinate ->
                    val point = project(coordinate)
                    if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
                }
            }
            drawPath(routePath, color = Color(0xFF1565C0), style = Stroke(width = 2f))
        }

        currentPosition?.let {
            drawCircle(color = Color(0xFFC62828), radius = 8f, center = project(it))
        }
    }
}
