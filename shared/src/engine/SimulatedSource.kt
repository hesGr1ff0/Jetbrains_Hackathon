package wastetrack.engine

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Replays a fixed list of readings on a delay, standing in for a real
 * GPS+weight device behind the [ReadingSource] interface (CLAUDE.md §5, §8).
 */
class SimulatedSource(
    private val readings: List<Reading>,
    private val delayMs: Long = 350L
) : ReadingSource {
    override fun stream(): Flow<Reading> = flow {
        for (reading in readings) {
            emit(reading)
            delay(delayMs)
        }
    }
}
