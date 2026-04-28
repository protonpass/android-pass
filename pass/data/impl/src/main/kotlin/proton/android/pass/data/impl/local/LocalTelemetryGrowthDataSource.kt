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

package proton.android.pass.data.impl.local

import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.TelemetryGrowthEntity
import javax.inject.Inject

interface LocalTelemetryGrowthDataSource {
    suspend fun store(entity: TelemetryGrowthEntity)
    suspend fun getAll(): List<TelemetryGrowthEntity>
    suspend fun removeInRange(min: Long, max: Long)
    suspend fun deleteAll()
}

class LocalTelemetryGrowthDataSourceImpl @Inject constructor(
    private val db: PassDatabase
) : LocalTelemetryGrowthDataSource {
    override suspend fun store(entity: TelemetryGrowthEntity) {
        db.telemetryGrowthDao().insertOrUpdate(entity)
    }

    override suspend fun getAll(): List<TelemetryGrowthEntity> = db.telemetryGrowthDao().getAll()

    override suspend fun removeInRange(min: Long, max: Long) = db.telemetryGrowthDao().deleteInRange(min, max)

    override suspend fun deleteAll() = db.telemetryGrowthDao().deleteAll()
}
