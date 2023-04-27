package proton.android.pass.image.fakes

import proton.android.pass.image.api.ClearIconCache
import javax.inject.Inject

class TestClearIconCache @Inject constructor() : ClearIconCache {

    private var result: Result<Unit> = Result.success(Unit)

    fun setResult(value: Result<Unit>) {
        result = value
    }

    override suspend fun invoke() {
        result.getOrThrow()
    }
}
