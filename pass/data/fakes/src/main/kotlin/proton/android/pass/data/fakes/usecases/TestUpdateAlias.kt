package proton.android.pass.data.fakes.usecases

import proton.android.pass.data.api.usecases.UpdateAlias
import proton.android.pass.data.api.usecases.UpdateAliasContent
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Result
import proton.pass.domain.Item
import javax.inject.Inject

class TestUpdateAlias @Inject constructor() : UpdateAlias {

    private var result: Result<Item> = Result.Loading

    fun setResult(result: Result<Item>) {
        this.result = result
    }

    override suspend fun invoke(
        userId: UserId,
        item: Item,
        content: UpdateAliasContent
    ): Result<Item> = result
}
