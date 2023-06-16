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

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.TelemetryEntity
import javax.inject.Inject

interface LocalTelemetryDataSource {
    suspend fun store(entity: TelemetryEntity)
    suspend fun getAll(userId: UserId): List<TelemetryEntity>
    suspend fun removeInRange(min: Long, max: Long)
}

class LocalTelemetryDataSourceImpl @Inject constructor(
    private val db: PassDatabase
) : LocalTelemetryDataSource {
    override suspend fun store(entity: TelemetryEntity) {
        db.telemetryEventsDao().insertOrUpdate(entity)
    }

    override suspend fun getAll(userId: UserId): List<TelemetryEntity> =
        db.telemetryEventsDao().getAll(userId.id)

    override suspend fun removeInRange(min: Long, max: Long) =
        db.telemetryEventsDao().deleteInRange(min, max)
}
