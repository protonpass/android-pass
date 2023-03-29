package proton.android.pass.data.impl.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.data.impl.repositories.OnMemoryDraftRepository

class OnMemoryDraftRepositoryTest {

    private lateinit var instance: OnMemoryDraftRepository

    @Before
    fun setup() {
        instance = OnMemoryDraftRepository()
    }

    @Test
    fun `save and get`() = runTest {
        val value = 1234
        instance.save(KEY, value)
        instance.get<String>(KEY).test {
            assertThat(awaitItem()).isEqualTo(value.some())
        }
    }

    @Test
    fun `get with unknown key emits None`() = runTest {
        instance.get<String>(KEY).test {
            assertThat(awaitItem()).isEqualTo(None)
        }
    }

    @Test
    fun `save twice replaces the value`() = runTest {
        val value = 1234
        instance.save(KEY, 789)
        instance.save(KEY, value)
        instance.get<String>(KEY).test {
            assertThat(awaitItem()).isEqualTo(value.some())
        }
    }

    @Test
    fun `delete deletes the value`() = runTest {
        instance.save(KEY, 1234)
        instance.delete(KEY)
        instance.get<String>(KEY).test {
            assertThat(awaitItem()).isEqualTo(None)
        }
    }

    @Test
    fun `get emits new values and deletions`() = runTest {
        val value1 = 1234
        val value2 = 5678

        val startMutex = Mutex()
        startMutex.lock()
        val writeMutex = Mutex()
        writeMutex.lock()
        val readMutex = Mutex()
        readMutex.lock()

        var received: Option<Int> = None
        instance.save(KEY, value1)

        val job = launch {
            instance.get<Int>(KEY)
                .onStart { startMutex.unlock() }
                .collect {
                    writeMutex.lock()
                    received = it
                    readMutex.unlock()
                }
        }

        startMutex.lock() // Make sure the consumer has started

        writeMutex.unlock() // Allow the consumer to emit
        readMutex.lock() // Make sure the consumer has emitted

        assertThat(received).isEqualTo(value1.some())

        instance.save(KEY, value2)
        writeMutex.unlock() // Allow the consumer to emit
        readMutex.lock() // Make sure the consumer has emitted
        assertThat(received).isEqualTo(value2.some())

        instance.delete(KEY)

        writeMutex.unlock() // Allow the consumer to emit
        readMutex.lock() // Make sure the consumer has emitted
        assertThat(received).isEqualTo(None)

        job.cancel()
    }

    companion object {
        private const val KEY = "test_key"
    }
}
