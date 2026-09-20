package wastetrack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import wastetrack.engine.SimulatedSource
import wastetrack.ui.screens.driver.DriverSettingsScreen
import wastetrack.ui.screens.driver.DropOffResultScreen
import wastetrack.ui.screens.driver.LiveShiftScreen
import wastetrack.ui.screens.driver.ShiftSummaryScreen
import wastetrack.ui.theme.paletteFor

private enum class DriverTab(val label: String) {
    SHIFT("Shift"),
    ACTIVITY("Activity"),
    SETTINGS("Settings"),
}

/**
 * Narrow-layout (<700dp) root — CLAUDE.md §13 driver screens, 3-tab bottom
 * nav per the reference mockup (Drop-off Reminder is folded into Live
 * Shift as a conditional banner instead of a 4th tab; Drop-off Result is
 * reached by tapping a visit under Activity).
 */
@Composable
fun DriverRoot(viewModel: FleetViewModel) {
    var tab by remember { mutableStateOf(DriverTab.SHIFT) }
    var viewingResult by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()
    val palette = paletteFor(state.darkTheme)

    LaunchedEffect(state.selectedVehicleId) {
        val route = DEFAULT_SCENARIO_BY_VEHICLE_ID[state.selectedVehicleId].orEmpty()
        viewModel.start(scope, SimulatedSource(route, delayMs = 350L))
    }

    Column(modifier = Modifier.fillMaxSize().background(palette.background)) {
        Column(modifier = Modifier.fillMaxSize().weight(1f)) {
            when {
                tab == DriverTab.ACTIVITY && viewingResult -> DropOffResultScreen(state, onBack = { viewingResult = false })
                tab == DriverTab.SHIFT -> LiveShiftScreen(state)
                tab == DriverTab.ACTIVITY -> ShiftSummaryScreen(state, onSelectVisit = { viewingResult = true })
                tab == DriverTab.SETTINGS -> DriverSettingsScreen(state, viewModel)
            }
        }
        Row(modifier = Modifier.fillMaxWidth().background(palette.surface).padding(vertical = 8.dp)) {
            DriverTab.entries.forEach { entry ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            tab = entry
                            viewingResult = false
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = entry.label,
                        color = if (tab == entry) palette.compliantFg else palette.onSurfaceMuted,
                        fontWeight = if (tab == entry) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
