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
import wastetrack.engine.VisitClassification
import wastetrack.ui.FleetState
import wastetrack.ui.components.VerdictBadge
import wastetrack.ui.formatKg

/** CLAUDE.md §13 driver screen 3 — shown after a zone visit completes. */
@Composable
fun DropOffResultScreen(state: FleetState, modifier: Modifier = Modifier) {
    val snapshot = state.selectedVehicle
    val lastVisit = snapshot.visits.lastOrNull()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        BasicText(text = "Drop-off result")
        Spacer(modifier = Modifier.height(12.dp))
        if (lastVisit == null) {
            BasicText(text = "No drop-off recorded yet this shift.")
        } else {
            BasicText(text = "Zone: ${lastVisit.zone.name}")
            Spacer(modifier = Modifier.height(4.dp))
            BasicText(text = "Entry: ${lastVisit.entryWeightKg.formatKg()}")
            BasicText(text = "Exit: ${lastVisit.exitWeightKg.formatKg()}")
            BasicText(text = "Change: ${(lastVisit.entryWeightKg - lastVisit.exitWeightKg).formatKg()}")
            Spacer(modifier = Modifier.height(12.dp))
            val classificationLabel = when (lastVisit.classification) {
                VisitClassification.FULL -> "Fully unloaded"
                VisitClassification.PARTIAL -> "Partially unloaded"
                VisitClassification.NONE -> "No unload detected"
            }
            BasicText(text = classificationLabel)
            Spacer(modifier = Modifier.height(8.dp))
            VerdictBadge(snapshot.verdict)
            Spacer(modifier = Modifier.height(4.dp))
            BasicText(text = snapshot.reason)
        }
    }
}
