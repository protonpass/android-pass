package proton.android.pass.data.fakes.usecases

import proton.android.pass.data.api.usecases.CreateAlias
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.pass.domain.Item
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewAlias
import javax.inject.Inject

class TestCreateAlias @Inject constructor() : CreateAlias {

    private var result: LoadingResult<Item> = LoadingResult.Loading

    fun setResult(result: LoadingResult<Item>) {
        this.result = result
    }

    override suspend fun invoke(
        userId: UserId,
        shareId: ShareId,
        newAlias: NewAlias
    ): LoadingResult<Item> = result
}
