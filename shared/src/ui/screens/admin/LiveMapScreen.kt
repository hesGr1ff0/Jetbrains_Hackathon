package wastetrack.ui.screens.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import wastetrack.engine.Coordinate
import wastetrack.engine.Zone
import wastetrack.ui.DEFAULT_SCENARIO_BY_VEHICLE_ID
import wastetrack.ui.FleetState
import wastetrack.ui.VehicleSnapshot
import wastetrack.ui.centroid
import wastetrack.ui.components.ZoneCanvas

/**
 * CLAUDE.md §13 admin screen 2. The city-wide map uses fixed-size markers
 * (the five real zones are 14-28km apart, so a to-scale 200m square would
 * be invisible at that zoom) — selecting a zone shows an inset zoomed view
 * drawn to scale via [ZoneCanvas]. The zone list panel (with source
 * citations) is folded in here rather than a separate zones page.
 */
@Composable
fun LiveMapScreen(state: FleetState, modifier: Modifier = Modifier) {
    var selectedZoneId by remember { mutableStateOf(state.zones.first().id) }
    val selectedZone = state.zones.first { it.id == selectedZoneId }
    val selectedSnapshot = state.selectedVehicle
    val routeCoordinates = remember(selectedSnapshot.vehicle.id) {
        DEFAULT_SCENARIO_BY_VEHICLE_ID[selectedSnapshot.vehicle.id].orEmpty().map { it.coordinate }
    }

    Row(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            BasicText(text = "Live Map (city-wide — markers not to scale)")
            Spacer(modifier = Modifier.height(8.dp))
            CityWideMap(
                zones = state.zones,
                vehicles = state.vehicles.values.toList(),
                modifier = Modifier.fillMaxWidth().height(220.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            BasicText(text = "Zoomed to scale: ${selectedZone.name} (${selectedSnapshot.vehicle.id})")
            ZoneCanvas(
                zone = selectedZone,
                route = routeCoordinates,
                currentPosition = selectedSnapshot.currentReading?.coordinate,
                modifier = Modifier.fillMaxWidth().height(320.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            BasicText(text = "Authorized zones")
            state.zones.forEach { zone ->
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { selectedZoneId = zone.id }) { Text(zone.name) }
                Spacer(modifier = Modifier.height(2.dp))
                BasicText(text = zone.source)
            }
        }
    }
}

@Composable
private fun CityWideMap(zones: List<Zone>, vehicles: List<VehicleSnapshot>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
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
            drawCircle(color = Color(0xFF2E7D32), radius = 10f, center = project(center))
        }
        vehiclePositions.forEach { position ->
            drawCircle(color = Color(0xFF1565C0), radius = 6f, center = project(position))
        }
    }
}
