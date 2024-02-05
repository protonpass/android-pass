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

package proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.PassEventEntity
import proton.android.pass.data.impl.util.TimeUtil
import proton.android.pass.domain.ShareId
import javax.inject.Inject

class LocalEventDataSourceImpl @Inject constructor(
    private val database: PassDatabase
) : LocalEventDataSource {

    override fun getLatestEventId(userId: UserId, shareId: ShareId): Flow<String?> = database
        .passEventsDao()
        .getLatestEventId(userId.id, shareId.id)
        .map { it?.eventId }

    override suspend fun storeLatestEventId(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        eventId: String,
    ) {
        PassEventEntity(
            eventId = eventId,
            userId = userId.id,
            addressId = addressId.id,
            shareId = shareId.id,
            retrievedAt = TimeUtil.getNowUtc(),
        ).let { passEventEntity -> database.passEventsDao().insertOrUpdate(passEventEntity) }
    }

}
