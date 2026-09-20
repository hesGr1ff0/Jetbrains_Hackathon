import kotlin.test.Test
import kotlin.test.assertEquals
import wastetrack.data.ACCRA_ZONES
import wastetrack.data.FLEET_VEHICLES
import wastetrack.engine.Verdict
import wastetrack.ui.DEFAULT_SCENARIO_BY_VEHICLE_ID
import wastetrack.ui.FleetViewModel

/**
 * Verifies the Fleet Overview's initial six-vehicle mix is computed by the
 * real engine (via DEFAULT_SCENARIO_BY_VEHICLE_ID), not hardcoded rows.
 */
class FleetOverviewDataTest {
    @Test
    fun sixVehiclesGetRealDistinctVerdictsFromDefaultScenarios() {
        val viewModel = FleetViewModel(FLEET_VEHICLES, ACCRA_ZONES, DEFAULT_SCENARIO_BY_VEHICLE_ID)
        val snapshots = viewModel.state.value.vehicles

        assertEquals(6, snapshots.size)
        assertEquals(Verdict.COMPLIANT, snapshots.getValue("M-24-GT-1842").verdict)
        assertEquals(Verdict.PARTIAL, snapshots.getValue("M-23-GT-0977").verdict)
        assertEquals(Verdict.FLAGGED, snapshots.getValue("M-24-GT-3310").verdict)
        assertEquals(Verdict.FLAGGED, snapshots.getValue("M-22-GT-7165").verdict)
        assertEquals(Verdict.FLAGGED, snapshots.getValue("GT-5109-23").verdict)
        assertEquals(Verdict.FLAGGED, snapshots.getValue("GT-2740-24").verdict)

        val counts = snapshots.values.groupingBy { it.verdict }.eachCount()
        assertEquals(1, counts[Verdict.COMPLIANT])
        assertEquals(1, counts[Verdict.PARTIAL])
        assertEquals(4, counts[Verdict.FLAGGED])
    }
}
