package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId

data class UserPlan(
    val internal: String,
    val humanReadable: String
)

interface GetUserPlan {
    suspend operator fun invoke(userId: UserId): UserPlan
}
