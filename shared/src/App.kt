package wastetrack.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import wastetrack.data.ACCRA_ZONES
import wastetrack.data.FLEET_VEHICLES
import wastetrack.ui.theme.WasteTrackTheme

/**
 * Single adaptive root: BoxWithConstraints picks the admin (wide, >=700dp)
 * or driver (narrow) layout by width, per CLAUDE.md §6/§14 step 10. Both
 * MainActivity.kt (Android) and the web/desktop entry points display this
 * same composable unmodified.
 */
@Composable
fun App() {
    val viewModel = remember {
        FleetViewModel(FLEET_VEHICLES, ACCRA_ZONES, DEFAULT_SCENARIO_BY_VEHICLE_ID)
    }
    val state by viewModel.state.collectAsState()

    WasteTrackTheme(darkTheme = state.darkTheme) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            if (isAdminLayout(maxWidth)) {
                AdminRoot(viewModel)
            } else {
                DriverRoot(viewModel)
            }
        }
    }
}
