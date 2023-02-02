package proton.android.pass.data.fakes.usecases

import proton.android.pass.data.api.usecases.UpdateAlias
import proton.android.pass.data.api.usecases.UpdateAliasContent
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.pass.domain.Item
import javax.inject.Inject

class TestUpdateAlias @Inject constructor() : UpdateAlias {

    private var result: LoadingResult<Item> = LoadingResult.Loading

    fun setResult(result: LoadingResult<Item>) {
        this.result = result
    }

    override suspend fun invoke(
        userId: UserId,
        item: Item,
        content: UpdateAliasContent
    ): LoadingResult<Item> = result
}
