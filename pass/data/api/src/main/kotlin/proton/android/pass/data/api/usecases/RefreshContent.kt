package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Result

interface RefreshContent {
    suspend operator fun invoke(userId: UserId): Result<Unit>
}
