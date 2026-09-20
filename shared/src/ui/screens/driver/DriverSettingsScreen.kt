package wastetrack.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import wastetrack.ui.FleetState
import wastetrack.ui.FleetViewModel
import wastetrack.ui.Language
import wastetrack.ui.LocalStrings
import wastetrack.ui.formatKg
import wastetrack.ui.theme.Palette
import wastetrack.ui.theme.paletteFor

/** CLAUDE.md §13 driver screen 5. Uses "empty weight," never "tare weight" (§13). */
@Composable
fun DriverSettingsScreen(state: FleetState, viewModel: FleetViewModel, modifier: Modifier = Modifier) {
    val palette = paletteFor(state.darkTheme)
    val strings = LocalStrings.current
    val vehicle = state.selectedVehicle.vehicle
    val initials = vehicle.driverName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")

    Column(modifier = modifier.fillMaxSize().background(palette.background).padding(16.dp)) {
        Text(text = strings.navSettings, fontWeight = FontWeight.Bold, color = palette.onSurface)
        Spacer(modifier = Modifier.height(16.dp))

        Surface(color = palette.surface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(44.dp).background(palette.sidebarActive, CircleShape), contentAlignment = Alignment.Center) {
                    Text(text = initials, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = vehicle.driverName, color = palette.onSurface, fontWeight = FontWeight.SemiBold)
                    Text(text = "${vehicle.id} · ${vehicle.type.name.lowercase().replaceFirstChar { it.uppercase() }}", color = palette.onSurfaceMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = strings.language, color = palette.onSurface, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SettingsChip(strings.english, state.language == Language.EN, palette, Modifier.weight(1f)) { viewModel.setLanguage(Language.EN) }
            SettingsChip(strings.twi, state.language == Language.TWI, palette, Modifier.weight(1f)) { viewModel.setLanguage(Language.TWI) }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = strings.theme, color = palette.onSurface, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SettingsChip(strings.light, !state.darkTheme, palette, Modifier.weight(1f)) { viewModel.setDarkTheme(false) }
            SettingsChip(strings.dark, state.darkTheme, palette, Modifier.weight(1f)) { viewModel.setDarkTheme(true) }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Surface(color = palette.surface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Vehicle", color = palette.onSurface, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                VehicleRow("Empty weight", vehicle.tareKg.formatKg(), palette)
                VehicleRow("Allowed margin", "+/-${vehicle.toleranceKg.formatKg()}", palette)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Surface(color = palette.surface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Drop-off reminders", color = palette.onSurface, fontWeight = FontWeight.SemiBold)
                    Text(text = "Nudge me if I am still loaded late in a shift", color = palette.onSurfaceMuted)
                }
                Switch(checked = state.reminderEnabled, onCheckedChange = { viewModel.setReminderEnabled(it) })
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Surface(color = palette.inProgressBg, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Demo build. Location and weight readings come from a simulator, not real hardware.",
                color = palette.inProgressFg,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun VehicleRow(label: String, value: String, palette: Palette) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(text = label, color = palette.onSurfaceMuted, modifier = Modifier.weight(1f))
        Text(text = value, color = palette.onSurface)
    }
}

@Composable
private fun SettingsChip(label: String, selected: Boolean, palette: Palette, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        color = if (selected) palette.sidebarActive else palette.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else palette.onSurface,
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
