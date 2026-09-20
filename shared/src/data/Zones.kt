package wastetrack.data

import wastetrack.engine.Coordinate
import wastetrack.engine.Zone

// Approximate degree offset for a ~200m square at Accra's latitude.
// Precise geodesic math is intentionally skipped at this scale (CLAUDE.md §10).
private const val ZONE_HALF_WIDTH_DEG = 0.0009

private const val DIRECTIVE_SOURCE =
    "11 July 2026 Presidential directive reopening Zoomlion transfer stations for tricycle drop-off"

private fun squareZone(id: String, name: String, lat: Double, lng: Double): Zone {
    val north = lat + ZONE_HALF_WIDTH_DEG
    val south = lat - ZONE_HALF_WIDTH_DEG
    val east = lng + ZONE_HALF_WIDTH_DEG
    val west = lng - ZONE_HALF_WIDTH_DEG
    return Zone(
        id = id,
        name = name,
        boundary = listOf(
            Coordinate(lat = south, lng = west),
            Coordinate(lat = south, lng = east),
            Coordinate(lat = north, lng = east),
            Coordinate(lat = north, lng = west),
            Coordinate(lat = south, lng = west),
        ),
        source = DIRECTIVE_SOURCE
    )
}

val ACCRA_ZONES = listOf(
    squareZone("z1", "Achimota Transfer Station (ZoomPak)", lat = 5.6226, lng = -0.2283),
    squareZone("z2", "Teshie Transfer Station", lat = 5.5832, lng = -0.1046),
    squareZone("z3", "Kpone Transfer Station", lat = 5.7031, lng = 0.0287),
    squareZone("z4", "Ashaiman Transfer Station", lat = 5.6830, lng = -0.0488),
    squareZone("z5", "Pantang Transfer Station", lat = 5.7087, lng = -0.1956),
)
