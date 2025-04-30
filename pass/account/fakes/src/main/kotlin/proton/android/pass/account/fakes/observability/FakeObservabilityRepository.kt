package proton.android.pass.account.fakes.observability

import me.proton.core.observability.domain.ObservabilityRepository
import me.proton.core.observability.domain.entity.ObservabilityEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeObservabilityRepository @Inject constructor() : ObservabilityRepository {
    override suspend fun addEvent(event: ObservabilityEvent) = Unit

    override suspend fun deleteAllEvents() = Unit

    override suspend fun deleteEvents(events: List<ObservabilityEvent>) = Unit

    override suspend fun deleteEvent(event: ObservabilityEvent) = Unit

    override suspend fun getEventsAndSanitizeDb(limit: Int?): List<ObservabilityEvent> = emptyList()

    override suspend fun getEventCount(): Long = 0
}
