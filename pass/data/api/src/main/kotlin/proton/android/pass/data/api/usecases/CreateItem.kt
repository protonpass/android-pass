package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.pass.domain.Item
import proton.pass.domain.ItemContents
import proton.pass.domain.ShareId

interface CreateItem {
    suspend operator fun invoke(
        userId: UserId,
        shareId: ShareId,
        itemContents: ItemContents
    ): LoadingResult<Item>
}
