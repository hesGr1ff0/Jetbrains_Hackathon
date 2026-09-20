import kotlin.test.Test
import kotlin.test.assertEquals
import wastetrack.data.ACCRA_ZONES
import wastetrack.data.COMPLIANT_ROUTE
import wastetrack.data.DRIVE_THROUGH_ROUTE
import wastetrack.data.FLEET_VEHICLES
import wastetrack.data.NO_ZONE_ROUTE
import wastetrack.data.PARTIAL_ROUTE
import wastetrack.engine.Reading
import wastetrack.engine.ShiftResult
import wastetrack.engine.ShiftTracker
import wastetrack.engine.Verdict
import wastetrack.engine.VisitClassification

private val testVehicle = FLEET_VEHICLES.first { it.id == "M-24-GT-1842" }

private fun runScenario(readings: List<Reading>): ShiftResult {
    val tracker = ShiftTracker(testVehicle, ACCRA_ZONES)
    readings.forEach { tracker.record(it) }
    return tracker.result(shiftEnded = true)
}

class EngineTest {

    @Test
    fun compliantScenarioLandsCompliant() {
        val result = runScenario(COMPLIANT_ROUTE)

        assertEquals(Verdict.COMPLIANT, result.verdict)
        assertEquals(1, result.visits.size)
        assertEquals(VisitClassification.FULL, result.visits.single().classification)
        assertEquals("z1", result.visits.single().zone.id)
    }

    @Test
    fun noZoneScenarioLandsFlagged() {
        val result = runScenario(NO_ZONE_ROUTE)

        assertEquals(Verdict.FLAGGED, result.verdict)
        assertEquals("never reached an authorized zone.", result.reason)
        assertEquals(0, result.visits.size)
    }

    @Test
    fun driveThroughScenarioLandsFlaggedWithNoneVisit() {
        val result = runScenario(DRIVE_THROUGH_ROUTE)

        assertEquals(Verdict.FLAGGED, result.verdict)
        assertEquals(1, result.visits.size)
        assertEquals(VisitClassification.NONE, result.visits.single().classification)
    }

    @Test
    fun partialScenarioLandsPartial() {
        val result = runScenario(PARTIAL_ROUTE)

        assertEquals(Verdict.PARTIAL, result.verdict)
        assertEquals(1, result.visits.size)
        assertEquals(VisitClassification.PARTIAL, result.visits.single().classification)
    }
}
