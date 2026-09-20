import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import wastetrack.engine.Reading
import wastetrack.engine.ReadingSource

/**
 * A [ReadingSource] that emits one reading at a time and then waits for the
 * test to call [step] before emitting the next one. Lets a test land a
 * manual-weight override on an exact reading index deterministically,
 * instead of racing a wall-clock delay against a real coroutine dispatcher.
 */
class SteppableSource(private val readings: List<Reading>) : ReadingSource {
    private val gate = Channel<Unit>(Channel.RENDEZVOUS)

    suspend fun step() = gate.send(Unit)

    override fun stream(): Flow<Reading> = flow {
        readings.forEachIndexed { index, reading ->
            emit(reading)
            if (index < readings.lastIndex) gate.receive()
        }
    }
}
