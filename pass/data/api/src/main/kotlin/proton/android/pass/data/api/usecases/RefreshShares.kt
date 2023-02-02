package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.pass.domain.Share

interface RefreshShares {
    suspend operator fun invoke(userId: UserId): LoadingResult<List<Share>>
}
