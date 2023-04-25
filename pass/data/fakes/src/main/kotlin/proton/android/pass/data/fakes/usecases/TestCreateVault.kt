package proton.android.pass.data.fakes.usecases

import me.proton.core.domain.entity.SessionUserId
import proton.android.pass.data.api.usecases.CreateVault
import proton.pass.domain.Share
import proton.pass.domain.entity.NewVault
import javax.inject.Inject

class TestCreateVault @Inject constructor() : CreateVault {

    private var result: Result<Share> =
        Result.failure(IllegalStateException("TestCreateVault result not set"))

    fun setResult(result: Result<Share>) {
        this.result = result
    }

    override suspend fun invoke(
        userId: SessionUserId?,
        vault: NewVault
    ): Share = result.getOrThrow()
}
