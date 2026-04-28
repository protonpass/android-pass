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
import proton.android.pass.data.impl.db.entities.LiveTelemetryGrowthEntity
import javax.inject.Inject

interface LocalLiveTelemetryGrowthDataSource {
    suspend fun getPendingEvents(): List<LiveTelemetryGrowthEntity>
    suspend fun deletePendingEvents(events: List<Long>)
    suspend fun storeEvents(events: List<LiveTelemetryGrowthEntity>)
    suspend fun deleteAll()
}

class LocalLiveTelemetryGrowthDataSourceImpl @Inject constructor(
    private val database: PassDatabase
) : LocalLiveTelemetryGrowthDataSource {
    override suspend fun getPendingEvents(): List<LiveTelemetryGrowthEntity> =
        database.liveTelemetryGrowthDao().getAll()

    override suspend fun deletePendingEvents(events: List<Long>) {
        database.liveTelemetryGrowthDao().deleteByIds(events)
    }

    override suspend fun storeEvents(events: List<LiveTelemetryGrowthEntity>) {
        database.liveTelemetryGrowthDao().insertOrUpdate(*events.toTypedArray())
    }

    override suspend fun deleteAll() {
        database.liveTelemetryGrowthDao().deleteAll()
    }
}
