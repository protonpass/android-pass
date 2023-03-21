package proton.android.pass.data.fakes.repositories

import proton.android.pass.data.api.repositories.TelemetryRepository
import javax.inject.Inject

class TestTelemetryRepository @Inject constructor() : TelemetryRepository {

    private val memory: MutableList<Entry> = mutableListOf()
    private var sendInvoked = false
    private var storeResult: Result<Unit> = Result.failure(IllegalStateException("storeResult not set"))

    fun getMemory(): List<Entry> = memory
    fun getSendInvoked() = sendInvoked
    fun setStoreResult(value: Result<Unit>) {
        storeResult = value
    }

    override suspend fun storeEntry(event: String, dimensions: Map<String, String>) {
        memory.add(Entry(event, dimensions))
        storeResult.onFailure { throw it }
    }

    override suspend fun sendEvents() {
        sendInvoked = true
    }

    data class Entry(val event: String, val dimensions: Map<String, String>)
}
