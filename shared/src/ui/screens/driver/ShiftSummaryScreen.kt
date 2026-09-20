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
import wastetrack.ui.components.VerdictBadge
import wastetrack.ui.formatKg

/**
 * CLAUDE.md §13 driver screen 4 — end-of-shift summary.
 *
 * ASSUMPTION: "collected vs. delivered totals" isn't defined precisely in
 * CLAUDE.md. Interpreted here as: collected = total waste the vehicle was
 * carrying on entry across all visits (entryWeightKg - tareKg, summed);
 * delivered = how much was actually dropped at authorized zones
 * (entryWeightKg - exitWeightKg, summed).
 */
@Composable
fun ShiftSummaryScreen(state: FleetState, modifier: Modifier = Modifier) {
    val snapshot = state.selectedVehicle
    val vehicle = snapshot.vehicle
    val collectedKg = snapshot.visits.sumOf { it.entryWeightKg - vehicle.tareKg }
    val deliveredKg = snapshot.visits.sumOf { it.entryWeightKg - it.exitWeightKg }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        BasicText(text = "Shift summary")
        Spacer(modifier = Modifier.height(12.dp))
        VerdictBadge(snapshot.verdict)
        Spacer(modifier = Modifier.height(4.dp))
        BasicText(text = snapshot.reason)
        Spacer(modifier = Modifier.height(16.dp))
        BasicText(text = "Collected: ${collectedKg.formatKg()}")
        BasicText(text = "Delivered: ${deliveredKg.formatKg()}")
        Spacer(modifier = Modifier.height(16.dp))
        BasicText(text = "Zone visits (${snapshot.visits.size})")
        Spacer(modifier = Modifier.height(4.dp))
        if (snapshot.visits.isEmpty()) {
            BasicText(text = "No zone visits this shift.")
        } else {
            snapshot.visits.forEach { visit ->
                BasicText(
                    text = "${visit.zone.name}: ${visit.entryWeightKg.formatKg()} -> " +
                        "${visit.exitWeightKg.formatKg()} (${visit.classification})"
                )
            }
        }
    }
}
