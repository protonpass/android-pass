package proton.android.pass.data.fakes.usecases

import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.usecases.DeleteVault
import proton.pass.domain.ShareId
import javax.inject.Inject

class TestDeleteVault @Inject constructor() : DeleteVault {
    private var result: LoadingResult<Unit> = LoadingResult.Error(IllegalStateException("Result not set"))

    fun setResult(value: LoadingResult<Unit>) {
        result = value
    }

    override suspend fun invoke(shareId: ShareId): LoadingResult<Unit> = result
}
