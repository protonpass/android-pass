package me.proton.pass.test.domain.usecases

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import me.proton.pass.common.api.Result
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.usecases.ObserveActiveShare

class TestObserveActiveShare : ObserveActiveShare {

    private val activeShareFlow: MutableSharedFlow<Result<ShareId?>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun invoke(): Flow<Result<ShareId?>> = activeShareFlow

    fun sendShare(result: Result<ShareId?>) = activeShareFlow.tryEmit(result)
}
