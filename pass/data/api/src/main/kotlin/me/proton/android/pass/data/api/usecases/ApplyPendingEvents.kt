package me.proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.pass.domain.ShareId

interface ApplyPendingEvents {
    operator fun invoke(
        userId: UserId,
        shareId: ShareId
    ): Flow<Unit>
}
