package wastetrack.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import wastetrack.engine.Verdict

// ASSUMPTION: exact hex colors are not specified in CLAUDE.md — only the
// word/color pairing (Compliant=green, Partial=amber, Flagged=red,
// In progress=blue) is specified in §13. These are placeholder shades.
private fun colorFor(verdict: Verdict): Color = when (verdict) {
    Verdict.IN_PROGRESS -> Color(0xFF1565C0)
    Verdict.COMPLIANT -> Color(0xFF2E7D32)
    Verdict.PARTIAL -> Color(0xFFF9A825)
    Verdict.FLAGGED -> Color(0xFFC62828)
}

private fun labelFor(verdict: Verdict): String = when (verdict) {
    Verdict.IN_PROGRESS -> "In progress"
    Verdict.COMPLIANT -> "Compliant"
    Verdict.PARTIAL -> "Partial"
    Verdict.FLAGGED -> "Flagged"
}

@Composable
fun VerdictBadge(verdict: Verdict, modifier: Modifier = Modifier) {
    Surface(
        color = colorFor(verdict),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = labelFor(verdict),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
