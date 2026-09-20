import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import wastetrack.data.ACCRA_ZONES
import wastetrack.data.DRIVE_THROUGH_ROUTE
import wastetrack.data.FLEET_VEHICLES
import wastetrack.engine.Verdict
import wastetrack.ui.FleetViewModel

/**
 * Reproduces CLAUDE.md §15's demo script end to end through FleetViewModel
 * (no UI harness available in this environment — see handing_over.md):
 * "run the drive-through scenario ... hand the slider to a judge, they drag
 * it up while the vehicle is inside the zone, verdict flips to Flagged live,
 * then back down, Compliant again." All within the SAME zone visit, proving
 * the live flip isn't just an end-of-shift recomputation.
 */
class SimulationDemoScriptTest {
    private val vehicle = FLEET_VEHICLES.first { it.id == "M-24-GT-1842" }
    private val zone = ACCRA_ZONES.first { it.id == "z1" }

    @Test
    fun sliderFlipsVerdictLiveWithinTheSameZoneVisit() = runBlocking {
        val viewModel = FleetViewModel(listOf(vehicle), listOf(zone), emptyMap())
        val source = SteppableSource(DRIVE_THROUGH_ROUTE)
        val job = viewModel.start(this, source)

        // Zone entry is index 11, exit is index 20 (verified in Scenarios.kt).
        repeat(11) { source.step() } // enter the zone on auto (loaded) weight

        viewModel.setManualKg(vehicle.tareKg) // judge drags the slider DOWN to empty weight
        repeat(2) { source.step() } // indices 12-13, still inside
        assertEquals(
            Verdict.COMPLIANT,
            viewModel.state.value.selectedVehicle.verdict,
            "dragging to empty weight mid-visit should read Compliant live, before the vehicle exits"
        )

        viewModel.setManualKg(320.0) // judge drags the slider back UP to loaded weight
        repeat(2) { source.step() } // indices 14-15, still inside
        assertEquals(
            Verdict.FLAGGED,
            viewModel.state.value.selectedVehicle.verdict,
            "dragging back up mid-visit should flip live to Flagged (the drive-through trick), before the vehicle exits"
        )

        viewModel.setManualKg(vehicle.tareKg) // judge drags back DOWN again
        repeat(DRIVE_THROUGH_ROUTE.size - 1 - 15) { source.step() } // drain the rest of the route

        job.join()

        assertEquals(Verdict.COMPLIANT, viewModel.state.value.selectedVehicle.verdict)
    }
}
