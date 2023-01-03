package me.proton.pass.test.domain.usecases

import me.proton.android.pass.data.api.usecases.CreateAlias
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Item
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.entity.NewAlias

class TestCreateAlias : CreateAlias {

    private var result: Result<Item> = Result.Loading

    fun setResult(result: Result<Item>) {
        this.result = result
    }

    override suspend fun invoke(
        userId: UserId,
        shareId: ShareId,
        newAlias: NewAlias
    ): Result<Item> = result
}
