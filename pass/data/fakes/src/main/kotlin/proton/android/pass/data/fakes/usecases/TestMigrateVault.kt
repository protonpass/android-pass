package proton.android.pass.data.fakes.usecases

import proton.android.pass.data.api.usecases.MigrateVault
import proton.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestMigrateVault @Inject constructor() : MigrateVault {

    private var result: Result<Unit> = Result.success(Unit)

    private val memory = mutableListOf<Memory>()

    fun memory(): List<Memory> = memory

    fun setResult(value: Result<Unit>) {
        result = value
    }

    override suspend fun invoke(origin: ShareId, dest: ShareId) {
        memory.add(Memory(origin, dest))
        result.getOrThrow()
    }

    data class Memory(
        val origin: ShareId,
        val destination: ShareId
    )
}
