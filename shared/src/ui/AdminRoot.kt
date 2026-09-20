package wastetrack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import wastetrack.ui.screens.admin.AdminSettingsScreen
import wastetrack.ui.screens.admin.FleetOverviewScreen
import wastetrack.ui.screens.admin.LiveMapScreen
import wastetrack.ui.screens.admin.SimulationPanelScreen
import wastetrack.ui.screens.admin.VehicleDetailScreen
import wastetrack.ui.theme.paletteFor

private enum class AdminScreen {
    FLEET_OVERVIEW, LIVE_MAP, VEHICLE_DETAIL, SIMULATION, SETTINGS
}

private fun AdminScreen.label(strings: Strings): String = when (this) {
    AdminScreen.FLEET_OVERVIEW -> strings.navFleetOverview
    AdminScreen.LIVE_MAP -> strings.navLiveMap
    AdminScreen.VEHICLE_DETAIL -> strings.navVehicleDetail
    AdminScreen.SIMULATION -> strings.navSimulation
    AdminScreen.SETTINGS -> strings.navSettings
}

private val SIDEBAR_WIDTH = 220.dp

/** Wide-layout (>=700dp) root — CLAUDE.md §13 admin screens 1-5, dark sidebar nav per the reference mockup. */
@Composable
fun AdminRoot(viewModel: FleetViewModel) {
    var screen by remember { mutableStateOf(AdminScreen.FLEET_OVERVIEW) }
    val state by viewModel.state.collectAsState()
    val palette = paletteFor(state.darkTheme)
    val strings = LocalStrings.current

    Row(modifier = Modifier.fillMaxSize().background(palette.background)) {
        Column(
            modifier = Modifier
                .width(SIDEBAR_WIDTH)
                .fillMaxHeight()
                .background(palette.sidebarBackground)
                .padding(16.dp)
        ) {
            Text(text = strings.appName, color = palette.sidebarOnBackground, fontWeight = FontWeight.Bold)
            Text(text = strings.tagline, color = palette.sidebarOnBackground.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(24.dp))

            AdminScreen.entries.forEach { entry ->
                val isActive = entry == screen
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(
                            if (isActive) palette.sidebarActive else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { screen = entry }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = entry.label(strings),
                        color = if (isActive) Color.White else palette.sidebarOnBackground,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SidebarToggle(
                    label = "EN",
                    active = state.language == Language.EN,
                    palette = paletteFor(state.darkTheme),
                    onClick = { viewModel.setLanguage(Language.EN) },
                    modifier = Modifier.weight(1f)
                )
                SidebarToggle(
                    label = "TWI",
                    active = state.language == Language.TWI,
                    palette = paletteFor(state.darkTheme),
                    onClick = { viewModel.setLanguage(Language.TWI) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            SidebarToggle(
                label = strings.dark,
                active = state.darkTheme,
                palette = paletteFor(state.darkTheme),
                onClick = { viewModel.setDarkTheme(!state.darkTheme) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Column(modifier = Modifier.fillMaxSize().weight(1f)) {
            when (screen) {
                AdminScreen.FLEET_OVERVIEW -> FleetOverviewScreen(
                    state = state,
                    onSelectVehicle = { vehicleId ->
                        viewModel.selectVehicle(vehicleId)
                        screen = AdminScreen.VEHICLE_DETAIL
                    }
                )
                AdminScreen.LIVE_MAP -> LiveMapScreen(state)
                AdminScreen.VEHICLE_DETAIL -> VehicleDetailScreen(state)
                AdminScreen.SIMULATION -> SimulationPanelScreen(state, viewModel)
                AdminScreen.SETTINGS -> AdminSettingsScreen(state, viewModel)
            }
        }
    }
}

@Composable
private fun SidebarToggle(
    label: String,
    active: Boolean,
    palette: wastetrack.ui.theme.Palette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                if (active) palette.sidebarActive else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            color = if (active) Color.White else palette.sidebarOnBackground,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
