package me.proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result

interface RefreshContent {
    suspend operator fun invoke(userId: UserId): Result<Unit>
}
