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

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.local.LocalUserEventDataSource
import proton.android.pass.data.impl.remote.RemoteUserEventDataSource
import proton.android.pass.domain.UserEventId
import proton.android.pass.domain.events.UserEventList
import javax.inject.Inject

class UserEventRepositoryImpl @Inject constructor(
    private val localUserEventDataSource: LocalUserEventDataSource,
    private val remoteUserEventDataSource: RemoteUserEventDataSource
) : UserEventRepository {

    override suspend fun fetchLatestUserEventId(userId: UserId): UserEventId =
        remoteUserEventDataSource.fetchLatestUserEventId(userId)

    override suspend fun getUserEvents(lastEventId: UserEventId, userId: UserId): UserEventList =
        remoteUserEventDataSource.getUserEvents(userId, lastEventId).toDomain()

    override fun getLatestEventId(userId: UserId): Flow<UserEventId?> =
        localUserEventDataSource.getLatestEventId(userId)

    override suspend fun storeLatestEventId(userId: UserId, eventId: UserEventId) {
        if (localUserEventDataSource.getLatestEventId(userId).first() == eventId) return

        localUserEventDataSource.storeLatestEventId(userId, eventId)
    }

}
