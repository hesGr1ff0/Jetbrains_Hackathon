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

data class FleetState(
    val vehicle: Vehicle,
    val zones: List<Zone>,
    val currentReading: Reading? = null,
    val visits: List<ZoneVisit> = emptyList(),
    val verdict: Verdict = Verdict.IN_PROGRESS,
    val reason: String = "shift in progress",
    val manualKg: Double? = null, // null = Auto (follow the route's own values)
    val noiseEnabled: Boolean = false,
    val isRunning: Boolean = false,
)

/**
 * Drives one vehicle's shift off a [ReadingSource], exposing a single
 * [StateFlow] per CLAUDE.md §6 ("a single StateFlow-backed view model is
 * sufficient"). The manual weight override and noise jitter are applied to
 * each incoming reading before it reaches the [ShiftTracker], per §15.
 */
class FleetViewModel(
    vehicle: Vehicle,
    zones: List<Zone>
) {
    private val tracker = ShiftTracker(vehicle, zones)

    private val _state = MutableStateFlow(FleetState(vehicle = vehicle, zones = zones))
    val state: StateFlow<FleetState> = _state.asStateFlow()

    private var job: Job? = null

    fun start(scope: CoroutineScope, source: ReadingSource): Job {
        job?.cancel()
        val newJob = scope.launch {
            _state.update { it.copy(isRunning = true) }
            source.stream().collect { raw ->
                val effective = applyOverrides(raw)
                tracker.record(effective)
                // shiftEnded=false: the stream is still running. The tracker still
                // recomputes live for any zone visit in progress — that's what lets
                // the simulation panel's slider flip the verdict mid-visit — it only
                // holds back the terminal "never reached a zone" rule until the
                // shift truly ends (see ShiftEvaluator.kt).
                val result = tracker.result(shiftEnded = false)
                _state.update {
                    it.copy(
                        currentReading = effective,
                        visits = result.visits,
                        verdict = result.verdict,
                        reason = result.reason
                    )
                }
            }
            val finalResult = tracker.result(shiftEnded = true)
            _state.update {
                it.copy(
                    visits = finalResult.visits,
                    verdict = finalResult.verdict,
                    reason = finalResult.reason,
                    isRunning = false
                )
            }
        }
        job = newJob
        return newJob
    }

    fun stop() {
        job?.cancel()
        _state.update { it.copy(isRunning = false) }
    }

    fun setManualKg(manualKg: Double?) {
        _state.update { it.copy(manualKg = manualKg) }
    }

    fun setNoiseEnabled(enabled: Boolean) {
        _state.update { it.copy(noiseEnabled = enabled) }
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
