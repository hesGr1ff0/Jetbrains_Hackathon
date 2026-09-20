package wastetrack.ui.screens.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import wastetrack.ui.FleetState
import wastetrack.ui.FleetViewModel
import wastetrack.ui.Language

/** CLAUDE.md §13 admin screen 5. */
@Composable
fun AdminSettingsScreen(state: FleetState, viewModel: FleetViewModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        BasicText(text = "Settings")
        Spacer(modifier = Modifier.height(16.dp))

        BasicText(text = "Language")
        Spacer(modifier = Modifier.height(4.dp))
        Row {
            Button(onClick = { viewModel.setLanguage(Language.EN) }) { Text("English") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.setLanguage(Language.TWI) }) { Text("Twi") }
        }
        Spacer(modifier = Modifier.height(4.dp))
        BasicText(text = "Current: ${state.language}")

        Spacer(modifier = Modifier.height(16.dp))
        Row {
            BasicText(text = "Dark theme")
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = state.darkTheme, onCheckedChange = { viewModel.setDarkTheme(it) })
        }
    }
}
