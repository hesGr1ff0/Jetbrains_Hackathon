package wastetrack.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import wastetrack.engine.Verdict
import wastetrack.ui.LocalStrings
import wastetrack.ui.Strings
import wastetrack.ui.theme.Palette
import wastetrack.ui.theme.backgroundFor
import wastetrack.ui.theme.foregroundFor

private fun labelFor(verdict: Verdict, strings: Strings): String = when (verdict) {
    Verdict.IN_PROGRESS -> strings.verdictInProgress
    Verdict.COMPLIANT -> strings.verdictCompliant
    Verdict.PARTIAL -> strings.verdictPartial
    Verdict.FLAGGED -> strings.verdictFlagged
}

/** Never color alone (CLAUDE.md §13) — the label text always carries the verdict word too. */
@Composable
fun VerdictBadge(verdict: Verdict, palette: Palette, modifier: Modifier = Modifier) {
    val strings = LocalStrings.current
    Surface(
        color = palette.backgroundFor(verdict),
        shape = RoundedCornerShape(999.dp),
        modifier = modifier
    ) {
        Text(
            text = labelFor(verdict, strings),
            color = palette.foregroundFor(verdict),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
