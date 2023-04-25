package proton.android.pass.data.fakes.usecases

import proton.android.pass.data.api.usecases.DeleteVault
import proton.pass.domain.ShareId
import javax.inject.Inject

class TestDeleteVault @Inject constructor() : DeleteVault {
    private var result: Result<Unit> = Result.failure(IllegalStateException("Result not set"))

    fun setResult(value: Result<Unit>) {
        result = value
    }

    override suspend fun invoke(shareId: ShareId) = result.getOrThrow()
}
