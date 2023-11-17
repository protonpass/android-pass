/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.data.impl.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiResult
import proton.android.pass.data.api.errors.ShareNotAvailableError
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.responses.EventList
import proton.android.pass.domain.ShareId
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
