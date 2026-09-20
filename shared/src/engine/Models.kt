package wastetrack.engine

import kotlinx.coroutines.flow.Flow

data class Coordinate(val lat: Double, val lng: Double)

data class Reading(
    val coordinate: Coordinate,
    val loadKg: Double,
    val timestampMs: Long
)

data class Zone(
    val id: String,
    val name: String,
    val boundary: List<Coordinate>, // polygon corners, closed
    val source: String
)

enum class VehicleType { TRICYCLE, TRUCK }

data class Vehicle(
    val id: String,        // e.g. Ghana plate "M-24-GT-1842"
    val driverName: String,
    val type: VehicleType,
    val tareKg: Double,     // empty weight
    val toleranceKg: Double = 3.0
)

enum class VisitClassification { FULL, PARTIAL, NONE }

data class ZoneVisit(
    val zone: Zone,
    val entryWeightKg: Double,
    val exitWeightKg: Double,
    val classification: VisitClassification,
    val dwellMs: Long
)

enum class Verdict { IN_PROGRESS, COMPLIANT, PARTIAL, FLAGGED }

data class ShiftResult(
    val vehicle: Vehicle,
    val visits: List<ZoneVisit>,
    val verdict: Verdict,
    val reason: String
)

interface ReadingSource {
    fun stream(): Flow<Reading>
}
