package wastetrack.engine

/**
 * Ray-casting point-in-polygon test. `polygon` corners may be given open or
 * closed (first point repeated at the end) — both work.
 */
fun isPointInPolygon(point: Coordinate, polygon: List<Coordinate>): Boolean {
    var inside = false
    val n = polygon.size
    var j = n - 1
    for (i in 0 until n) {
        val pi = polygon[i]
        val pj = polygon[j]
        val crossesLatitude = (pi.lat > point.lat) != (pj.lat > point.lat)
        if (crossesLatitude) {
            val intersectLng = pi.lng + (point.lat - pi.lat) / (pj.lat - pi.lat) * (pj.lng - pi.lng)
            if (point.lng < intersectLng) {
                inside = !inside
            }
        }
        j = i
    }
    return inside
}
