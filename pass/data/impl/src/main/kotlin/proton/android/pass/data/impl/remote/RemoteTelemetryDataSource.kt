package proton.android.pass.data.impl.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.requests.TelemetryRequest
import javax.inject.Inject

interface RemoteTelemetryDataSource {
    suspend fun send(userId: UserId, events: TelemetryRequest)
}

class RemoteTelemetryDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteTelemetryDataSource {
    override suspend fun send(userId: UserId, events: TelemetryRequest) {
        api.get<PasswordManagerApi>(userId)
            .invoke { sendTelemetry(events) }
            .valueOrThrow
    }
}
