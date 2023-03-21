package proton.android.pass.data.impl.fakes

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.remote.RemoteTelemetryDataSource
import proton.android.pass.data.impl.requests.TelemetryRequest

class TestRemoteTelemetryDataSource : RemoteTelemetryDataSource {

    private var memory: MutableList<TelemetryRequest> = mutableListOf()
    private var result: Result<Unit> = Result.failure(IllegalStateException("result not set"))

    fun getMemory() = memory

    fun setResult(value: Result<Unit>) {
        result = value
    }

    override suspend fun send(userId: UserId, events: TelemetryRequest) {
        memory.add(events)
        result.onFailure { throw it }
    }
}
