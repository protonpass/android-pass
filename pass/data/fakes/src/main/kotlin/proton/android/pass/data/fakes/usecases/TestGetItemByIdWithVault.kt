package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.api.usecases.GetItemByIdWithVault
import proton.android.pass.data.api.usecases.ItemWithVaultInfo
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

class TestGetItemByIdWithVault @Inject constructor() : GetItemByIdWithVault {

    private val result = testFlow<Result<ItemWithVaultInfo>>()

    fun emitValue(value: Result<ItemWithVaultInfo>) {
        result.tryEmit(value)
    }

    override fun invoke(
        shareId: ShareId,
        itemId: ItemId
    ): Flow<ItemWithVaultInfo> = result.map {
        it.getOrThrow()
    }
}
