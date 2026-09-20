package wastetrack.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun WasteTrackTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val palette = paletteFor(darkTheme)
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.primary,
            secondary = palette.compliantFg,
            error = palette.flaggedFg,
            background = palette.background,
            surface = palette.surface,
            onBackground = palette.onSurface,
            onSurface = palette.onSurface,
        )
    } else {
        lightColorScheme(
            primary = palette.primary,
            secondary = palette.compliantFg,
            error = palette.flaggedFg,
            background = palette.background,
            surface = palette.surface,
            onBackground = palette.onSurface,
            onSurface = palette.onSurface,
        )
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
