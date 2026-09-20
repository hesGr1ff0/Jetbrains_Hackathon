package wastetrack.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** A colored callout banner — used for the driver's inside-zone / reminder states. */
@Composable
fun StatusBanner(
    title: String,
    body: String,
    foreground: Color,
    background: Color,
    modifier: Modifier = Modifier
) {
    Surface(color = background, shape = RoundedCornerShape(12.dp), modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, color = foreground, fontWeight = FontWeight.SemiBold)
            Text(text = body, color = foreground)
        }
    }
}
