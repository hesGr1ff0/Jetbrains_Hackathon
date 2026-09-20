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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import wastetrack.data.ACCRA_ZONES
import wastetrack.data.DRIVE_THROUGH_ROUTE
import wastetrack.data.FLEET_VEHICLES
import wastetrack.engine.Reading
import wastetrack.engine.ShiftTracker
import wastetrack.engine.SimulatedSource
import wastetrack.engine.Verdict
import wastetrack.ui.components.VerdictBadge
import wastetrack.ui.components.ZoneCanvas

/**
 * SCRATCH SCREEN — not one of the real Section 8/9 screens. Exists only to
 * get the drive-through scenario visible on jvm-app as fast as possible,
 * ahead of the full FleetViewModel / App() shell (Sections 5-9). Wires
 * SimulatedSource + ShiftTracker directly, bypassing the view model.
 */
@Composable
fun DriveThroughScratchScreen() {
    val vehicle = remember { FLEET_VEHICLES.first { it.id == "M-24-GT-1842" } }
    val zone = remember { ACCRA_ZONES.first { it.id == "z1" } }
    val tracker = remember { ShiftTracker(vehicle, listOf(zone)) }

    var currentReading by remember { mutableStateOf<Reading?>(null) }
    var verdict by remember { mutableStateOf(Verdict.IN_PROGRESS) }

    LaunchedEffect(Unit) {
        var index = 0
        SimulatedSource(DRIVE_THROUGH_ROUTE, delayMs = 300L).stream().collect { reading ->
            tracker.record(reading)
            currentReading = reading
            // shiftEnded=false here: the stream is still running. ShiftTracker
            // still recomputes live for any zone visit in progress (that's what
            // lets the slider flip the verdict mid-visit) — it only holds back
            // the terminal "never reached a zone" rule until the shift truly ends.
            verdict = tracker.result(shiftEnded = false).verdict
            // TEMPORARY: no display attached in this environment to screenshot the
            // window, so this line stands in for visual confirmation that the
            // canvas/badge/readout are receiving live, changing state.
            println("[scratch] i=$index lat=${reading.coordinate.lat} lng=${reading.coordinate.lng} loadKg=${reading.loadKg} verdict=$verdict")
            index++
        }
        verdict = tracker.result(shiftEnded = true).verdict
        println("[scratch] route complete, final verdict=$verdict")
    }

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            VerdictBadge(verdict)
            Spacer(modifier = Modifier.height(12.dp))
            BasicText(text = "${vehicle.id} — ${vehicle.driverName}")
            BasicText(text = "Load: ${currentReading?.loadKg ?: 0.0} kg  (empty weight ${vehicle.tareKg} kg)")
            Spacer(modifier = Modifier.height(16.dp))
            ZoneCanvas(
                zone = zone,
                route = DRIVE_THROUGH_ROUTE.map { it.coordinate },
                currentPosition = currentReading?.coordinate,
                modifier = Modifier.fillMaxWidth().height(420.dp)
            )
        }
    }
}
