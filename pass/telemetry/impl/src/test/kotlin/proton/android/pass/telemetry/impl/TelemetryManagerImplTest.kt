package proton.android.pass.telemetry.impl

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.data.fakes.repositories.TestTelemetryRepository
import proton.android.pass.telemetry.api.TelemetryEvent
import proton.android.pass.test.MainDispatcherRule

object TestEvent : TelemetryEvent("test")

class TelemetryManagerImplTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: TelemetryManagerImpl
    private lateinit var repository: TestTelemetryRepository

    @Before
    fun setup() {
        repository = TestTelemetryRepository()
        instance = TelemetryManagerImpl(repository)
    }

    @Test
    fun `sent event is stored`() = runTest {
        repository.setStoreResult(Result.success(Unit))

        val startMutex = Mutex()
        startMutex.lock()

        val eventMutex = Mutex()
        eventMutex.lock()

        val job = launch {
            instance.startListening(
                onSubscribed = { startMutex.unlock() },
                onPerformed = { eventMutex.unlock() }
            )
        }

        // Make sure it is subscribed
        startMutex.lock()

        // Send the event
        instance.sendEvent(TestEvent)

        // Make sure the process has been called
        eventMutex.lock()

        val memory = repository.getMemory()
        assertThat(memory.size).isEqualTo(1)
        assertThat(memory[0].event).isEqualTo(TestEvent.eventName)

        job.cancel()
    }

}
