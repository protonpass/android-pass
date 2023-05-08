package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId

sealed interface UserPlan {

    fun humanReadableName(): String
    fun internalName(): String

    object Free : UserPlan {
        override fun humanReadableName(): String = "Proton Free"
        override fun internalName(): String = "free"
    }

    data class Paid(val internal: String, val humanReadable: String) : UserPlan {
        override fun humanReadableName(): String = humanReadable
        override fun internalName(): String = internal
    }
}

interface GetUserPlan {
    operator fun invoke(userId: UserId): Flow<UserPlan>
}
