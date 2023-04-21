package proton.android.pass.data.fakes.usecases

import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.usecases.ApplyPendingEvents
import javax.inject.Inject

class TestApplyPendingEvents @Inject constructor(): ApplyPendingEvents {

    private var result: LoadingResult<Unit> = LoadingResult.Success(Unit)

    fun setResult(value: LoadingResult<Unit>) {
        result = value
    }

    override suspend fun invoke(): LoadingResult<Unit> = result
}
