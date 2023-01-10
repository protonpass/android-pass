package proton.android.pass.data.impl.remote

import kotlinx.coroutines.flow.Flow
import proton.android.pass.data.impl.responses.EventList
import me.proton.core.domain.entity.UserId
import proton.pass.domain.ShareId

interface RemoteEventDataSource {
    fun getLatestEventId(userId: UserId, shareId: ShareId): Flow<String>
    fun getEvents(userId: UserId, shareId: ShareId, since: String): Flow<EventList>
}
