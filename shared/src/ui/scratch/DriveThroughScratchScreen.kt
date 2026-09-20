package wastetrack.ui.scratch

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import wastetrack.data.ACCRA_ZONES
import wastetrack.data.DRIVE_THROUGH_ROUTE
import wastetrack.data.FLEET_VEHICLES
import wastetrack.engine.SimulatedSource
import wastetrack.ui.FleetViewModel
import wastetrack.ui.components.VerdictBadge
import wastetrack.ui.components.ZoneCanvas

/**
 * SCRATCH SCREEN — not one of the real Section 8/9 screens. Exists only to
 * get the drive-through scenario visible on jvm-app as fast as possible,
 * and now to prove Section 5's FleetViewModel drives live state changes
 * (CLAUDE.md build order §14 steps 8-9).
 */
@Composable
fun DriveThroughScratchScreen() {
    val vehicle = remember { FLEET_VEHICLES.first { it.id == "M-24-GT-1842" } }
    val zone = remember { ACCRA_ZONES.first { it.id == "z1" } }
    val viewModel = remember { FleetViewModel(vehicle, listOf(zone)) }
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.start(scope, SimulatedSource(DRIVE_THROUGH_ROUTE, delayMs = 300L))
    }

    LaunchedEffect(state) {
        // TEMPORARY: no display attached in this environment to screenshot the
        // window, so this line stands in for visual confirmation that the
        // canvas/badge/readout are receiving live, changing state from the
        // FleetViewModel's StateFlow.
        println(
            "[scratch] lat=${state.currentReading?.coordinate?.lat} " +
                "lng=${state.currentReading?.coordinate?.lng} " +
                "loadKg=${state.currentReading?.loadKg} " +
                "verdict=${state.verdict} running=${state.isRunning}"
        )
    }

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            VerdictBadge(state.verdict)
            Spacer(modifier = Modifier.height(12.dp))
            BasicText(text = "${vehicle.id} — ${vehicle.driverName}")
            BasicText(text = "Load: ${state.currentReading?.loadKg ?: 0.0} kg  (empty weight ${vehicle.tareKg} kg)")
            Spacer(modifier = Modifier.height(16.dp))
            ZoneCanvas(
                zone = zone,
                route = DRIVE_THROUGH_ROUTE.map { it.coordinate },
                currentPosition = state.currentReading?.coordinate,
                modifier = Modifier.fillMaxWidth().height(420.dp)
            )
        }
    }
}
