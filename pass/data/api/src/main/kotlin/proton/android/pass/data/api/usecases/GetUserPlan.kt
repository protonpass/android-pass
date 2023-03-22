package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId

interface GetUserPlan {
    suspend operator fun invoke(userId: UserId): String
}
