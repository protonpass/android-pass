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

    override suspend fun getAll(userId: UserId): List<TelemetryEntity> = memory

    override suspend fun removeInRange(min: Long, max: Long) {
        operationMemory.add(Operation.RemoveInRange(min = min, max = max))
        val newList = memory.filter { it.id >= min && it.id <= max }
        memory = newList.toMutableList()
    }

    sealed interface Operation {
        data class Store(val element: TelemetryEntity) : Operation
        data class RemoveInRange(val min: Long, val max: Long) : Operation
    }
}
