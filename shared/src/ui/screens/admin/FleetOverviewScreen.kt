package wastetrack.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import wastetrack.engine.Verdict
import wastetrack.ui.FleetState
import wastetrack.ui.components.VerdictBadge
import wastetrack.ui.formatKg

private enum class OverviewFilter { ALL, FLAGGED, PARTIAL }

/** CLAUDE.md §13 admin screen 1 — home. */
@Composable
fun FleetOverviewScreen(
    state: FleetState,
    onSelectVehicle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var filter by remember { mutableStateOf(OverviewFilter.ALL) }
    val allSnapshots = state.vehicles.values.toList()
    val total = allSnapshots.size
    val compliant = allSnapshots.count { it.verdict == Verdict.COMPLIANT }
    val partial = allSnapshots.count { it.verdict == Verdict.PARTIAL }
    val flagged = allSnapshots.count { it.verdict == Verdict.FLAGGED }

    val visibleSnapshots = when (filter) {
        OverviewFilter.ALL -> allSnapshots
        OverviewFilter.FLAGGED -> allSnapshots.filter { it.verdict == Verdict.FLAGGED }
        OverviewFilter.PARTIAL -> allSnapshots.filter { it.verdict == Verdict.PARTIAL }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        BasicText(text = "Fleet Overview")
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            StatCard(label = "Total", value = total, modifier = Modifier.weight(1f))
            StatCard(label = "Compliant", value = compliant, modifier = Modifier.weight(1f))
            StatCard(label = "Partial", value = partial, modifier = Modifier.weight(1f))
            StatCard(label = "Flagged", value = flagged, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = { filter = OverviewFilter.ALL }) { Text("All") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { filter = OverviewFilter.FLAGGED }) { Text("Flagged") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { filter = OverviewFilter.PARTIAL }) { Text("Partial") }
        }

        Spacer(modifier = Modifier.height(16.dp))
        visibleSnapshots.forEach { snapshot ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                BasicText(text = snapshot.vehicle.id, modifier = Modifier.weight(1f))
                BasicText(text = snapshot.vehicle.driverName, modifier = Modifier.weight(1f))
                BasicText(text = snapshot.vehicle.type.name, modifier = Modifier.weight(1f))
                VerdictBadge(snapshot.verdict, modifier = Modifier.weight(1f))
                BasicText(text = snapshot.reason, modifier = Modifier.weight(2f))
                BasicText(text = (snapshot.currentReading?.loadKg ?: 0.0).formatKg(), modifier = Modifier.weight(1f))
                Button(onClick = { onSelectVehicle(snapshot.vehicle.id) }) { Text("Detail") }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: Int, modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFFECEFF1),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.padding(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            BasicText(text = value.toString())
            BasicText(text = label)
        }
    }
}
