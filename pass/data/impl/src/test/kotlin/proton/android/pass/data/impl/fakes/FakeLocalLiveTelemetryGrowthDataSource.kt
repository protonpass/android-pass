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

import proton.android.pass.data.impl.db.entities.LiveTelemetryGrowthEntity
import proton.android.pass.data.impl.local.LocalLiveTelemetryGrowthDataSource

class FakeLocalLiveTelemetryGrowthDataSource : LocalLiveTelemetryGrowthDataSource {

    private var memory: MutableList<LiveTelemetryGrowthEntity> = mutableListOf()
    private var autoId: Long = 1

    fun getMemory(): List<LiveTelemetryGrowthEntity> = memory

    override suspend fun getPendingEvents(): List<LiveTelemetryGrowthEntity> = memory

    override suspend fun deletePendingEvents(events: List<Long>) {
        memory = memory.filter { it.id !in events }.toMutableList()
    }

    override suspend fun storeEvents(events: List<LiveTelemetryGrowthEntity>) {
        events.forEach { memory.add(it.copy(id = autoId++)) }
    }

    override suspend fun deleteAll() {
        memory.clear()
    }
}
