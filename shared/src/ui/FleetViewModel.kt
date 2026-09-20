package wastetrack.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random
import wastetrack.engine.Reading
import wastetrack.engine.ReadingSource
import wastetrack.engine.ShiftTracker
import wastetrack.engine.Vehicle
import wastetrack.engine.Verdict
import wastetrack.engine.Zone
import wastetrack.engine.ZoneVisit

private const val NOISE_JITTER_KG = 0.5

enum class Language { EN, TWI }

data class VehicleSnapshot(
    val vehicle: Vehicle,
    val currentReading: Reading? = null,
    val loadHistory: List<Reading> = emptyList(),
    val visits: List<ZoneVisit> = emptyList(),
    val verdict: Verdict = Verdict.IN_PROGRESS,
    val reason: String = "shift in progress",
)

data class FleetState(
    val zones: List<Zone>,
    val vehicles: Map<String, VehicleSnapshot>,
    val selectedVehicleId: String,
    val manualKg: Double? = null, // null = Auto (follow the route's own values)
    val noiseEnabled: Boolean = false,
    val isRunning: Boolean = false,
    val language: Language = Language.EN,
    val darkTheme: Boolean = false,
    val reminderEnabled: Boolean = true,
) {
    val selectedVehicle: VehicleSnapshot get() = vehicles.getValue(selectedVehicleId)
}

/**
 * Drives the whole fleet off a single [StateFlow] per CLAUDE.md §6 ("a
 * single StateFlow-backed view model is sufficient"). Every vehicle gets a
 * real classification up front by running its assigned default scenario
 * fully through a fresh [ShiftTracker] (so the Fleet Overview table shows
 * genuine engine output, not hardcoded rows) — see [defaultScenarios]. Only
 * the currently [selectVehicle]-ed vehicle streams live via [start], which
 * is what the Simulation panel and driver Live Shift screen drive.
 */
class FleetViewModel(
    vehicles: List<Vehicle>,
    zones: List<Zone>,
    defaultScenarios: Map<String, List<Reading>>
) {
    private val _state: MutableStateFlow<FleetState>
    val state: StateFlow<FleetState>

    private var job: Job? = null

    init {
        val snapshots = vehicles.associate { vehicle ->
            val route = defaultScenarios[vehicle.id].orEmpty()
            val tracker = ShiftTracker(vehicle, zones)
            route.forEach { tracker.record(it) }
            val result = tracker.result(shiftEnded = true)
            vehicle.id to VehicleSnapshot(
                vehicle = vehicle,
                currentReading = route.lastOrNull(),
                loadHistory = route,
                visits = result.visits,
                verdict = result.verdict,
                reason = result.reason
            )
        }
        _state = MutableStateFlow(
            FleetState(
                zones = zones,
                vehicles = snapshots,
                selectedVehicleId = vehicles.first().id
            )
        )
        state = _state.asStateFlow()
    }

    fun selectVehicle(vehicleId: String) {
        job?.cancel()
        _state.update {
            it.copy(
                selectedVehicleId = vehicleId,
                manualKg = null,
                noiseEnabled = false,
                isRunning = false
            )
        }
    }

    fun setManualKg(manualKg: Double?) {
        _state.update { it.copy(manualKg = manualKg) }
    }

    fun setNoiseEnabled(enabled: Boolean) {
        _state.update { it.copy(noiseEnabled = enabled) }
    }

    fun setLanguage(language: Language) {
        _state.update { it.copy(language = language) }
    }

    fun setDarkTheme(enabled: Boolean) {
        _state.update { it.copy(darkTheme = enabled) }
    }

    fun setReminderEnabled(enabled: Boolean) {
        _state.update { it.copy(reminderEnabled = enabled) }
    }

    /** Streams [source] into the currently selected vehicle, replacing its tracker. */
    fun start(scope: CoroutineScope, source: ReadingSource): Job {
        job?.cancel()
        val vehicleId = _state.value.selectedVehicleId
        val vehicle = _state.value.vehicles.getValue(vehicleId).vehicle
        val tracker = ShiftTracker(vehicle, _state.value.zones)

        val newJob = scope.launch {
            _state.update { it.copy(isRunning = true) }
            updateSelectedSnapshot(vehicleId) {
                it.copy(currentReading = null, loadHistory = emptyList(), visits = emptyList())
            }
            source.stream().collect { raw ->
                val effective = applyOverrides(raw)
                tracker.record(effective)
                // shiftEnded=false: the stream is still running. The tracker still
                // recomputes live for any zone visit in progress — that's what lets
                // the simulation panel's slider flip the verdict mid-visit — it only
                // holds back the terminal "never reached a zone" rule until the
                // shift truly ends (see ShiftEvaluator.kt).
                val result = tracker.result(shiftEnded = false)
                updateSelectedSnapshot(vehicleId) {
                    it.copy(
                        currentReading = effective,
                        loadHistory = it.loadHistory + effective,
                        visits = result.visits,
                        verdict = result.verdict,
                        reason = result.reason
                    )
                }
            }
            val finalResult = tracker.result(shiftEnded = true)
            updateSelectedSnapshot(vehicleId) {
                it.copy(visits = finalResult.visits, verdict = finalResult.verdict, reason = finalResult.reason)
            }
            _state.update { it.copy(isRunning = false) }
        }
        job = newJob
        return newJob
    }

    fun stop() {
        job?.cancel()
        _state.update { it.copy(isRunning = false) }
    }

    private fun updateSelectedSnapshot(vehicleId: String, transform: (VehicleSnapshot) -> VehicleSnapshot) {
        _state.update { current ->
            val updated = current.vehicles.getValue(vehicleId).let(transform)
            current.copy(vehicles = current.vehicles + (vehicleId to updated))
        }
    }

    private fun applyOverrides(reading: Reading): Reading {
        val current = _state.value
        var loadKg = reading.loadKg
        if (current.noiseEnabled) {
            loadKg += Random.nextDouble(-NOISE_JITTER_KG, NOISE_JITTER_KG)
        }
        current.manualKg?.let { loadKg = it }
        return reading.copy(loadKg = loadKg)
    }
}
