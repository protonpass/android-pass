package me.proton.core.pass.test.domain.usecases

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.usecases.ObserveActiveShare

class TestObserveActiveShare : ObserveActiveShare {

    private val activeShareFlow: MutableSharedFlow<ShareId?> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun invoke(): Flow<ShareId?> = activeShareFlow

    fun sendShare(share: ShareId?) = activeShareFlow.tryEmit(share)
}
