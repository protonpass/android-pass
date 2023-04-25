package proton.android.pass.data.fakes.usecases

import proton.android.pass.data.api.usecases.ApplyPendingEvents
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestApplyPendingEvents @Inject constructor() : ApplyPendingEvents {

    private var result: Result<Unit> = Result.success(Unit)

    fun setResult(value: Result<Unit>) {
        result = value
    }

    override suspend fun invoke() {
        result.getOrThrow()
    }
}
