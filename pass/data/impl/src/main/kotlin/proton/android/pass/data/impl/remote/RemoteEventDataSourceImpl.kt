package proton.android.pass.data.impl.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiResult
import proton.android.pass.data.api.errors.ShareNotAvailableError
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.responses.EventList
import proton.pass.domain.ShareId
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

    override fun getEvents(userId: UserId, shareId: ShareId, since: String): Flow<EventList> =
        flow {
            val apiResult = api.get<PasswordManagerApi>(userId).invoke {
                getEvents(shareId.id, since)
            }
            if (apiResult is ApiResult.Error.Http && apiResult.proton?.code == DELETED_SHARE_CODE) {
                throw ShareNotAvailableError()
            }
            emit(apiResult.valueOrThrow.events)
        }

    companion object {
        private const val DELETED_SHARE_CODE = 300_004
    }
}
