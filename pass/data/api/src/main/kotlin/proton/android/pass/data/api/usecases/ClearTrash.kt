package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId

interface ClearTrash {
    suspend operator fun invoke(userId: UserId? = null)
}
