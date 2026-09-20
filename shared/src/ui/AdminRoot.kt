package wastetrack.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import wastetrack.ui.screens.admin.AdminSettingsScreen
import wastetrack.ui.screens.admin.FleetOverviewScreen
import wastetrack.ui.screens.admin.LiveMapScreen
import wastetrack.ui.screens.admin.VehicleDetailScreen

private enum class AdminScreen(val label: String) {
    FLEET_OVERVIEW("Fleet Overview"),
    LIVE_MAP("Live Map"),
    VEHICLE_DETAIL("Vehicle Detail"),
    SETTINGS("Settings"),
}

/** Wide-layout (>=700dp) root — CLAUDE.md §13 admin screens 1-3 + settings; Simulation panel lands in Section 10. */
@Composable
fun AdminRoot(viewModel: FleetViewModel) {
    var screen by remember { mutableStateOf(AdminScreen.FLEET_OVERVIEW) }
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            AdminScreen.entries.forEach { entry ->
                Button(onClick = { screen = entry }, modifier = Modifier.weight(1f)) {
                    Text(entry.label)
                }
            }
        }
        Column(modifier = Modifier.fillMaxSize().weight(1f)) {
            when (screen) {
                AdminScreen.FLEET_OVERVIEW -> FleetOverviewScreen(
                    state = state,
                    onSelectVehicle = { vehicleId ->
                        viewModel.selectVehicle(vehicleId)
                        screen = AdminScreen.VEHICLE_DETAIL
                    }
                )
                AdminScreen.LIVE_MAP -> LiveMapScreen(state)
                AdminScreen.VEHICLE_DETAIL -> VehicleDetailScreen(state)
                AdminScreen.SETTINGS -> AdminSettingsScreen(state, viewModel)
            }
        }
    }
}
