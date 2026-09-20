package wastetrack.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import wastetrack.engine.SimulatedSource
import wastetrack.ui.screens.driver.DriverSettingsScreen
import wastetrack.ui.screens.driver.DropOffReminderScreen
import wastetrack.ui.screens.driver.DropOffResultScreen
import wastetrack.ui.screens.driver.LiveShiftScreen
import wastetrack.ui.screens.driver.ShiftSummaryScreen

private enum class DriverScreen(val label: String) {
    LIVE_SHIFT("Live Shift"),
    REMINDER("Reminder"),
    RESULT("Result"),
    SUMMARY("Summary"),
    SETTINGS("Settings"),
}

/** Narrow-layout (<700dp) root — CLAUDE.md §13 driver screens, simple state-based nav (no library needed). */
@Composable
fun DriverRoot(viewModel: FleetViewModel) {
    var screen by remember { mutableStateOf(DriverScreen.LIVE_SHIFT) }
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.selectedVehicleId) {
        val route = DEFAULT_SCENARIO_BY_VEHICLE_ID[state.selectedVehicleId].orEmpty()
        viewModel.start(scope, SimulatedSource(route, delayMs = 350L))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().weight(1f)) {
            when (screen) {
                DriverScreen.LIVE_SHIFT -> LiveShiftScreen(state)
                DriverScreen.REMINDER -> DropOffReminderScreen(state)
                DriverScreen.RESULT -> DropOffResultScreen(state)
                DriverScreen.SUMMARY -> ShiftSummaryScreen(state)
                DriverScreen.SETTINGS -> DriverSettingsScreen(state, viewModel)
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            DriverScreen.entries.forEach { entry ->
                Button(onClick = { screen = entry }, modifier = Modifier.weight(1f)) {
                    Text(entry.label)
                }
            }
        }
    }
}
