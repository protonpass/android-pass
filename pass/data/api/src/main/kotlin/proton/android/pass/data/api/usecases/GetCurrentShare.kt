package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Result
import proton.pass.domain.Share

interface GetCurrentShare {
    suspend operator fun invoke(userId: UserId): Result<List<Share>>
}
