package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils
import proton.android.pass.data.api.usecases.GetVaultById
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import javax.inject.Inject

class TestGetVaultById @Inject constructor() : GetVaultById {

    private val flow: MutableSharedFlow<Vault> = FlowUtils.testFlow()

    fun emitValue(value: Vault) {
        flow.tryEmit(value)
    }

    override suspend fun invoke(userId: UserId?, shareId: ShareId): Flow<Vault> = flow
}
