package wastetrack.data

import wastetrack.engine.Coordinate
import wastetrack.engine.Reading

private const val READING_INTERVAL_MS = 1000L

private fun buildRoute(coordinates: List<Coordinate>, loadsKg: List<Double>): List<Reading> {
    require(coordinates.size == loadsKg.size)
    return coordinates.mapIndexed { index, coordinate ->
        Reading(coordinate = coordinate, loadKg = loadsKg[index], timestampMs = index * READING_INTERVAL_MS)
    }
}

// Shared physical path used by the compliant, drive-through, and partial
// scenarios below — same GPS trail approaching, dwelling inside, and leaving
// the Achimota zone (5.6226, -0.2283). Only the weight signal (loadKg)
// differs per scenario; verified against the real zone box that indices
// 11-19 fall inside Achimota and every other index falls outside all five
// zones (CLAUDE.md §12).
private val ACHIMOTA_ROUTE_COORDINATES = listOf(
    // approach, outside all zones (0-10)
    Coordinate(lat = 5.6120, lng = -0.2420),
    Coordinate(lat = 5.6140, lng = -0.2410),
    Coordinate(lat = 5.6155, lng = -0.2400),
    Coordinate(lat = 5.6168, lng = -0.2392),
    Coordinate(lat = 5.6180, lng = -0.2384),
    Coordinate(lat = 5.6192, lng = -0.2376),
    Coordinate(lat = 5.6200, lng = -0.2368),
    Coordinate(lat = 5.6206, lng = -0.2360),
    Coordinate(lat = 5.6210, lng = -0.2340),
    Coordinate(lat = 5.6213, lng = -0.2310),
    Coordinate(lat = 5.6216, lng = -0.2295),
    // inside the Achimota zone, dwelling (11-19)
    Coordinate(lat = 5.6219, lng = -0.2290),
    Coordinate(lat = 5.6222, lng = -0.2288),
    Coordinate(lat = 5.6224, lng = -0.2286),
    Coordinate(lat = 5.6226, lng = -0.2283),
    Coordinate(lat = 5.6227, lng = -0.2281),
    Coordinate(lat = 5.6225, lng = -0.2284),
    Coordinate(lat = 5.6223, lng = -0.2287),
    Coordinate(lat = 5.6221, lng = -0.2289),
    Coordinate(lat = 5.6220, lng = -0.2291),
    // departure, outside all zones (20-29)
    Coordinate(lat = 5.6218, lng = -0.2293),
    Coordinate(lat = 5.6222, lng = -0.2300),
    Coordinate(lat = 5.6238, lng = -0.2305),
    Coordinate(lat = 5.6248, lng = -0.2320),
    Coordinate(lat = 5.6260, lng = -0.2340),
    Coordinate(lat = 5.6275, lng = -0.2360),
    Coordinate(lat = 5.6290, lng = -0.2380),
    Coordinate(lat = 5.6305, lng = -0.2400),
    Coordinate(lat = 5.6320, lng = -0.2420),
    Coordinate(lat = 5.6335, lng = -0.2440),
)

// Scenario 1 — Compliant: drops from a loaded weight to within the
// tricycle's tare (250kg) ± tolerance while inside the zone.
private val COMPLIANT_LOADS_KG = listOf(
    320.0, 320.0, 320.0, 320.0, 320.0, 320.0, 320.0, 320.0, 320.0, 320.0, 320.0, // approach
    320.0, 305.0, 292.0, 280.0, 268.0, 258.0, 252.0, 250.0, 250.0,               // inside, dropping to tare
    250.0, 250.0, 250.0, 250.0, 250.0, 250.0, 250.0, 250.0, 250.0, 250.0,        // departure, empty
)

// Scenario 3 — Drive-through: same coordinates as the compliant route, but
// weight stays constant and loaded the entire time, including inside the
// zone — presence without unloading (the flagship "gaming" scenario).
private val DRIVE_THROUGH_LOADS_KG = List(ACHIMOTA_ROUTE_COORDINATES.size) { 320.0 }

// Scenario 4 — Partial: same coordinates again, weight drops but stops well
// short of tare (roughly half the compliant drop).
private val PARTIAL_LOADS_KG = listOf(
    320.0, 320.0, 320.0, 320.0, 320.0, 320.0, 320.0, 320.0, 320.0, 320.0, 320.0, // approach
    320.0, 313.0, 306.0, 300.0, 295.0, 291.0, 288.0, 286.0, 285.0,               // inside, partial drop
    285.0, 285.0, 285.0, 285.0, 285.0, 285.0, 285.0, 285.0, 285.0, 285.0,        // departure, still loaded
)

// Scenario 2 — No zone: a route far from every one of the five zone boxes
// (verified against all five), weight stays loaded throughout.
private val NO_ZONE_ROUTE_COORDINATES = listOf(
    Coordinate(lat = 5.6400, lng = -0.1600),
    Coordinate(lat = 5.6410, lng = -0.1589),
    Coordinate(lat = 5.6420, lng = -0.1579),
    Coordinate(lat = 5.6430, lng = -0.1568),
    Coordinate(lat = 5.6440, lng = -0.1557),
    Coordinate(lat = 5.6450, lng = -0.1546),
    Coordinate(lat = 5.6460, lng = -0.1536),
    Coordinate(lat = 5.6470, lng = -0.1525),
    Coordinate(lat = 5.6480, lng = -0.1514),
    Coordinate(lat = 5.6490, lng = -0.1503),
    Coordinate(lat = 5.6500, lng = -0.1493),
    Coordinate(lat = 5.6510, lng = -0.1482),
    Coordinate(lat = 5.6520, lng = -0.1471),
    Coordinate(lat = 5.6530, lng = -0.1460),
    Coordinate(lat = 5.6540, lng = -0.1450),
    Coordinate(lat = 5.6550, lng = -0.1439),
    Coordinate(lat = 5.6560, lng = -0.1428),
    Coordinate(lat = 5.6570, lng = -0.1417),
    Coordinate(lat = 5.6580, lng = -0.1407),
    Coordinate(lat = 5.6590, lng = -0.1396),
    Coordinate(lat = 5.6600, lng = -0.1385),
    Coordinate(lat = 5.6610, lng = -0.1374),
    Coordinate(lat = 5.6620, lng = -0.1364),
    Coordinate(lat = 5.6630, lng = -0.1353),
    Coordinate(lat = 5.6640, lng = -0.1342),
    Coordinate(lat = 5.6650, lng = -0.1331),
    Coordinate(lat = 5.6660, lng = -0.1321),
    Coordinate(lat = 5.6670, lng = -0.1310),
)

private val NO_ZONE_LOADS_KG = List(NO_ZONE_ROUTE_COORDINATES.size) { 320.0 }

val COMPLIANT_ROUTE: List<Reading> = buildRoute(ACHIMOTA_ROUTE_COORDINATES, COMPLIANT_LOADS_KG)
val NO_ZONE_ROUTE: List<Reading> = buildRoute(NO_ZONE_ROUTE_COORDINATES, NO_ZONE_LOADS_KG)
val DRIVE_THROUGH_ROUTE: List<Reading> = buildRoute(ACHIMOTA_ROUTE_COORDINATES, DRIVE_THROUGH_LOADS_KG)
val PARTIAL_ROUTE: List<Reading> = buildRoute(ACHIMOTA_ROUTE_COORDINATES, PARTIAL_LOADS_KG)
