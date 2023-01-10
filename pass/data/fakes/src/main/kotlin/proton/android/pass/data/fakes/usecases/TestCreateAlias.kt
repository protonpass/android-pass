package proton.android.pass.data.fakes.usecases

import proton.android.pass.data.api.usecases.CreateAlias
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Result
import proton.pass.domain.Item
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewAlias
import javax.inject.Inject

class TestCreateAlias @Inject constructor() : CreateAlias {

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
