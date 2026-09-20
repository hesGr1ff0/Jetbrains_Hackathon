package wastetrack.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import wastetrack.data.ACCRA_ZONES
import wastetrack.data.FLEET_VEHICLES
import wastetrack.ui.theme.WasteTrackTheme
import wastetrack.ui.theme.paletteFor

/**
 * Single adaptive root: BoxWithConstraints picks the admin (wide, >=700dp)
 * or driver (narrow) layout by width, per CLAUDE.md §6/§14 step 10 — unless
 * [TopControlBar]'s mode switch overrides it (added at the user's request,
 * not in CLAUDE.md, so one window can preview both layouts). Both
 * MainActivity.kt (Android) and the web/desktop entry points display this
 * same composable unmodified.
 */
@Composable
fun App() {
    val viewModel = remember {
        FleetViewModel(FLEET_VEHICLES, ACCRA_ZONES, DEFAULT_SCENARIO_BY_VEHICLE_ID)
    }
    val state by viewModel.state.collectAsState()
    val palette = paletteFor(state.darkTheme)

    WasteTrackTheme(darkTheme = state.darkTheme) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopControlBar(
                palette = palette,
                layoutOverride = state.layoutOverride,
                onLayoutOverrideChange = { viewModel.setLayoutOverride(it) },
                driverName = state.selectedVehicle.vehicle.driverName,
                vehicleId = state.selectedVehicle.vehicle.id
            )
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val showAdmin = state.layoutOverride?.let { it == LayoutMode.ADMIN } ?: isAdminLayout(maxWidth)
                if (showAdmin) {
                    AdminRoot(viewModel)
                } else {
                    DriverRoot(viewModel)
                }
            }
        }
    }
}
