package wastetrack.ui

import wastetrack.data.COMPLIANT_ROUTE
import wastetrack.data.DRIVE_THROUGH_ROUTE
import wastetrack.data.NO_ZONE_ROUTE
import wastetrack.data.PARTIAL_ROUTE
import wastetrack.engine.Reading

// ASSUMPTION: CLAUDE.md's four scenarios (§12) are tuned to the tricycle
// tare (250kg) — a truck's tare (2500kg) would misclassify COMPLIANT/PARTIAL
// against that scale. Not specified in CLAUDE.md how the initial Fleet
// Overview mix should look, so: the four tricycles get one canonical
// scenario each (a believable spread of verdicts), and the two trucks get
// the two scenarios whose classification is tare-independent (drive-through
// and no-zone), avoiding a tare/scale mismatch.
val DEFAULT_SCENARIO_BY_VEHICLE_ID: Map<String, List<Reading>> = mapOf(
    "M-24-GT-1842" to COMPLIANT_ROUTE,
    "M-23-GT-0977" to PARTIAL_ROUTE,
    "M-24-GT-3310" to DRIVE_THROUGH_ROUTE,
    "M-22-GT-7165" to NO_ZONE_ROUTE,
    "GT-5109-23" to NO_ZONE_ROUTE,
    "GT-2740-24" to DRIVE_THROUGH_ROUTE,
)
