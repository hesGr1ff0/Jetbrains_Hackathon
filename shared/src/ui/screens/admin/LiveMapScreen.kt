package wastetrack.ui.screens.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import wastetrack.engine.Coordinate
import wastetrack.engine.Zone
import wastetrack.ui.DEFAULT_SCENARIO_BY_VEHICLE_ID
import wastetrack.ui.FleetState
import wastetrack.ui.VehicleSnapshot
import wastetrack.ui.centroid
import wastetrack.ui.components.ZoneCanvas
import wastetrack.ui.theme.Palette
import wastetrack.ui.theme.paletteFor

/**
 * CLAUDE.md §13 admin screen 2. The city-wide map uses fixed-size markers
 * (the five real zones are 14-28km apart, so a to-scale 200m square would
 * be invisible at that zoom) — selecting a zone shows an inset zoomed view
 * drawn to scale via [ZoneCanvas]. The zone list panel (with source
 * citations) is folded in here rather than a separate zones page.
 *
 * DEVIATION from the reference mockup: the mockup shows a real Leaflet/
 * OpenStreetMap basemap with roads and coastline. CLAUDE.md explicitly
 * rules out network calls ("all data is simulated... not fetched from any
 * network"), so this draws a stylized flat map instead of real map tiles.
 */
@Composable
fun LiveMapScreen(state: FleetState, modifier: Modifier = Modifier) {
    val palette = paletteFor(state.darkTheme)
    var selectedZoneId by remember { mutableStateOf(state.zones.first().id) }
    val selectedZone = state.zones.first { it.id == selectedZoneId }
    val selectedSnapshot = state.selectedVehicle
    val routeCoordinates = remember(selectedSnapshot.vehicle.id) {
        DEFAULT_SCENARIO_BY_VEHICLE_ID[selectedSnapshot.vehicle.id].orEmpty().map { it.coordinate }
    }

    Column(modifier = modifier.fillMaxSize().background(palette.background).padding(24.dp)) {
        Text(text = "Live map", fontWeight = FontWeight.Bold, color = palette.onSurface)
        Text(text = "${state.zones.size} authorized zones · markers not to scale", color = palette.onSurfaceMuted)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(color = palette.surface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    CityWideMap(
                        zones = state.zones,
                        vehicles = state.vehicles.values.toList(),
                        palette = palette,
                        modifier = Modifier.fillMaxWidth().height(220.dp).padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Surface(color = palette.surface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "${selectedZone.name} — to scale (${selectedSnapshot.vehicle.id})",
                            color = palette.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ZoneCanvas(
                            zone = selectedZone,
                            route = routeCoordinates,
                            currentPosition = selectedSnapshot.currentReading?.coordinate,
                            palette = palette,
                            modifier = Modifier.fillMaxWidth().height(300.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Authorized zones", color = palette.onSurface, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                state.zones.forEach { zone ->
                    Surface(
                        color = if (zone.id == selectedZoneId) palette.compliantBg else palette.surface,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedZoneId = zone.id }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = zone.name, color = palette.onSurface, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = zone.source, color = palette.onSurfaceMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CityWideMap(zones: List<Zone>, vehicles: List<VehicleSnapshot>, palette: Palette, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.clipToBounds()) {
        // See ZoneCanvas.kt: Canvas paints its own opaque (white) backing on
        // this target rather than showing the parent Surface through, and
        // does not clip drawing to its own bounds by default.
        drawRect(color = palette.surface)

        val zoneCenters = zones.map { it.centroid() }
        val vehiclePositions = vehicles.mapNotNull { it.currentReading?.coordinate }
        val allPoints = zoneCenters + vehiclePositions
        if (allPoints.isEmpty()) return@Canvas

        val minLat = allPoints.minOf { it.lat }
        val maxLat = allPoints.maxOf { it.lat }
        val minLng = allPoints.minOf { it.lng }
        val maxLng = allPoints.maxOf { it.lng }
        val latSpan = (maxLat - minLat).let { if (it > 0.0) it else 1.0 }
        val lngSpan = (maxLng - minLng).let { if (it > 0.0) it else 1.0 }
        val paddingPx = 24f

        fun project(c: Coordinate): Offset {
            val x = paddingPx + ((c.lng - minLng) / lngSpan) * (size.width - 2 * paddingPx)
            val y = paddingPx + (1.0 - (c.lat - minLat) / latSpan) * (size.height - 2 * paddingPx)
            return Offset(x.toFloat(), y.toFloat())
        }

        zoneCenters.forEach { center ->
            drawCircle(color = palette.compliantFg, radius = 10f, center = project(center))
        }
        vehiclePositions.forEach { position ->
            drawCircle(color = palette.inProgressFg, radius = 6f, center = project(position))
        }
    }
}
