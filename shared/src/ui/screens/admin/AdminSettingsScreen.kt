package wastetrack.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import wastetrack.ui.FleetState
import wastetrack.ui.FleetViewModel
import wastetrack.ui.Language
import wastetrack.ui.LocalStrings
import wastetrack.ui.theme.Palette
import wastetrack.ui.theme.paletteFor

/** CLAUDE.md §13 admin screen 5. */
@Composable
fun AdminSettingsScreen(state: FleetState, viewModel: FleetViewModel, modifier: Modifier = Modifier) {
    val palette = paletteFor(state.darkTheme)
    val strings = LocalStrings.current
    Column(modifier = modifier.fillMaxSize().background(palette.background).padding(24.dp)) {
        Text(text = strings.navSettings, fontWeight = FontWeight.Bold, color = palette.onSurface)
        Spacer(modifier = Modifier.height(20.dp))

        Text(text = strings.language, color = palette.onSurface, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SettingsChip(strings.english, state.language == Language.EN, palette) { viewModel.setLanguage(Language.EN) }
            SettingsChip(strings.twi, state.language == Language.TWI, palette) { viewModel.setLanguage(Language.TWI) }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = strings.theme, color = palette.onSurface, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SettingsChip(strings.light, !state.darkTheme, palette) { viewModel.setDarkTheme(false) }
            SettingsChip(strings.dark, state.darkTheme, palette) { viewModel.setDarkTheme(true) }
        }
    }
}

@Composable
private fun SettingsChip(label: String, selected: Boolean, palette: Palette, onClick: () -> Unit) {
    Surface(
        color = if (selected) palette.sidebarActive else palette.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else palette.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}
