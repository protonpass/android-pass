package proton.android.pass.data.api.usecases

import proton.android.pass.common.api.Result
import proton.pass.domain.ShareId

interface UpdateActiveShare {
    suspend operator fun invoke(shareId: ShareId): Result<Unit>
}
