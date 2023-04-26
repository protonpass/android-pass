package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.api.usecases.ObserveAllShares
import proton.pass.domain.Share
import javax.inject.Inject

class TestObserveAllShares @Inject constructor() : ObserveAllShares {

    private val observeAllSharesFlow = testFlow<Result<List<Share>>>()

    override fun invoke(userId: UserId?): Flow<List<Share>> = observeAllSharesFlow
        .map { it.getOrThrow() }

    fun sendResult(result: Result<List<Share>>) = observeAllSharesFlow.tryEmit(result)
}
