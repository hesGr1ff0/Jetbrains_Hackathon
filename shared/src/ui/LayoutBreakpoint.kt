package wastetrack.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// CLAUDE.md §6/§13: admin (web, wide) layout at width >= 700dp, driver
// (Android, narrow) layout below it.
val ADMIN_LAYOUT_BREAKPOINT: Dp = 700.dp

/** Pure decision extracted from [App]'s BoxWithConstraints so it's unit-testable. */
fun isAdminLayout(width: Dp): Boolean = width >= ADMIN_LAYOUT_BREAKPOINT
