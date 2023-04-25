package proton.android.pass.data.fakes.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.UpdateAlias
import proton.android.pass.data.api.usecases.UpdateAliasContent
import proton.pass.domain.Item
import javax.inject.Inject

class TestUpdateAlias @Inject constructor() : UpdateAlias {

    private var result: Result<Item> =
        Result.failure(IllegalStateException("TestUpdateAlias result not set"))

    fun setResult(result: Result<Item>) {
        this.result = result
    }

    override suspend fun invoke(
        userId: UserId,
        item: Item,
        content: UpdateAliasContent
    ): Item = result.getOrThrow()
}
