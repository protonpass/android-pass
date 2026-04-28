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

import proton.android.pass.data.impl.db.entities.TelemetryGrowthEntity
import proton.android.pass.data.impl.local.LocalTelemetryGrowthDataSource

class FakeLocalTelemetryGrowthDataSource : LocalTelemetryGrowthDataSource {

    private var memory: MutableList<TelemetryGrowthEntity> = mutableListOf()

    fun getMemory(): List<TelemetryGrowthEntity> = memory

    override suspend fun store(entity: TelemetryGrowthEntity) {
        memory.add(entity)
    }

    override suspend fun getAll(): List<TelemetryGrowthEntity> = memory

    override suspend fun removeInRange(min: Long, max: Long) {
        memory = memory.filter { it.id !in min..max }.toMutableList()
    }

    override suspend fun deleteAll() {
        memory.clear()
    }
}
