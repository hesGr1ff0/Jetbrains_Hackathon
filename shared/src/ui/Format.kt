package wastetrack.ui

import kotlin.math.round
import wastetrack.engine.Coordinate
import wastetrack.engine.Zone

// String.format is JVM-only; shared/src also targets wasmJs and Android, so
// formatting is done by hand here to stay platform-free.
fun Double.roundedTo1Decimal(): Double = round(this * 10) / 10.0

fun Double.formatKg(): String = "${roundedTo1Decimal()} kg"

fun Zone.centroid(): Coordinate {
    val corners = boundary.dropLast(1) // last point repeats the first (closed polygon)
    val avgLat = corners.sumOf { it.lat } / corners.size
    val avgLng = corners.sumOf { it.lng } / corners.size
    return Coordinate(lat = avgLat, lng = avgLng)
}

// Flat-earth approximation consistent with squareZone()'s own approach
// (CLAUDE.md §10 says precise geodesic math isn't needed at this scale).
fun approxDistanceMeters(a: Coordinate, b: Coordinate): Double {
    val dLat = (a.lat - b.lat) * 111_320.0
    val dLng = (a.lng - b.lng) * 111_320.0
    return kotlin.math.sqrt(dLat * dLat + dLng * dLng)
}

fun Double.formatMeters(): String = "${round(this).toInt()} m"
