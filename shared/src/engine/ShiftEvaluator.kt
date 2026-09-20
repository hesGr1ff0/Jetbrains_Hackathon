package wastetrack.engine

/**
 * Derives the shift-level verdict from all zone visits completed so far,
 * per the exact rules in CLAUDE.md §9.
 */
fun evaluateShift(visits: List<ZoneVisit>): Pair<Verdict, String> {
    if (visits.isEmpty()) {
        return Verdict.FLAGGED to "never reached an authorized zone."
    }
    if (visits.any { it.classification == VisitClassification.NONE }) {
        return Verdict.FLAGGED to "entered an authorized zone without unloading."
    }
    if (visits.all { it.classification == VisitClassification.FULL }) {
        return Verdict.COMPLIANT to "all zone visits delivered a full load."
    }
    return Verdict.PARTIAL to "at least one zone visit delivered only a partial load."
}

/**
 * Consumes a stream of readings for one vehicle and tracks zone entry/exit
 * to build up completed [ZoneVisit]s. [result] always evaluates a
 * currently-active zone visit as if it closed on the most recent reading —
 * this is what lets the simulation panel's weight slider flip the verdict
 * live while the vehicle is still inside a zone (CLAUDE.md §15). The §9
 * "never reached a zone" rule only fires once [shiftEnded] is genuinely
 * true and no zone has ever been visited; before that, an unstarted shift
 * reads as IN_PROGRESS rather than prematurely Flagged (CLAUDE.md §13 —
 * the driver's Live Shift badge reads "In progress" while active).
 */
class ShiftTracker(
    private val vehicle: Vehicle,
    private val zones: List<Zone>
) {
    private val completedVisits = mutableListOf<ZoneVisit>()
    private var activeZone: Zone? = null
    private var entryWeightKg = 0.0
    private var entryTimestampMs = 0L
    private var lastInsideWeightKg = 0.0
    private var lastInsideTimestampMs = 0L

    fun record(reading: Reading) {
        val zoneNow = zones.firstOrNull { isPointInPolygon(reading.coordinate, it.boundary) }
        val current = activeZone
        if (current != null && zoneNow?.id != current.id) {
            completedVisits += closeVisit(current)
            activeZone = null
        }
        if (activeZone == null && zoneNow != null) {
            activeZone = zoneNow
            entryWeightKg = reading.loadKg
            entryTimestampMs = reading.timestampMs
        }
        if (activeZone != null) {
            lastInsideWeightKg = reading.loadKg
            lastInsideTimestampMs = reading.timestampMs
        }
    }

    private fun closeVisit(zone: Zone): ZoneVisit = ZoneVisit(
        zone = zone,
        entryWeightKg = entryWeightKg,
        exitWeightKg = lastInsideWeightKg,
        classification = classifyVisit(entryWeightKg, lastInsideWeightKg, vehicle.tareKg, vehicle.toleranceKg),
        dwellMs = lastInsideTimestampMs - entryTimestampMs
    )

    fun result(shiftEnded: Boolean): ShiftResult {
        val zone = activeZone
        val visits = if (zone != null) completedVisits + closeVisit(zone) else completedVisits.toList()
        if (visits.isEmpty() && !shiftEnded) {
            return ShiftResult(vehicle, emptyList(), Verdict.IN_PROGRESS, "shift in progress")
        }
        val (verdict, reason) = evaluateShift(visits)
        return ShiftResult(vehicle, visits, verdict, reason)
    }
}
