/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.impl.fakes

import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.data.impl.repositories.EventRepository
import proton.android.pass.data.impl.responses.EventList
import proton.android.pass.domain.ShareId

class FakeEventRepository : EventRepository {

    private val deletedUserIds = mutableListOf<UserId>()

    fun getDeletedAllLatestEventIdsMemory(): List<UserId> = deletedUserIds.toList()

    override suspend fun getLatestEventId(userId: UserId, shareId: ShareId): String = ""

    override suspend fun getEvents(
        lastEventId: String,
        userId: UserId,
        shareId: ShareId
    ): EventList = EventList(
        shareResponse = null,
        updatedItems = emptyList(),
        deletedItemIds = emptyList(),
        newRotationId = null,
        latestEventId = lastEventId,
        eventsPending = false
    )

    override suspend fun storeLatestEventId(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        eventId: String
    ) = Unit

    override suspend fun deleteAllLatestEventIds(userId: UserId) {
        deletedUserIds += userId
    }
}
