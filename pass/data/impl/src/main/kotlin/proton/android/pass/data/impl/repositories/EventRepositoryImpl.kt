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

import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.data.impl.local.LocalEventDataSource
import proton.android.pass.data.impl.remote.RemoteEventDataSource
import proton.android.pass.data.impl.responses.EventList
import proton.android.pass.domain.ShareId
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val localEventDataSource: LocalEventDataSource,
    private val remoteEventDataSource: RemoteEventDataSource,
) : EventRepository {

    override suspend fun getLatestEventId(
        userId: UserId,
        shareId: ShareId,
    ): String = localEventDataSource.getLatestEventId(userId, shareId).first()
        ?: remoteEventDataSource.getLatestEventId(userId, shareId).first()

    override suspend fun getEvents(
        lastEventId: String,
        userId: UserId,
        shareId: ShareId,
    ): EventList = remoteEventDataSource.getEvents(userId, shareId, lastEventId).first()

    override suspend fun storeLatestEventId(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        eventId: String,
    ) {
        if (localEventDataSource.getLatestEventId(userId, shareId).first() == eventId) return

        localEventDataSource.storeLatestEventId(userId, addressId, shareId, eventId)
    }

}
