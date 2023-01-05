package me.proton.android.pass.data.fakes.usecases

import me.proton.android.pass.data.api.usecases.UpdateAlias
import me.proton.android.pass.data.api.usecases.UpdateAliasContent
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Item
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
