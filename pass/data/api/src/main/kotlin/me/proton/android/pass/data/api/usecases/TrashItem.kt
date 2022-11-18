package me.proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId

interface TrashItem {
    suspend operator fun invoke(userId: UserId, shareId: ShareId, itemId: ItemId): Result<Unit>
}
