package wastetrack.ui.screens.admin

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import wastetrack.engine.VehicleType
import wastetrack.engine.Verdict
import wastetrack.ui.FleetState
import wastetrack.ui.components.VerdictBadge
import wastetrack.ui.formatKg
import wastetrack.ui.theme.Palette
import wastetrack.ui.theme.backgroundFor
import wastetrack.ui.theme.foregroundFor
import wastetrack.ui.theme.paletteFor

private enum class OverviewFilter(val label: String) {
    ALL("All"), FLAGGED("Flagged"), PARTIAL("Partial"), TRICYCLES("Tricycles"), TRUCKS("Trucks")
}

/** CLAUDE.md §13 admin screen 1 — home. */
@Composable
fun FleetOverviewScreen(
    state: FleetState,
    onSelectVehicle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = paletteFor(state.darkTheme)
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
        OverviewFilter.TRICYCLES -> allSnapshots.filter { it.vehicle.type == VehicleType.TRICYCLE }
        OverviewFilter.TRUCKS -> allSnapshots.filter { it.vehicle.type == VehicleType.TRUCK }
    }

    Column(modifier = modifier.fillMaxSize().background(palette.background).padding(24.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Fleet overview", fontWeight = FontWeight.Bold, color = palette.onSurface)
                Text(text = "$total vehicles reporting", color = palette.onSurfaceMuted)
            }
            Surface(color = palette.inProgressBg, shape = RoundedCornerShape(999.dp)) {
                Text(
                    text = "Simulated feed",
                    color = palette.inProgressFg,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(label = "VEHICLES", value = total, valueColor = palette.onSurface, palette = palette, modifier = Modifier.weight(1f))
            StatCard(label = "COMPLIANT", value = compliant, valueColor = palette.compliantFg, palette = palette, modifier = Modifier.weight(1f))
            StatCard(label = "PARTIAL", value = partial, valueColor = palette.partialFg, palette = palette, modifier = Modifier.weight(1f))
            StatCard(label = "FLAGGED", value = flagged, valueColor = palette.flaggedFg, palette = palette, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OverviewFilter.entries.forEach { entry ->
                FilterChip(entry.label, selected = filter == entry, palette = palette) { filter = entry }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Surface(color = palette.surface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(text = "VEHICLE", color = palette.onSurfaceMuted, modifier = Modifier.weight(2f))
                    Text(text = "DRIVER", color = palette.onSurfaceMuted, modifier = Modifier.weight(2f))
                    Text(text = "VERDICT", color = palette.onSurfaceMuted, modifier = Modifier.weight(1f))
                    Text(text = "REASON", color = palette.onSurfaceMuted, modifier = Modifier.weight(3f))
                    Text(text = "LOAD / EMPTY", color = palette.onSurfaceMuted, modifier = Modifier.weight(2f))
                }
                visibleSnapshots.forEach { snapshot ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectVehicle(snapshot.vehicle.id) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Column(modifier = Modifier.weight(2f)) {
                            Text(text = snapshot.vehicle.id, fontFamily = FontFamily.Monospace, color = palette.onSurface)
                            Text(text = snapshot.vehicle.type.name.lowercase().replaceFirstChar { it.uppercase() }, color = palette.onSurfaceMuted)
                        }
                        Text(text = snapshot.vehicle.driverName, color = palette.onSurface, modifier = Modifier.weight(2f))
                        VerdictBadge(snapshot.verdict, palette = palette, modifier = Modifier.weight(1f))
                        Text(text = snapshot.reason, color = palette.onSurfaceMuted, modifier = Modifier.weight(3f))
                        Text(
                            text = "${(snapshot.currentReading?.loadKg ?: 0.0).formatKg()} / ${snapshot.vehicle.tareKg.formatKg()}",
                            fontFamily = FontFamily.Monospace,
                            color = palette.onSurface,
                            modifier = Modifier.weight(2f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: Int, valueColor: androidx.compose.ui.graphics.Color, palette: Palette, modifier: Modifier = Modifier) {
    Surface(color = palette.surface, shape = RoundedCornerShape(12.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, color = palette.onSurfaceMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value.toString(), color = valueColor, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, palette: Palette, onClick: () -> Unit) {
    Surface(
        color = if (selected) palette.onSurface else palette.surface,
        shape = RoundedCornerShape(999.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = if (selected) palette.background else palette.onSurface,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}
