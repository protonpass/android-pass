package proton.android.pass.image.fakes

import proton.android.pass.image.api.ClearIconCache
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestClearIconCache @Inject constructor() : ClearIconCache {

    private var result: Result<Unit> = Result.success(Unit)
    private var invoked = false

    fun setResult(value: Result<Unit>) {
        result = value
    }

    fun invoked() = invoked

    override suspend fun invoke() {
        invoked = true
        result.getOrThrow()
    }
}
