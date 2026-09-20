package wastetrack.ui.theme

import androidx.compose.ui.graphics.Color
import wastetrack.engine.Verdict

/**
 * Design tokens matching the reference mockups (admin: light, cream
 * background + near-black sidebar; driver: dark by default). Not derived
 * from CLAUDE.md — the spec only names verdict colors by word, not shade
 * (see handing_over.md's assumptions list).
 */
data class Palette(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onSurface: Color,
    val onSurfaceMuted: Color,
    val border: Color,
    val sidebarBackground: Color,
    val sidebarOnBackground: Color,
    val sidebarActive: Color,
    val primary: Color,
    val compliantFg: Color,
    val compliantBg: Color,
    val partialFg: Color,
    val partialBg: Color,
    val flaggedFg: Color,
    val flaggedBg: Color,
    val inProgressFg: Color,
    val inProgressBg: Color,
)

val LightPalette = Palette(
    background = Color(0xFFF4F1EA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFECE8DF),
    onSurface = Color(0xFF1A1A1A),
    onSurfaceMuted = Color(0xFF6B6B63),
    border = Color(0xFFDEDACF),
    sidebarBackground = Color(0xFF161616),
    sidebarOnBackground = Color(0xFFE6E6E1),
    sidebarActive = Color(0xFF1F7A5C),
    primary = Color(0xFF1F7A5C),
    compliantFg = Color(0xFF1E7A4C),
    compliantBg = Color(0xFFDDF2E4),
    partialFg = Color(0xFF8A5A00),
    partialBg = Color(0xFFF5E6C8),
    flaggedFg = Color(0xFFB23A32),
    flaggedBg = Color(0xFFF7DEDC),
    inProgressFg = Color(0xFF2F6FBF),
    inProgressBg = Color(0xFFDCEAFB),
)

val DarkPalette = Palette(
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF262626),
    onSurface = Color(0xFFF2F2F0),
    onSurfaceMuted = Color(0xFFA0A0A0),
    border = Color(0xFF333333),
    sidebarBackground = Color(0xFF0D0D0D),
    sidebarOnBackground = Color(0xFFE6E6E1),
    sidebarActive = Color(0xFF1F7A5C),
    primary = Color(0xFF2E9C74),
    compliantFg = Color(0xFF4ADE80),
    compliantBg = Color(0xFF123420),
    partialFg = Color(0xFFE8B84B),
    partialBg = Color(0xFF3A2E05),
    flaggedFg = Color(0xFFEF6A61),
    flaggedBg = Color(0xFF3A1512),
    inProgressFg = Color(0xFF6AA9F4),
    inProgressBg = Color(0xFF122236),
)

fun paletteFor(darkTheme: Boolean): Palette = if (darkTheme) DarkPalette else LightPalette

fun Palette.foregroundFor(verdict: Verdict): Color = when (verdict) {
    Verdict.COMPLIANT -> compliantFg
    Verdict.PARTIAL -> partialFg
    Verdict.FLAGGED -> flaggedFg
    Verdict.IN_PROGRESS -> inProgressFg
}

fun Palette.backgroundFor(verdict: Verdict): Color = when (verdict) {
    Verdict.COMPLIANT -> compliantBg
    Verdict.PARTIAL -> partialBg
    Verdict.FLAGGED -> flaggedBg
    Verdict.IN_PROGRESS -> inProgressBg
}
