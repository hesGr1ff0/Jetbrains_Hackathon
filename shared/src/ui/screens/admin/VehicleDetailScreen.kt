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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import wastetrack.engine.MIN_DROP_KG
import wastetrack.engine.Vehicle
import wastetrack.engine.ZoneVisit
import wastetrack.ui.FleetState
import wastetrack.ui.components.LoadOverTimeChart
import wastetrack.ui.components.VerdictBadge
import wastetrack.ui.formatKg
import wastetrack.ui.theme.Palette
import wastetrack.ui.theme.backgroundFor
import wastetrack.ui.theme.foregroundFor
import wastetrack.ui.theme.paletteFor

/** CLAUDE.md §13 admin screen 3. */
@Composable
fun VehicleDetailScreen(state: FleetState, modifier: Modifier = Modifier) {
    val palette = paletteFor(state.darkTheme)
    val snapshot = state.selectedVehicle
    val vehicle = snapshot.vehicle
    val lastVisit = snapshot.visits.lastOrNull()

    Column(modifier = modifier.fillMaxSize().background(palette.background).padding(24.dp)) {
        Row {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = vehicle.id, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = palette.onSurface)
                Text(text = vehicle.driverName, color = palette.onSurfaceMuted)
            }
            VerdictBadge(snapshot.verdict, palette = palette)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Surface(color = palette.backgroundFor(snapshot.verdict), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Why: ${snapshot.reason}",
                color = palette.foregroundFor(snapshot.verdict),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(14.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxSize()) {
            Surface(color = palette.surface, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(2f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Load through the shift", color = palette.onSurface, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LoadOverTimeChart(
                        history = snapshot.loadHistory,
                        tareKg = vehicle.tareKg,
                        toleranceKg = vehicle.toleranceKg,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(text = "Empty weight ${vehicle.tareKg.formatKg()}, tolerance band +/-${vehicle.toleranceKg.formatKg()}", color = palette.onSurfaceMuted)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Surface(color = palette.surface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Zone visit", color = palette.onSurface, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (lastVisit == null) {
                            Text(text = "No zone visits recorded.", color = palette.onSurfaceMuted)
                        } else {
                            DetailRow("Zone", lastVisit.zone.name, palette)
                            DetailRow("Weight in", lastVisit.entryWeightKg.formatKg(), palette)
                            DetailRow("Weight out", lastVisit.exitWeightKg.formatKg(), palette)
                            DetailRow("Change", (lastVisit.entryWeightKg - lastVisit.exitWeightKg).formatKg(), palette)
                            DetailRow("Classification", lastVisit.classification.name, palette)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Surface(color = palette.surface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "How this was decided", color = palette.onSurface, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (lastVisit == null) {
                            Text(text = "The shift hasn't reached an authorized zone yet.", color = palette.onSurfaceMuted)
                        } else {
                            reasoningFor(lastVisit, vehicle).forEach { line ->
                                Text(text = "- $line", color = palette.onSurfaceMuted)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Location and weight readings are simulated. The classification logic is real.", color = palette.onSurfaceMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, palette: Palette) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(text = label, color = palette.onSurfaceMuted, modifier = Modifier.weight(1f))
        Text(text = value, color = palette.onSurface, fontFamily = FontFamily.Monospace)
    }
}

private fun reasoningFor(visit: ZoneVisit, vehicle: Vehicle): List<String> {
    val drop = visit.entryWeightKg - visit.exitWeightKg
    val lines = mutableListOf("Location fell inside an authorized zone polygon.")
    if (drop < MIN_DROP_KG) {
        lines += "Weight barely changed (${drop.formatKg()}), below the ${MIN_DROP_KG}kg minimum drop — read as no unload."
    } else {
        lines += "Weight dropped ${drop.formatKg()}, above the ${MIN_DROP_KG}kg minimum."
        val toleranceRange = (vehicle.tareKg - vehicle.toleranceKg)..(vehicle.tareKg + vehicle.toleranceKg)
        if (visit.exitWeightKg in toleranceRange) {
            lines += "Exit weight landed inside the tolerance band around empty weight."
        } else {
            lines += "Exit weight stayed well above empty weight — some cargo still on board."
        }
    }
    return lines
}
