import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import wastetrack.data.ACCRA_ZONES
import wastetrack.data.DRIVE_THROUGH_ROUTE
import wastetrack.data.FLEET_VEHICLES
import wastetrack.engine.Reading
import wastetrack.engine.ReadingSource
import wastetrack.engine.SimulatedSource
import wastetrack.engine.Verdict
import wastetrack.ui.FleetViewModel

/**
 * A [ReadingSource] that emits one reading at a time and then waits for the
 * test to call [step] before emitting the next one. Lets a test land the
 * manual-weight override on an exact reading index deterministically,
 * instead of racing a wall-clock delay against a real coroutine dispatcher.
 */
private class SteppableSource(private val readings: List<Reading>) : ReadingSource {
    private val gate = Channel<Unit>(Channel.RENDEZVOUS)

    suspend fun step() = gate.send(Unit)

    override fun stream(): Flow<Reading> = flow {
        readings.forEachIndexed { index, reading ->
            emit(reading)
            if (index < readings.lastIndex) gate.receive()
        }
    }
}

class FleetViewModelTest {
    private val vehicle = FLEET_VEHICLES.first { it.id == "M-24-GT-1842" }
    private val zone = ACCRA_ZONES.first { it.id == "z1" }

    @Test
    fun manualOverrideForcesDriveThroughToCompliant() = runBlocking {
        // Simulates a judge dragging the weight slider down to empty weight
        // AFTER the vehicle has already entered the zone at its auto (loaded)
        // weight — matching CLAUDE.md §15's demo script. Setting the override
        // before entry would also override the entry reading itself, making
        // entry==exit==tare (zero drop -> NONE, not FULL) instead of the
        // "drove in loaded, then emptied" story this is meant to prove.
        // DRIVE_THROUGH_ROUTE enters the zone at index 11 and exits at index
        // 20 (verified against the real zone box in Scenarios.kt).
        val viewModel = FleetViewModel(vehicle, listOf(zone))
        val source = SteppableSource(DRIVE_THROUGH_ROUTE)
        val job = viewModel.start(this, source)

        repeat(11) { source.step() } // advance through index 11 (zone entry), still on auto weight
        viewModel.setManualKg(vehicle.tareKg) // judge drags the slider now, while inside the zone
        repeat(DRIVE_THROUGH_ROUTE.size - 1 - 11) { source.step() } // drain the rest of the route

        job.join()

        val finalState = viewModel.state.value
        assertEquals(Verdict.COMPLIANT, finalState.verdict)
    }

    @Test
    fun autoModeKeepsDriveThroughFlagged() = runBlocking {
        val viewModel = FleetViewModel(vehicle, listOf(zone))
        viewModel.start(this, SimulatedSource(DRIVE_THROUGH_ROUTE, delayMs = 0L)).join()

        val finalState = viewModel.state.value
        assertEquals(Verdict.FLAGGED, finalState.verdict)
    }

    @Test
    fun noiseTogglePerturbsLoadWithinJitterBound() = runBlocking {
        val viewModel = FleetViewModel(vehicle, listOf(zone))
        viewModel.setNoiseEnabled(true)
        viewModel.start(this, SimulatedSource(DRIVE_THROUGH_ROUTE, delayMs = 0L)).join()

        val lastLoadKg = viewModel.state.value.currentReading!!.loadKg
        assertTrue(lastLoadKg != 320.0, "noise should perturb the raw 320.0kg reading")
        assertTrue(kotlin.math.abs(lastLoadKg - 320.0) <= 0.5, "jitter should stay within +/-0.5kg")
    }
}
