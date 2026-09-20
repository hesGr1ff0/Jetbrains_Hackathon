package wastetrack.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import wastetrack.ui.theme.Palette
import wastetrack.ui.theme.backgroundFor
import wastetrack.ui.theme.foregroundFor
import wastetrack.ui.theme.paletteFor

private const val SLIDER_MIN_KG = 250f
private const val SLIDER_MAX_KG = 400f
private const val SLIDER_DEFAULT_KG = 320f
private const val REPLAY_DELAY_MS = 350L

private enum class SimScenario(val label: String, val route: List<Reading>) {
    COMPLIANT("Drops off fully", COMPLIANT_ROUTE),
    DRIVE_THROUGH("Drives through", DRIVE_THROUGH_ROUTE),
    PARTIAL("Drops off partly", PARTIAL_ROUTE),
    NO_ZONE("Never visits a zone", NO_ZONE_ROUTE),
}

/**
 * CLAUDE.md §13 admin screen 4 / §15 — demo control, not a real feature.
 * This is what proves the engine is live rather than a pre-recorded
 * animation: swapping scenarios and dragging the weight slider must flip
 * the verdict in front of a judge, not just at shift end.
 */
@Composable
fun SimulationPanelScreen(state: FleetState, viewModel: FleetViewModel, modifier: Modifier = Modifier) {
    val palette = paletteFor(state.darkTheme)
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

    Column(modifier = modifier.fillMaxSize().background(palette.background).padding(24.dp)) {
        Text(text = "Simulation", fontWeight = FontWeight.Bold, color = palette.onSurface)
        Text(
            text = "Stands in for the sensor feed. A real deployment replaces this with a device stream; the logic below it does not change.",
            color = palette.onSurfaceMuted
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxSize()) {
            Surface(color = palette.surface, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Driver behaviour", color = palette.onSurface, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScenarioButton(SimScenario.COMPLIANT, selectedScenario, palette, Modifier.weight(1f)) { runScenario(it) }
                        ScenarioButton(SimScenario.DRIVE_THROUGH, selectedScenario, palette, Modifier.weight(1f)) { runScenario(it) }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScenarioButton(SimScenario.PARTIAL, selectedScenario, palette, Modifier.weight(1f)) { runScenario(it) }
                        ScenarioButton(SimScenario.NO_ZONE, selectedScenario, palette, Modifier.weight(1f)) { runScenario(it) }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Cargo weight", color = palette.onSurface, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text(text = (state.manualKg ?: sliderPosition.toDouble()).formatKg(), color = palette.onSurface, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = sliderPosition,
                        onValueChange = { value ->
                            sliderPosition = value
                            viewModel.setManualKg(value.toDouble())
                        },
                        valueRange = SLIDER_MIN_KG..SLIDER_MAX_KG,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "${SLIDER_MIN_KG.toInt()} empty", color = palette.onSurfaceMuted, modifier = Modifier.weight(1f))
                        Text(text = "${SLIDER_MAX_KG.toInt()} full", color = palette.onSurfaceMuted)
                    }
                    Text(
                        text = "Drag while the vehicle sits inside a zone. The verdict recomputes live from the same engine the app ships with.",
                        color = palette.onSurfaceMuted
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = "Sensor noise", color = palette.onSurface, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ToggleChip("Off", selected = !state.noiseEnabled, palette = palette) { viewModel.setNoiseEnabled(false) }
                        ToggleChip("+/-0.5 kg", selected = state.noiseEnabled, palette = palette) { viewModel.setNoiseEnabled(true) }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PrimaryButton("Run shift", palette = palette) { runScenario(selectedScenario) }
                        SecondaryButton("Pause", palette = palette) { viewModel.stop() }
                        SecondaryButton("Reset", palette = palette) {
                            viewModel.stop()
                            viewModel.setManualKg(null)
                            viewModel.setNoiseEnabled(false)
                            sliderPosition = SLIDER_DEFAULT_KG
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Surface(color = palette.surface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "LIVE VERDICT · ${snapshot.vehicle.id} · ${snapshot.vehicle.driverName}", color = palette.onSurfaceMuted)
                        Spacer(modifier = Modifier.height(8.dp))
                        VerdictBadge(snapshot.verdict, palette = palette)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = snapshot.reason, color = palette.onSurface)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Load: ${(snapshot.currentReading?.loadKg ?: 0.0).formatKg()}", color = palette.onSurfaceMuted, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Surface(color = palette.surface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Same route, different weight, different verdict", color = palette.onSurface, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(10.dp))
                        SameRouteRow("251 kg", "Compliant", "Back to empty weight, so the load was unloaded here", palette.compliantBg, palette.compliantFg, palette)
                        SameRouteRow("310 kg", "Partial", "Some unloaded, cargo still on board", palette.partialBg, palette.partialFg, palette)
                        SameRouteRow("380 kg", "Flagged", "No change inside the zone, so nothing was dropped off", palette.flaggedBg, palette.flaggedFg, palette)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "The route and GPS trail are identical in all three. Only the weight differs.",
                            color = palette.onSurfaceMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                ZoneCanvas(
                    zone = state.zones.first(),
                    route = selectedScenario.route.map { it.coordinate },
                    currentPosition = snapshot.currentReading?.coordinate,
                    modifier = Modifier.fillMaxWidth().height(220.dp)
                )
            }
        }
    }
}

@Composable
private fun ScenarioButton(scenario: SimScenario, selected: SimScenario, palette: Palette, modifier: Modifier, onClick: (SimScenario) -> Unit) {
    val isActive = scenario == selected
    Surface(
        color = if (isActive) palette.onSurface else palette.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.clickable { onClick(scenario) }
    ) {
        Text(
            text = scenario.label,
            color = if (isActive) palette.background else palette.onSurface,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun ToggleChip(label: String, selected: Boolean, palette: Palette, onClick: () -> Unit) {
    Surface(
        color = if (selected) palette.onSurface else palette.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = if (selected) palette.background else palette.onSurface,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun PrimaryButton(label: String, palette: Palette, onClick: () -> Unit) {
    Surface(color = palette.sidebarActive, shape = RoundedCornerShape(8.dp), modifier = Modifier.clickable(onClick = onClick)) {
        Text(text = label, color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp))
    }
}

@Composable
private fun SecondaryButton(label: String, palette: Palette, onClick: () -> Unit) {
    Surface(color = palette.surfaceVariant, shape = RoundedCornerShape(8.dp), modifier = Modifier.clickable(onClick = onClick)) {
        Text(text = label, color = palette.onSurface, modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp))
    }
}

@Composable
private fun SameRouteRow(
    weight: String,
    verdictLabel: String,
    description: String,
    bg: androidx.compose.ui.graphics.Color,
    fg: androidx.compose.ui.graphics.Color,
    palette: Palette
) {
    Surface(color = bg, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(12.dp)) {
            Text(text = weight, fontFamily = FontFamily.Monospace, color = palette.onSurface, modifier = Modifier.width(72.dp))
            Text(text = verdictLabel, color = fg, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(90.dp))
            Text(text = description, color = palette.onSurfaceMuted, modifier = Modifier.weight(1f))
        }
    }
}
