package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.api.usecases.GetAliasDetails
import proton.pass.domain.AliasDetails
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestGetAliasDetails @Inject constructor() : GetAliasDetails {

    private val flow = testFlow<Result<AliasDetails>>()

    fun sendValue(value: Result<AliasDetails>) {
        flow.tryEmit(value)
    }

    override fun invoke(shareId: ShareId, itemId: ItemId): Flow<AliasDetails> =
        flow.map { it.getOrThrow() }
}
