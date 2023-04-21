package proton.android.pass.data.fakes.usecases

import proton.android.pass.data.api.usecases.searchentry.DeleteAllSearchEntry
import javax.inject.Inject

class TestDeleteAllSearchEntry @Inject constructor() : DeleteAllSearchEntry {

    private var result: Result<Unit> = Result.success(Unit)

    fun setResult(value: Result<Unit>) {
        result = value
    }

    override suspend fun invoke() {
        result.getOrThrow()
    }
}
