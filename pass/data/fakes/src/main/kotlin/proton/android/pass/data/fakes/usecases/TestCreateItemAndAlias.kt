package proton.android.pass.data.fakes.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.CreateItemAndAlias
import proton.pass.domain.Item
import proton.pass.domain.ItemContents
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewAlias
import javax.inject.Inject

class TestCreateItemAndAlias @Inject constructor() : CreateItemAndAlias {

    private var result: Result<Item> = Result.failure(IllegalStateException("Result not set"))
    private var invoked = false

    fun setResult(value: Result<Item>) {
        result = value
    }

    fun hasBeenInvoked(): Boolean = invoked

    override suspend fun invoke(
        userId: UserId?,
        shareId: ShareId,
        itemContents: ItemContents,
        newAlias: NewAlias
    ): Item {
        invoked = true
        return result.getOrThrow()
    }
}
