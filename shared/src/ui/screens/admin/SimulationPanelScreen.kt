package wastetrack.ui.screens.admin

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
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import wastetrack.data.COMPLIANT_ROUTE
import wastetrack.data.DRIVE_THROUGH_ROUTE
import wastetrack.data.NO_ZONE_ROUTE
import wastetrack.data.PARTIAL_ROUTE
import wastetrack.engine.Reading
import wastetrack.engine.SimulatedSource
import wastetrack.ui.FleetState
import wastetrack.ui.FleetViewModel
import wastetrack.ui.components.VerdictBadge
import wastetrack.ui.components.ZoneCanvas
import wastetrack.ui.formatKg

private const val SLIDER_MIN_KG = 250f
private const val SLIDER_MAX_KG = 400f
private const val SLIDER_DEFAULT_KG = 320f
private const val REPLAY_DELAY_MS = 350L

private enum class SimScenario(val label: String, val route: List<Reading>) {
    COMPLIANT("Compliant", COMPLIANT_ROUTE),
    NO_ZONE("No zone", NO_ZONE_ROUTE),
    DRIVE_THROUGH("Drive-through", DRIVE_THROUGH_ROUTE),
    PARTIAL("Partial", PARTIAL_ROUTE),
}

/**
 * CLAUDE.md §13 admin screen 4 / §15 — demo control, not a real feature.
 * This is what proves the engine is live rather than a pre-recorded
 * animation: swapping scenarios and dragging the weight slider must flip
 * the verdict in front of a judge, not just at shift end.
 */
@Composable
fun SimulationPanelScreen(state: FleetState, viewModel: FleetViewModel, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var selectedScenario by remember { mutableStateOf(SimScenario.COMPLIANT) }
    var sliderPosition by remember { mutableStateOf(SLIDER_DEFAULT_KG) }
    val snapshot = state.selectedVehicle

    fun runScenario(scenario: SimScenario) {
        selectedScenario = scenario
        sliderPosition = SLIDER_DEFAULT_KG
        viewModel.setManualKg(null) // Auto — a fresh run starts on the route's own weight values
        viewModel.setNoiseEnabled(false)
        viewModel.start(scope, SimulatedSource(scenario.route, delayMs = REPLAY_DELAY_MS))
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        BasicText(text = "Simulation")
        Spacer(modifier = Modifier.height(4.dp))
        BasicText(text = "Not a real feature — a demo control for the ${snapshot.vehicle.id} vehicle.")
        Spacer(modifier = Modifier.height(16.dp))

        BasicText(text = "Scenario")
        Spacer(modifier = Modifier.height(4.dp))
        Row {
            SimScenario.entries.forEach { scenario ->
                Button(onClick = { runScenario(scenario) }) { Text(scenario.label) }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        BasicText(text = "Weight override: ${state.manualKg?.formatKg() ?: "Auto"}")
        Slider(
            value = sliderPosition,
            onValueChange = { value ->
                sliderPosition = value
                viewModel.setManualKg(value.toDouble())
            },
            valueRange = SLIDER_MIN_KG..SLIDER_MAX_KG,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Row {
            BasicText(text = "Sensor noise (+/-0.5kg)")
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = state.noiseEnabled, onCheckedChange = { viewModel.setNoiseEnabled(it) })
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = { runScenario(selectedScenario) }) { Text("Run") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.stop() }) { Text("Pause") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                viewModel.stop()
                viewModel.setManualKg(null)
                viewModel.setNoiseEnabled(false)
                sliderPosition = SLIDER_DEFAULT_KG
            }) { Text("Reset") }
        }

        Spacer(modifier = Modifier.height(20.dp))
        VerdictBadge(snapshot.verdict)
        Spacer(modifier = Modifier.height(4.dp))
        BasicText(text = snapshot.reason)
        Spacer(modifier = Modifier.height(4.dp))
        BasicText(text = "Load: ${(snapshot.currentReading?.loadKg ?: 0.0).formatKg()}")

        Spacer(modifier = Modifier.height(16.dp))
        ZoneCanvas(
            zone = state.zones.first(),
            route = selectedScenario.route.map { it.coordinate },
            currentPosition = snapshot.currentReading?.coordinate,
            modifier = Modifier.fillMaxWidth().height(280.dp)
        )
    }
}
