package me.proton.android.pass.data.impl.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.proton.android.pass.data.impl.responses.EventList
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.pass.data.api.PasswordManagerApi
import me.proton.pass.domain.ShareId
import javax.inject.Inject

class RemoteEventDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteEventDataSource {
    override fun getLatestEventId(userId: UserId, shareId: ShareId): Flow<String> = flow {
        val eventId = api.get<PasswordManagerApi>(userId).invoke {
            getLastEventId(shareId.id)
        }.valueOrThrow.eventId
        emit(eventId)
    }

    override fun getEvents(userId: UserId, shareId: ShareId, since: String): Flow<EventList> = flow {
        val events = api.get<PasswordManagerApi>(userId).invoke {
            getEvents(shareId.id, since)
        }.valueOrThrow.events
        emit(events)
    }
}
