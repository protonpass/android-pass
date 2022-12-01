package me.proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import me.proton.pass.domain.ShareId

interface ApplyPendingEvents {
    suspend operator fun invoke(
        userId: UserId,
        shareId: ShareId
    )
}
