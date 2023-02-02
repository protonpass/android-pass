package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult

interface RefreshContent {
    suspend operator fun invoke(userId: UserId): LoadingResult<Unit>
}
