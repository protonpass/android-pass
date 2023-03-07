package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.ObserveActiveShare
import proton.pass.domain.Share
import javax.inject.Inject

class TestObserveActiveShare @Inject constructor() : ObserveActiveShare {

    private val activeShareFlow: MutableSharedFlow<Share> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun invoke(userId: UserId?): Flow<Share> = activeShareFlow

    fun sendShare(result: Share) = activeShareFlow.tryEmit(result)
}
