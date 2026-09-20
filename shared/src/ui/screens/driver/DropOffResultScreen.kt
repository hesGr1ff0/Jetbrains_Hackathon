package wastetrack.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import wastetrack.engine.VisitClassification
import wastetrack.ui.FleetState
import wastetrack.ui.formatKg
import wastetrack.ui.theme.backgroundFor
import wastetrack.ui.theme.foregroundFor
import wastetrack.ui.theme.paletteFor

/** CLAUDE.md §13 driver screen 3 — shown after a zone visit completes. */
@Composable
fun DropOffResultScreen(state: FleetState, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val palette = paletteFor(state.darkTheme)
    val snapshot = state.selectedVehicle
    val lastVisit = snapshot.visits.lastOrNull()

    Column(modifier = modifier.fillMaxSize().background(palette.background).padding(16.dp)) {
        Text(text = "< Activity", color = palette.onSurfaceMuted, modifier = Modifier.clickable(onClick = onBack))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Drop-off result", fontWeight = FontWeight.Bold, color = palette.onSurface)
        Spacer(modifier = Modifier.height(12.dp))

        if (lastVisit == null) {
            Text(text = "No drop-off recorded yet this shift.", color = palette.onSurfaceMuted)
            return@Column
        }

        val classificationLabel = when (lastVisit.classification) {
            VisitClassification.FULL -> "Full drop-off"
            VisitClassification.PARTIAL -> "Partial drop-off"
            VisitClassification.NONE -> "No unload detected"
        }
        Surface(color = palette.backgroundFor(snapshot.verdict), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = classificationLabel, color = palette.foregroundFor(snapshot.verdict), fontWeight = FontWeight.Bold)
                Text(text = lastVisit.zone.name, color = palette.foregroundFor(snapshot.verdict))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Surface(color = palette.surface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "WEIGHT IN", color = palette.onSurfaceMuted)
                        Text(text = lastVisit.entryWeightKg.formatKg(), color = palette.onSurface, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(text = "WEIGHT OUT", color = palette.onSurfaceMuted)
                        Text(text = lastVisit.exitWeightKg.formatKg(), color = palette.compliantFg, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                ResultRow("Unloaded here", (lastVisit.entryWeightKg - lastVisit.exitWeightKg).formatKg(), palette.onSurface)
                ResultRow("Time inside zone", "${lastVisit.dwellMs / 1000}s", palette.onSurface)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        VerdictBadgeRow(text = snapshot.reason, palette = palette)
    }
}

@Composable
private fun ResultRow(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(text = label, color = color.copy(alpha = 0.7f), modifier = Modifier.weight(1f))
        Text(text = value, color = color, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun VerdictBadgeRow(text: String, palette: wastetrack.ui.theme.Palette) {
    Surface(color = palette.surface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Text(text = text, color = palette.onSurfaceMuted, modifier = Modifier.padding(16.dp))
    }
}
