package wastetrack.ui.screens.driver

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import wastetrack.ui.FleetState
import wastetrack.ui.approxDistanceMeters
import wastetrack.ui.centroid
import wastetrack.ui.formatMeters

/**
 * CLAUDE.md §13 driver screen 2 — a nudge, not a penalty. Deliberately says
 * "reminder"/"nearest zone", never "violation" or a compliance score (§4).
 */
@Composable
fun DropOffReminderScreen(state: FleetState, modifier: Modifier = Modifier) {
    val snapshot = state.selectedVehicle
    val currentCoordinate = snapshot.currentReading?.coordinate

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        BasicText(text = "Drop-off reminder")
        Spacer(modifier = Modifier.height(12.dp))
        if (!state.reminderEnabled) {
            BasicText(text = "Reminders are turned off in Settings.")
        } else if (currentCoordinate == null) {
            BasicText(text = "Waiting for a location signal...")
        } else {
            val nearest = state.zones.minByOrNull {
                approxDistanceMeters(currentCoordinate, it.centroid())
            }
            if (nearest == null) {
                BasicText(text = "No authorized zones configured.")
            } else {
                val distance = approxDistanceMeters(currentCoordinate, nearest.centroid())
                BasicText(text = "Nearest authorized drop-off: ${nearest.name}")
                Spacer(modifier = Modifier.height(4.dp))
                BasicText(text = "About ${distance.formatMeters()} away")
                Spacer(modifier = Modifier.height(12.dp))
                BasicText(text = "Just a reminder — head there when you're ready to unload.")
            }
        }
    }
}
