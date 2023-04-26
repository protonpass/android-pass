package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId

interface RefreshContent {
    suspend operator fun invoke(userId: UserId)
}
