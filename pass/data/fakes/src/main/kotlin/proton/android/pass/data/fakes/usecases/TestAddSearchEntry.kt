package proton.android.pass.data.fakes.usecases

import proton.android.pass.data.api.usecases.searchentry.AddSearchEntry
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestAddSearchEntry @Inject constructor() : AddSearchEntry {

    private var result: Result<Unit> = Result.success(Unit)

    fun setResult(value: Result<Unit>) {
        result = value
    }

    override suspend fun invoke(shareId: ShareId, itemId: ItemId) {
        result.getOrThrow()
    }
}
