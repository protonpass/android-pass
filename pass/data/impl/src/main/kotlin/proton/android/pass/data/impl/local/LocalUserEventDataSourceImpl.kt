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
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.UserEventEntity
import proton.android.pass.domain.UserEventId
import javax.inject.Inject

class LocalUserEventDataSourceImpl @Inject constructor(
    private val database: PassDatabase,
    private val clock: Clock
) : LocalUserEventDataSource {

    override fun getLatestEventId(userId: UserId): Flow<UserEventId?> = database
        .userEventsDao()
        .getLatestEventId(userId.id)
        .map { it?.eventId?.let(::UserEventId) }

    override suspend fun storeLatestEventId(userId: UserId, eventId: UserEventId) {
        UserEventEntity(
            eventId = eventId.id,
            userId = userId.id,
            retrievedAt = clock.now().epochSeconds
        ).let { userEventEntity -> database.userEventsDao().insertOrUpdate(userEventEntity) }
    }

    override suspend fun deleteLatestEventId(userId: UserId) {
        database.userEventsDao().deleteLatestEventId(userId.id)
    }

}
