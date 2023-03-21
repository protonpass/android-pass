package proton.android.pass.telemetry.impl

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onSubscription
import proton.android.pass.data.api.repositories.TelemetryRepository
import proton.android.pass.log.api.PassLogger
import proton.android.pass.telemetry.api.TelemetryEvent
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelemetryManagerImpl @Inject constructor(
    private val telemetryRepository: TelemetryRepository
) : TelemetryManager {

    private val mutableEventFlow: MutableSharedFlow<TelemetryEvent> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = BUFFER_CAPACITY,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun sendEvent(event: TelemetryEvent) {
        mutableEventFlow.tryEmit(event)
    }

    suspend fun startListening(onSubscribed: () -> Unit = {}, onPerformed: () -> Unit = {}) {
        mutableEventFlow
            .onSubscription { onSubscribed() }
            .collect { event ->
                runCatching {
                    performSendEvent(event)
                    onPerformed()
                }.onSuccess {
                    PassLogger.v(TAG, "Event sent successfully")
                }.onFailure {
                    PassLogger.w(TAG, it, "Error sending event")
                }
            }
    }

    private suspend fun performSendEvent(event: TelemetryEvent) {
        telemetryRepository.storeEntry(event.eventName, event.dimensions())
    }

    companion object {
        private const val TAG = "TelemetryManagerImpl"
        private const val BUFFER_CAPACITY = 10
    }
}
