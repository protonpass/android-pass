package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Result
import proton.pass.domain.Share

interface RefreshShares {
    suspend operator fun invoke(userId: UserId): Result<List<Share>>
}
