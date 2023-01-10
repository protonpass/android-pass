package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Result
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

interface TrashItem {
    suspend operator fun invoke(userId: UserId, shareId: ShareId, itemId: ItemId): Result<Unit>
}
