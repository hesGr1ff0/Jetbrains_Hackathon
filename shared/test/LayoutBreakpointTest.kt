import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import wastetrack.ui.isAdminLayout

class LayoutBreakpointTest {
    @Test
    fun narrowWidthIsDriverLayout() {
        assertFalse(isAdminLayout(360.dp))
        assertFalse(isAdminLayout(699.dp))
    }

    @Test
    fun wideWidthIsAdminLayout() {
        assertTrue(isAdminLayout(700.dp))
        assertTrue(isAdminLayout(1200.dp))
    }
}
