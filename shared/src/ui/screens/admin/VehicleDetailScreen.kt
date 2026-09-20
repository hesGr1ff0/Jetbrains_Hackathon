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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import wastetrack.ui.FleetState
import wastetrack.ui.components.LoadOverTimeChart
import wastetrack.ui.components.VerdictBadge
import wastetrack.ui.formatKg

/** CLAUDE.md §13 admin screen 3. */
@Composable
fun VehicleDetailScreen(state: FleetState, modifier: Modifier = Modifier) {
    val snapshot = state.selectedVehicle
    val vehicle = snapshot.vehicle

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row {
            BasicText(text = "${vehicle.id} — ${vehicle.driverName}")
            Spacer(modifier = Modifier.width(12.dp))
            VerdictBadge(snapshot.verdict)
        }
        Spacer(modifier = Modifier.height(4.dp))
        BasicText(text = snapshot.reason)

        Spacer(modifier = Modifier.height(16.dp))
        BasicText(text = "Zone visits (${snapshot.visits.size})")
        Spacer(modifier = Modifier.height(4.dp))
        if (snapshot.visits.isEmpty()) {
            BasicText(text = "No zone visits recorded.")
        } else {
            Row(modifier = Modifier.fillMaxWidth()) {
                BasicText(text = "Zone", modifier = Modifier.weight(2f))
                BasicText(text = "Entry kg", modifier = Modifier.weight(1f))
                BasicText(text = "Exit kg", modifier = Modifier.weight(1f))
                BasicText(text = "Change kg", modifier = Modifier.weight(1f))
                BasicText(text = "Classification", modifier = Modifier.weight(1f))
            }
            snapshot.visits.forEach { visit ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    BasicText(text = visit.zone.name, modifier = Modifier.weight(2f))
                    BasicText(text = visit.entryWeightKg.formatKg(), modifier = Modifier.weight(1f))
                    BasicText(text = visit.exitWeightKg.formatKg(), modifier = Modifier.weight(1f))
                    BasicText(text = (visit.entryWeightKg - visit.exitWeightKg).formatKg(), modifier = Modifier.weight(1f))
                    BasicText(text = visit.classification.name, modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        BasicText(text = "Load over time (dashed = empty weight, band = tolerance)")
        Spacer(modifier = Modifier.height(4.dp))
        LoadOverTimeChart(
            history = snapshot.loadHistory,
            tareKg = vehicle.tareKg,
            toleranceKg = vehicle.toleranceKg,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
