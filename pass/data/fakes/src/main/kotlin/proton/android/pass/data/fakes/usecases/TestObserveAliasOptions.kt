package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import proton.android.pass.data.api.usecases.ObserveAliasOptions
import proton.pass.domain.AliasOptions
import proton.pass.domain.ShareId
import javax.inject.Inject

class TestObserveAliasOptions @Inject constructor() : ObserveAliasOptions {

    private val aliasOptionsFlow: MutableSharedFlow<AliasOptions> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    fun sendAliasOptions(aliasOptions: AliasOptions) = aliasOptionsFlow.tryEmit(aliasOptions)
    override fun invoke(shareId: ShareId): Flow<AliasOptions> = aliasOptionsFlow
}
