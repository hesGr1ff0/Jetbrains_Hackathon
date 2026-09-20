package wastetrack.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import wastetrack.ui.FleetState
import wastetrack.ui.formatKg
import wastetrack.ui.theme.backgroundFor
import wastetrack.ui.theme.foregroundFor
import wastetrack.ui.theme.paletteFor

/**
 * CLAUDE.md §13 driver screen 4, shown under the "Activity" tab (mockup's
 * 3-tab bottom nav folds this and the drop-off result together).
 *
 * ASSUMPTION: "collected vs. delivered totals" isn't defined precisely in
 * CLAUDE.md. Interpreted as: collected = total waste the vehicle was
 * carrying on entry across all visits (entryWeightKg - tareKg, summed);
 * delivered = how much was actually dropped at authorized zones
 * (entryWeightKg - exitWeightKg, summed).
 *
 * DEVIATION from the reference mockup: no fabricated wall-clock shift
 * start/end times or "recent shifts" history — we only track the current
 * simulated shift; inventing past ones would contradict CLAUDE.md's
 * insistence that simulated data stay honestly labeled, not embellished.
 */
@Composable
fun ShiftSummaryScreen(state: FleetState, onSelectVisit: () -> Unit, modifier: Modifier = Modifier) {
    val palette = paletteFor(state.darkTheme)
    val snapshot = state.selectedVehicle
    val vehicle = snapshot.vehicle
    val collectedKg = snapshot.visits.sumOf { it.entryWeightKg - vehicle.tareKg }
    val deliveredKg = snapshot.visits.sumOf { it.entryWeightKg - it.exitWeightKg }

    Column(modifier = modifier.fillMaxSize().background(palette.background).padding(16.dp)) {
        Text(text = "Shift summary", fontWeight = FontWeight.Bold, color = palette.onSurface)
        Text(text = "Simulated shift", color = palette.onSurfaceMuted)
        Spacer(modifier = Modifier.height(12.dp))

        Surface(color = palette.backgroundFor(snapshot.verdict), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "SHIFT VERDICT", color = palette.foregroundFor(snapshot.verdict))
                Text(text = snapshot.verdict.name, color = palette.foregroundFor(snapshot.verdict), fontWeight = FontWeight.Bold)
                Text(text = snapshot.reason, color = palette.foregroundFor(snapshot.verdict))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatBox("COLLECTED", collectedKg.formatKg(), palette.onSurface, palette, Modifier.weight(1f))
            StatBox("DELIVERED", deliveredKg.formatKg(), palette.compliantFg, palette, Modifier.weight(1f))
            StatBox("ZONE VISITS", snapshot.visits.size.toString(), palette.onSurface, palette, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Zone visits", color = palette.onSurface, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        if (snapshot.visits.isEmpty()) {
            Text(text = "No zone visits this shift.", color = palette.onSurfaceMuted)
        } else {
            snapshot.visits.forEach { visit ->
                Surface(
                    color = palette.surface,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onSelectVisit)
                ) {
                    Row(modifier = Modifier.padding(14.dp).fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = visit.zone.name, color = palette.onSurface, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${visit.entryWeightKg.formatKg()} -> ${visit.exitWeightKg.formatKg()}",
                                color = palette.onSurfaceMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(text = visit.classification.name, color = palette.onSurfaceMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    palette: wastetrack.ui.theme.Palette,
    modifier: Modifier = Modifier
) {
    Surface(color = palette.surface, shape = RoundedCornerShape(10.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, color = palette.onSurfaceMuted)
            Text(text = value, color = valueColor, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
    }
}
