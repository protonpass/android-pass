package proton.android.pass.data.api.usecases

import proton.android.pass.common.api.LoadingResult
import proton.pass.domain.ShareId

interface DeleteVault {
    suspend operator fun invoke(shareId: ShareId): LoadingResult<Unit>
}

