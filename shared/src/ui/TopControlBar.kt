package wastetrack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import wastetrack.ui.theme.Palette

/**
 * Not in the reference mockups (each mockup page is captured at a single
 * width) — added at the user's request so one window can preview both
 * layouts without resizing. Sits above both layouts, always visible.
 */
@Composable
fun TopControlBar(
    palette: Palette,
    layoutOverride: LayoutMode?,
    onLayoutOverrideChange: (LayoutMode?) -> Unit,
    driverName: String,
    vehicleId: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(palette.surface)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        ModeSwitch(
            current = layoutOverride,
            onChange = onLayoutOverrideChange,
            palette = palette
        )
        Spacer(modifier = Modifier.width(12.dp))
        ProfileChip(driverName = driverName, vehicleId = vehicleId, palette = palette)
    }
}

@Composable
private fun ModeSwitch(current: LayoutMode?, onChange: (LayoutMode?) -> Unit, palette: Palette) {
    Row(
        modifier = Modifier
            .background(palette.surfaceVariant, RoundedCornerShape(999.dp))
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SegmentButton("Auto", selected = current == null, palette = palette) { onChange(null) }
        SegmentButton("Admin", selected = current == LayoutMode.ADMIN, palette = palette) { onChange(LayoutMode.ADMIN) }
        SegmentButton("Driver", selected = current == LayoutMode.DRIVER, palette = palette) { onChange(LayoutMode.DRIVER) }
    }
}

@Composable
private fun SegmentButton(label: String, selected: Boolean, palette: Palette, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                color = if (selected) palette.sidebarActive else Color.Transparent,
                shape = RoundedCornerShape(999.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else palette.onSurfaceMuted,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun ProfileChip(driverName: String, vehicleId: String, palette: Palette) {
    val initials = driverName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(28.dp).background(palette.sidebarActive, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = initials, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Row {
            Text(text = driverName, color = palette.onSurface, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "· $vehicleId", color = palette.onSurfaceMuted)
        }
    }
}
