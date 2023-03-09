package proton.android.pass.data.fakes.usecases

import me.proton.core.domain.entity.SessionUserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.usecases.CreateVault
import proton.pass.domain.Share
import proton.pass.domain.entity.NewVault
import javax.inject.Inject

class TestCreateVault @Inject constructor() : CreateVault {

    private var result: LoadingResult<Share> = LoadingResult.Loading

    fun setResult(result: LoadingResult<Share>) {
        this.result = result
    }

    override suspend fun invoke(
        userId: SessionUserId?,
        vault: NewVault
    ): LoadingResult<Share> = result
}
