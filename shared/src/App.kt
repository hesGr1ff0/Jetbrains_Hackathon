package wastetrack.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
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
 *
 * The admin/driver branches below are placeholders — the real screens land
 * in Sections 8 (driver) and 9 (admin) of TODO.md; this section only proves
 * the breakpoint switch and the view model wiring.
 */
@Composable
fun App() {
    val viewModel = remember { FleetViewModel(FLEET_VEHICLES.first(), ACCRA_ZONES) }
    val state by viewModel.state.collectAsState()

    WasteTrackTheme {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            if (isAdminLayout(maxWidth)) {
                BasicText("Admin layout — vehicle ${state.vehicle.id} (width=$maxWidth)")
            } else {
                BasicText("Driver layout — vehicle ${state.vehicle.id} (width=$maxWidth)")
            }
        }
    }
}
