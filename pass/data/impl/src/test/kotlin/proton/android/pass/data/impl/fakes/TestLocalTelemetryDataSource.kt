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

package proton.android.pass.data.impl.fakes

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.entities.TelemetryEntity
import proton.android.pass.data.impl.local.LocalTelemetryDataSource

class TestLocalTelemetryDataSource : LocalTelemetryDataSource {

    private var memory: MutableList<TelemetryEntity> = mutableListOf()
    private var operationMemory: MutableList<Operation> = mutableListOf()

    fun getMemory(): List<TelemetryEntity> = memory

    fun getOperationMemory(): List<Operation> = operationMemory

    override suspend fun store(entity: TelemetryEntity) {
        memory.add(entity)
        operationMemory.add(Operation.Store(entity))
    }

    override suspend fun getAllByUserId(userId: UserId): List<TelemetryEntity> = memory

    override suspend fun getAll(): List<TelemetryEntity> = memory

    override suspend fun removeInRange(
        userId: UserId,
        min: Long,
        max: Long
    ) {
        operationMemory.add(Operation.RemoveInRange(min = min, max = max))
        val newList = memory.filter { it.id in min..max }
        memory = newList.toMutableList()
    }

    sealed interface Operation {
        data class Store(val element: TelemetryEntity) : Operation
        data class RemoveInRange(val min: Long, val max: Long) : Operation
    }
}
