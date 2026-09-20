package wastetrack.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import wastetrack.engine.isPointInPolygon
import wastetrack.ui.DEFAULT_SCENARIO_BY_VEHICLE_ID
import wastetrack.ui.FleetState
import wastetrack.ui.components.StatusBanner
import wastetrack.ui.components.ZoneCanvas
import wastetrack.ui.formatKg
import wastetrack.ui.theme.paletteFor

// ASSUMPTION: vehicle "capacity" isn't part of the domain model (§8's
// Vehicle has no capacity field) — this is a display-only estimate for the
// load bar, not something the classifier uses.
private const val CAPACITY_MULTIPLIER = 1.6

/** CLAUDE.md §13 driver screen 1 — the driver's home screen. */
@Composable
fun LiveShiftScreen(state: FleetState, modifier: Modifier = Modifier) {
    val palette = paletteFor(state.darkTheme)
    val snapshot = state.selectedVehicle
    val vehicle = snapshot.vehicle
    val currentZone = snapshot.currentReading?.coordinate?.let { coordinate ->
        state.zones.firstOrNull { isPointInPolygon(coordinate, it.boundary) }
    }
    val routeCoordinates = remember(vehicle.id) {
        DEFAULT_SCENARIO_BY_VEHICLE_ID[vehicle.id].orEmpty().map { it.coordinate }
    }
    val displayZone = currentZone ?: state.zones.first()
    val currentLoad = snapshot.currentReading?.loadKg ?: vehicle.tareKg
    val capacity = vehicle.tareKg * CAPACITY_MULTIPLIER
    val hasDroppedOff = snapshot.visits.isNotEmpty()
    val stillLoaded = currentLoad > vehicle.tareKg + vehicle.toleranceKg

    Column(modifier = modifier.fillMaxSize().background(palette.background).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Live shift", fontWeight = FontWeight.Bold, color = palette.onSurface)
                Text(text = "${vehicle.id} · ${vehicle.driverName}", color = palette.onSurfaceMuted, fontFamily = FontFamily.Monospace)
            }
            Surface(color = palette.inProgressBg, shape = RoundedCornerShape(999.dp)) {
                Text(text = "Simulated", color = palette.inProgressFg, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Surface(color = palette.surface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            ZoneCanvas(
                zone = displayZone,
                route = routeCoordinates,
                currentPosition = snapshot.currentReading?.coordinate,
                palette = palette,
                modifier = Modifier.fillMaxWidth().height(220.dp).padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        when {
            currentZone != null -> StatusBanner(
                title = "Inside ${currentZone.name}",
                body = "Unload here and your shift moves toward Compliant.",
                foreground = palette.compliantFg,
                background = palette.compliantBg
            )
            state.reminderEnabled && stillLoaded && !hasDroppedOff -> StatusBanner(
                title = "You have not dropped off yet",
                body = "Head to an authorized zone before your shift ends so it counts. This is a reminder, not a penalty.",
                foreground = palette.partialFg,
                background = palette.partialBg
            )
            else -> StatusBanner(
                title = "En route",
                body = "Not at an authorized zone right now.",
                foreground = palette.onSurfaceMuted,
                background = palette.surfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Surface(color = palette.surface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "CURRENT LOAD", color = palette.onSurfaceMuted)
                        Text(text = currentLoad.formatKg(), color = palette.onSurface, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Column {
                        Text(text = "EMPTY WEIGHT", color = palette.onSurfaceMuted)
                        Text(text = vehicle.tareKg.formatKg(), color = palette.onSurface, fontFamily = FontFamily.Monospace)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LoadBar(fraction = (currentLoad / capacity).toFloat().coerceIn(0f, 1f), palette.partialFg, palette.surfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "${(currentLoad - vehicle.tareKg).formatKg()} on board", color = palette.onSurfaceMuted, modifier = Modifier.weight(1f))
                    Text(text = "capacity ${capacity.formatKg()}", color = palette.onSurfaceMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Surface(color = palette.surface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "SHIFT STATUS", color = palette.onSurfaceMuted)
                    Text(text = if (snapshot.verdict.name == "IN_PROGRESS") "In progress" else snapshot.verdict.name, color = palette.onSurface, fontWeight = FontWeight.SemiBold)
                }
                Text(text = "${snapshot.visits.size} drop-offs", color = palette.onSurfaceMuted)
            }
        }
    }
}

@Composable
private fun LoadBar(fraction: Float, fillColor: androidx.compose.ui.graphics.Color, trackColor: androidx.compose.ui.graphics.Color) {
    Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(trackColor, RoundedCornerShape(999.dp))) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(8.dp)
                .background(fillColor, RoundedCornerShape(999.dp))
        )
    }
}
