package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

interface DeleteItem {
    suspend operator fun invoke(userId: UserId? = null, shareId: ShareId, itemId: ItemId)
}
