package wastetrack.ui.screens.driver

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import wastetrack.engine.isPointInPolygon
import wastetrack.ui.DEFAULT_SCENARIO_BY_VEHICLE_ID
import wastetrack.ui.FleetState
import wastetrack.ui.components.VerdictBadge
import wastetrack.ui.components.WeightGauge
import wastetrack.ui.components.ZoneCanvas
import wastetrack.ui.formatKg

/** CLAUDE.md §13 driver screen 1 — the driver's home screen. */
@Composable
fun LiveShiftScreen(state: FleetState, modifier: Modifier = Modifier) {
    val snapshot = state.selectedVehicle
    val vehicle = snapshot.vehicle
    val currentZone = snapshot.currentReading?.coordinate?.let { coordinate ->
        state.zones.firstOrNull { isPointInPolygon(coordinate, it.boundary) }
    }
    val statusLine = if (currentZone != null) {
        "Inside ${currentZone.name}"
    } else {
        "En route — not at an authorized zone"
    }
    val routeCoordinates = remember(vehicle.id) {
        DEFAULT_SCENARIO_BY_VEHICLE_ID[vehicle.id].orEmpty().map { it.coordinate }
    }
    val displayZone = currentZone ?: state.zones.first()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        VerdictBadge(snapshot.verdict)
        Spacer(modifier = Modifier.height(8.dp))
        BasicText(text = "${vehicle.id} — ${vehicle.driverName}")
        Spacer(modifier = Modifier.height(8.dp))
        BasicText(text = statusLine)
        Spacer(modifier = Modifier.height(12.dp))
        WeightGauge(
            currentKg = snapshot.currentReading?.loadKg ?: vehicle.tareKg,
            tareKg = vehicle.tareKg,
            toleranceKg = vehicle.toleranceKg,
            maxScaleKg = vehicle.tareKg * 1.6,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        BasicText(
            text = "Load: ${(snapshot.currentReading?.loadKg ?: 0.0).formatKg()} " +
                "(empty weight ${vehicle.tareKg.formatKg()})"
        )
        Spacer(modifier = Modifier.height(16.dp))
        ZoneCanvas(
            zone = displayZone,
            route = routeCoordinates,
            currentPosition = snapshot.currentReading?.coordinate,
            modifier = Modifier.fillMaxWidth().height(320.dp)
        )
    }
}
